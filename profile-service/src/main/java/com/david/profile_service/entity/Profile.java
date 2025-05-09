package com.david.profile_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "profiles")
public class Profile {

    @Id
    private Long id;
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
