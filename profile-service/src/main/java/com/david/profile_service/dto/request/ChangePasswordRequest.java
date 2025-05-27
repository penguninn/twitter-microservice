package com.david.profile_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest implements Serializable {

//    @NotBlank(message = "Username is required")
//    private String username;

    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @Min(value = 8, message = "Password must be at least 8 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    public boolean isValid() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
