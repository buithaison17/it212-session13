package com.technova.shop.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO trả về sau khi xác thực thành công.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    
    private String username;
    private List<String> roles;
    private Long expiresIn; // Thời gian sống của token (ms)
}
