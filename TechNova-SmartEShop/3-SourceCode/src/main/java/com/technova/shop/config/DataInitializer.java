package com.technova.shop.config;

import com.technova.shop.entity.*;
import com.technova.shop.repository.CartRepository;
import com.technova.shop.repository.ProductRepository;
import com.technova.shop.repository.RoleRepository;
import com.technova.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Lớp khởi tạo dữ liệu mẫu cho Database khi ứng dụng chạy lần đầu.
 * Tự động tạo các Roles bảo mật, các tài khoản demo (Manager, Staff, Customer) và sản phẩm mẫu.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("--- Bắt đầu khởi tạo dữ liệu mẫu cho Database TechNova ---");

        // 1. Khởi tạo các Roles bảo mật
        Role customerRole = getOrCreateRole("ROLE_CUSTOMER", "Quyền hạn của Khách hàng mua sắm");
        Role staffRole = getOrCreateRole("ROLE_STAFF", "Quyền hạn của Nhân viên kho quản lý sản phẩm");
        Role managerRole = getOrCreateRole("ROLE_MANAGER", "Quyền hạn của Quản lý hệ thống cao nhất");

        // 2. Khởi tạo tài khoản Manager (Quản lý) mẫu
        if (!userRepository.existsByUsername("manager")) {
            Set<Role> roles = new HashSet<>();
            roles.add(managerRole);
            User manager = User.builder()
                    .username("manager")
                    .email("manager@technova.com")
                    .password(passwordEncoder.encode("manager123"))
                    .fullName("Giám đốc TechNova")
                    .phone("0912345678")
                    .address("Hà Nội, Việt Nam")
                    .enabled(true)
                    .roles(roles)
                    .build();
            userRepository.save(manager);
            log.info("Đã tạo tài khoản Manager mẫu: manager / manager123");
        }

        // 3. Khởi tạo tài khoản Staff (Nhân viên kho) mẫu
        if (!userRepository.existsByUsername("staff")) {
            Set<Role> roles = new HashSet<>();
            roles.add(staffRole);
            User staff = User.builder()
                    .username("staff")
                    .email("staff@technova.com")
                    .password(passwordEncoder.encode("staff123"))
                    .fullName("Nhân viên thủ kho TechNova")
                    .phone("0987654321")
                    .address("TP. Hồ Chí Minh, Việt Nam")
                    .enabled(true)
                    .roles(roles)
                    .build();
            userRepository.save(staff);
            log.info("Đã tạo tài khoản Staff mẫu: staff / staff123");
        }

        // 4. Khởi tạo tài khoản Customer (Khách hàng) mẫu và giỏ hàng của họ
        if (!userRepository.existsByUsername("customer")) {
            Set<Role> roles = new HashSet<>();
            roles.add(customerRole);
            User customer = User.builder()
                    .username("customer")
                    .email("customer@technova.com")
                    .password(passwordEncoder.encode("customer123"))
                    .fullName("Nguyễn Văn Khách Hàng")
                    .phone("0900000001")
                    .address("Đà Nẵng, Việt Nam")
                    .enabled(true)
                    .roles(roles)
                    .build();
            User savedCustomer = userRepository.save(customer);

            // Tạo giỏ hàng đi kèm cho khách hàng
            Cart cart = Cart.builder()
                    .user(savedCustomer)
                    .build();
            cartRepository.save(cart);
            log.info("Đã tạo tài khoản Customer mẫu: customer / customer123");
        }

        // 5. Khởi tạo sản phẩm điện tử mẫu (Điện thoại, Laptop)
        if (productRepository.count() == 0) {
            Product phone = Product.builder()
                    .name("iPhone 15 Pro Max 256GB")
                    .description("Điện thoại thông minh cao cấp nhất của Apple năm 2024, khung titan siêu bền, camera zoom 5x.")
                    .price(new BigDecimal("34990000.00")) // 34,990,000 VND
                    .stockQuantity(50)
                    .category("Phone")
                    .imageUrl("https://images.technova.vn/products/iphone15promax.jpg")
                    .active(true)
                    .build();
            productRepository.save(phone);

            Product laptop = Product.builder()
                    .name("MacBook Pro 14 M3 Chip")
                    .description("Laptop Apple thế hệ mới trang bị chip M3 siêu mạnh, màn hình Liquid Retina XDR 120Hz.")
                    .price(new BigDecimal("39990000.00")) // 39,990,000 VND
                    .stockQuantity(20)
                    .category("Laptop")
                    .imageUrl("https://images.technova.vn/products/macbookprom3.jpg")
                    .active(true)
                    .build();
            productRepository.save(laptop);
            log.info("Đã tạo các sản phẩm điện thoại và laptop mẫu.");
        }

        log.info("--- Hoàn tất khởi tạo dữ liệu mẫu ---");
    }

    private Role getOrCreateRole(String roleName, String description) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name(roleName)
                            .description(description)
                            .build();
                    return roleRepository.save(role);
                });
    }
}
