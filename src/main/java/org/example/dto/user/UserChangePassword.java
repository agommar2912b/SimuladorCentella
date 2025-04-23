package org.example.dto.user;

import lombok.Data;

@Data
public class UserChangePassword {
    private String username;
    private String old_password;
    private String new_password;
}

