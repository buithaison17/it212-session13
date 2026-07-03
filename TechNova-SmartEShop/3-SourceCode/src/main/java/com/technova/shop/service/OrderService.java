package com.technova.shop.service;

import com.technova.shop.dto.order.OrderItemResponse;
import com.technova.shop.dto.order.OrderResponse;
import com.technova.shop.dto.order.PlaceOrderRequest;
import com.technova.shop.entity.*;
import com.technova.shop.exception.BadRequestException;
import com.technova.shop.exception.ResourceNotFoundException;
import com.technova.shop.repository.CartRepository;
import com.technova.shop.repository.OrderRepository;
import com.technova.shop.repository.ProductRepository;
import com.technova.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Xử lý các nghiệp vụ liên quan đến Đặt hàng và Quản lý đơn hàng.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    /**
     * Khách hàng đặt hàng từ giỏ hàng hiện tại.
     * Thực hiện trừ tồn kho, ghi nhận giá mua lịch sử, xóa giỏ hàng.
     */
    @Transactional
    public OrderResponse placeOrder(String username, PlaceOrderRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        // Lấy giỏ hàng kèm các items
        Cart cart = cartRepository.findWithItemsByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng của người dùng."));

        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng của bạn đang trống. Vui lòng thêm sản phẩm trước khi thanh toán.");
        }

        // Tạo đơn hàng mới
        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            // Kiểm tra tồn kho của sản phẩm tại thời điểm đặt hàng
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Sản phẩm \"" + product.getName() + "\" không đủ số lượng tồn kho. Vui lòng cập nhật lại giỏ hàng.");
            }

            // Trừ số lượng trong kho và cập nhật sản phẩm
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Tạo chi tiết dòng đơn hàng (Lưu giá tại thời điểm mua để tránh thay đổi sau này)
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            orderItems.add(orderItem);

            // Tính tổng tiền đơn hàng
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Lưu đơn hàng vào Database
        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt thành công
        cartService.clearCart(cart);

        return mapToResponse(savedOrder);
    }

    /**
     * Khách hàng: Lấy danh sách lịch sử đơn hàng cá nhân.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));

        // Sử dụng findAllByUserIdOrderByOrderedAtDesc để lấy kèm items tránh N+1 Query
        List<Order> orders = orderRepository.findAllByUserIdOrderByOrderedAtDesc(user.getId());
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Admin/Manager: Lấy danh sách tất cả các đơn hàng trong hệ thống.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        // Sử dụng findAllWithUserAndItemsOrderByOrderedAtDesc tránh N+1 Query
        List<Order> orders = orderRepository.findAllWithUserAndItemsOrderByOrderedAtDesc();
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Helper map Entity -> DTO
    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .username(order.getUser().getUsername())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .orderItems(itemResponses)
                .orderedAt(order.getOrderedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
