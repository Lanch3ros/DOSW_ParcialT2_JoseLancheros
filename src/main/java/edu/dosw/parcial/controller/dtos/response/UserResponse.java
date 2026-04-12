package edu.dosw.parcial.controller.dtos.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private String id;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}