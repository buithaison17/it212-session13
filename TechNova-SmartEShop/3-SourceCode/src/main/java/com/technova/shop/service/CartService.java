package com.technova.shop.service;

import com.technova.shop.dto.cart.AddToCartRequest;
import com.technova.shop.dto.cart.CartItemResponse;
import com.technova.shop.dto.cart.CartResponse;
import com.technova.shop.entity.Cart;
import com.technova.shop.entity.CartItem;
import com.technova.shop.entity.Product;
import com.technova.shop.entity.User;
import com.technova.shop.exception.BadRequestException;
import com.technova.shop.exception.ResourceNotFoundException;
import com.technova.shop.repository.CartItemRepository;
import com.technova.shop.repository.CartRepository;
import com.technova.shop.repository.ProductRepository;
import com.technova.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Xử lý các nghiệp vụ liên quan đến Giỏ hàng (Cart) của Khách hàng.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Lấy giỏ hàng của người dùng hiện tại theo username.
     * Sử dụng findWithItemsByUserId (với @EntityGraph) để tránh N+1 Query.
     */
    @Transactional(readOnly = true)
    public CartResponse getCartOfUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        Cart cart = getOrCreateCart(user);
        return mapToResponse(cart);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng của người dùng hiện tại.
     * Kiểm tra tồn kho trước khi thêm.
     */
    @Transactional
    public CartResponse addItemToCart(String username, AddToCartRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        Product product = productRepository.findByIdAndActiveTrue(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại hoặc đã bị ẩn."));

        // Kiểm tra số lượng yêu cầu
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Số lượng sản phẩm thêm vào phải lớn hơn 0.");
        }

        // Lấy giỏ hàng kèm các items (sử dụng EntityGraph)
        Cart cart = getOrCreateCart(user);

        // Tìm xem sản phẩm đã có trong giỏ hàng chưa
        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        int targetQuantity = request.getQuantity();
        if (existingItemOpt.isPresent()) {
            targetQuantity += existingItemOpt.get().getQuantity();
        }

        // Kiểm tra xem số lượng yêu cầu có vượt quá tồn kho không
        if (product.getStockQuantity() < targetQuantity) {
            throw new BadRequestException("Sản phẩm \"" + product.getName() + "\" không đủ số lượng tồn kho. Hiện còn: " + product.getStockQuantity());
        }

        if (existingItemOpt.isPresent()) {
            // Nếu đã tồn tại, cập nhật số lượng
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(targetQuantity);
            cartItemRepository.save(existingItem);
        } else {
            // Nếu chưa tồn tại, tạo mới item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        // Lưu giỏ hàng
        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    /**
     * Xóa một dòng sản phẩm khỏi giỏ hàng.
     */
    @Transactional
    public CartResponse removeItemFromCart(String username, Long itemId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dòng sản phẩm này trong giỏ hàng."));

        // Kiểm tra xem item này có thuộc về giỏ hàng của user hiện tại không
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Bạn không thể thực hiện thao tác trên giỏ hàng của người khác.");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    /**
     * Xóa sạch giỏ hàng (sau khi Checkout thành công).
     */
    @Transactional
    public void clearCart(Cart cart) {
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    // Helper: Tìm hoặc khởi tạo giỏ hàng cho User
    private Cart getOrCreateCart(User user) {
        return cartRepository.findWithItemsByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    // Helper map Entity -> DTO và tính tổng tiền
    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productPrice(item.getProduct().getPrice())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // Tính tổng tiền động trong giỏ hàng
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .cartItems(items)
                .totalAmount(totalAmount)
                .build();
    }
}
