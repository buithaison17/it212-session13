package com.technova.shop.service;

import com.technova.shop.dto.auth.AuthResponse;
import com.technova.shop.dto.auth.LoginRequest;
import com.technova.shop.dto.auth.RegisterRequest;
import com.technova.shop.entity.Cart;
import com.technova.shop.entity.Role;
import com.technova.shop.entity.User;
import com.technova.shop.exception.BadRequestException;
import com.technova.shop.exception.ResourceNotFoundException;
import com.technova.shop.repository.CartRepository;
import com.technova.shop.repository.RoleRepository;
import com.technova.shop.repository.UserRepository;
import com.technova.shop.security.JwtTokenProvider;
import com.technova.shop.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Xử lý các logic nghiệp vụ liên quan đến Đăng ký & Xác thực người dùng.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${technova.jwt.expiration}")
    private long jwtExpirationInMs;

    /**
     * Đăng ký tài khoản người dùng mới với vai trò mặc định là CUSTOMER.
     * Tự động khởi tạo giỏ hàng trống cho khách hàng.
     */
    @Transactional
    public String registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại trong hệ thống.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng bởi tài khoản khác.");
        }

        // Tìm role CUSTOMER mặc định
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi hệ thống: Vai trò ROLE_CUSTOMER chưa được khởi tạo."));

        // Tạo đối tượng User mới và mã hóa mật khẩu bằng BCrypt
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .enabled(true)
                .roles(new HashSet<>(Collections.singletonList(customerRole)))
                .build();

        User savedUser = userRepository.save(user);

        // Khởi tạo giỏ hàng trống cho người dùng mới này
        Cart cart = Cart.builder()
                .user(savedUser)
                .build();
        cartRepository.save(cart);

        return "Đăng ký tài khoản thành công!";
    }

    /**
     * Xác thực thông tin đăng nhập và sinh JWT Token.
     */
    public AuthResponse login(LoginRequest request) {
        // Thực hiện xác thực dựa trên username và password gửi lên
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Lưu thông tin xác thực vào context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sinh JWT token
        String jwt = jwtTokenProvider.generateToken(authentication);

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .token(jwt)
                .username(userPrincipal.getUsername())
                .roles(roles)
                .expiresIn(jwtExpirationInMs)
                .build();
    }
}
