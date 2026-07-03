package com.technova.shop.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO đăng ký tài khoản.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Username không được trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Email không được trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu không được trống")
    @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự trở lên")
    private String password;

    @NotBlank(message = "Họ và tên không được trống")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
    private String fullName;

    private String phone;
    private String address;
}
