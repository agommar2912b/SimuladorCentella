package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@ToString
@Table(name = "team")

public class TeamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)

    private String name;
    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Column(nullable = false)
    private List<PlayerEntity> players = new ArrayList<>();

    @Column(nullable = false)
    private String profilePictureUrl;

    public TeamEntity(String name) {
        this.name = name;
    }


}

