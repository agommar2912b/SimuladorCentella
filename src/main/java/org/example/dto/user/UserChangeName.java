package org.example.dto.user;

import lombok.Data;

@Data
public class UserChangeName {
    private String NewUsername;
    private String OldUsername;
}

