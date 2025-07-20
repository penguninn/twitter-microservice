package com.david.profile_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String displayName;

    private boolean gender;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String bio;

    private String location;

    @Column(length = 2048)
    private String websiteUrl;

    @Column(length = 2048)
    private String profileImageUrl;

    @Column(length = 2048)
    private String bannerImageUrl;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate joinDate;

}