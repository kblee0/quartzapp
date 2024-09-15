package com.home.quartzapp.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "T_LOGIN_USER")
public class LoginUser {
    @Id
    @Size(max = 64)
    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Size(max = 255)
    @NotNull
    @Column(name = "LOGIN_ID", nullable = false)
    private String loginId;

    @Size(max = 255)
    @Column(name = "PASSWORD")
    private String password;

    @Size(max = 255)
    @Column(name = "NAME")
    private String name;

    @Size(max = 255)
    @Column(name = "EMAIL")
    private String email;

    @Size(max = 255)
    @Column(name = "ROLES")
    private String roles;

    @Size(max = 1024)
    @Column(name = "REFRESH_TOKEN", length = 1024)
    private String refreshToken;

}