package edu.dosw.parcial.controller.dtos.response;

import lombok.*;

@Data @Builder
public class AuthResponse {
    private String token;
    private Long expiresIn;
    private String userId;
    private String role;
}