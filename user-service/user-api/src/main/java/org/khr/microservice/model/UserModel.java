package org.khr.microservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 @author KK
 @create 2025-11-27-09:35
 */
public class UserModel {

    public record LoginRequest(@Email String email, @NotBlank String password) {

    }

    public record UserVO(Long id, String username, String email, String token) {

    }
}
