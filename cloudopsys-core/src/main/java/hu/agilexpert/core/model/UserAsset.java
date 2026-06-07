package hu.agilexpert.core.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = "users")
@NoArgsConstructor
@Entity
@Table(name = "assets")
public class UserAsset {

    public enum AssetType { ICON, BACKGROUND }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AssetType type;

    @Column(unique = true)
    private String fileName;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @ManyToMany(mappedBy = "assets")
    private Set<User> users = new HashSet<>();

    public UserAsset(AssetType type, String fileName, Visibility visibility) {
        this.type = type;
        this.fileName = fileName;
        this.visibility = visibility;
    }
}
