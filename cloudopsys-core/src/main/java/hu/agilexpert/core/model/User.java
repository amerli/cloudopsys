package hu.agilexpert.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    private Theme theme;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_assets",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "asset_id")
    )
    private Set<UserAsset> assets = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_icon_id")
    private UserAsset activeIcon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_background_id")
    private UserAsset activeBackground;

    public User(String name) {
        this.name = name;
    }

    public User(String name, String username) {
        this.name = name;
        this.username = username;
    }
}
