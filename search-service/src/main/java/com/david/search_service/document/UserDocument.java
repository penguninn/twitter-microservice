package com.david.search_service.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDate;

@Data
@Document(indexName = "users")
public class UserDocument {

    @Id
    private String id;

    private String userId;

    private String username;

    private String email;

    private String displayName;

    private boolean gender;

    private String bio;

    private String location;

    private String websiteUrl;

    private String profileImageUrl;

    private String bannerImageUrl;

    private LocalDate joinDate;

    private LocalDate dateOfBirth;
}
