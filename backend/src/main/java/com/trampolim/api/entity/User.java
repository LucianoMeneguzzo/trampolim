package com.trampolim.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "senha", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private UserType type;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "github", length = 150)
    private String github;

    @Column(name = "linkedin", length = 150)
    private String linkedin;

    @Column(name = "titulo_profissional", length = 100)
    private String professionalTitle;

}