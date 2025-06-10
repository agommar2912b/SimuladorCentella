package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@ToString
@Table(name = "user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String profilePictureUrl;

    @Column(nullable = false)
    private String securityQuestion;

    @Column(nullable = false)
    private String securityAnswer;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Column(nullable = false)
    private List<TeamEntity> teams;


    public UserEntity(String name, String password) {
        this.name = name;
        this.password = password;
    }
}

