package org.example.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserChangePassword {
    private String username;
    private String securityAnswer;
    private String new_password;
}

