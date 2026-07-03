package com.technova.shop.security;

import com.technova.shop.entity.User;
import com.technova.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai UserDetailsService để tải thông tin tài khoản phục vụ quá trình xác thực.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Sử dụng findWithRolesByUsername với @EntityGraph để tránh N+1 Query khi load user kèm roles
        User user = userRepository.findWithRolesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username));

        return UserDetailsImpl.build(user);
    }
}
