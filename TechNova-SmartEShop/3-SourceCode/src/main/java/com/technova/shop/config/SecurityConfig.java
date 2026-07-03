package com.technova.shop.config;

import com.technova.shop.security.CustomAccessDeniedHandler;
import com.technova.shop.security.CustomAuthenticationEntryPoint;
import com.technova.shop.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình bảo mật chính của ứng dụng sử dụng Spring Security 6.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Sử dụng thuật toán BCrypt với độ mạnh mặc định là 10 (hoặc cấu hình lên 12 như SRS)
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF vì API hoạt động stateless bằng JWT
            .csrf(AbstractHttpConfigurer::disable)
            // Cấu hình custom 401 (chưa xác thực) và 403 (không đủ quyền) dạng JSON
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            // Không sử dụng Session phía Server
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Cấu hình phân quyền chi tiết các API
            .authorizeHttpRequests(auth -> auth
                // API public cho phép truy cập tự do (Xem sản phẩm, tìm kiếm, đăng ký, đăng nhập)
                .requestMatchers("/api/v1/public/**", "/api/v1/auth/**").permitAll()
                // Cho phép xem tài liệu API Swagger UI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // API Admin quản trị sản phẩm: Chỉ STAFF hoặc MANAGER
                .requestMatchers("/api/v1/admin/products/**").hasAnyRole("STAFF", "MANAGER")
                // API Admin quản trị người dùng & phân quyền: Chỉ MANAGER
                .requestMatchers("/api/v1/admin/users/**").hasRole("MANAGER")
                // API Admin thống kê doanh thu: Chỉ MANAGER
                .requestMatchers("/api/v1/admin/stats/**").hasRole("MANAGER")
                // API dành riêng cho khách hàng (Giỏ hàng, đặt hàng, xem lịch sử): CUSTOMER hoặc MANAGER
                .requestMatchers("/api/v1/customer/**").hasAnyRole("CUSTOMER", "MANAGER")
                // Các request khác đều cần đăng nhập
                .anyRequest().authenticated()
            );

        // Đăng ký bộ lọc JWT trước bộ lọc xác thực cơ bản
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
