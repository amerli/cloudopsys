package hu.agilexpert.core.repository;

import hu.agilexpert.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Modifying
    @Query(value = "INSERT OR IGNORE INTO user_assets (user_id, asset_id) VALUES (:userId, :assetId)", nativeQuery = true)
    void insertUserAssetIfAbsent(@Param("userId") Long userId, @Param("assetId") Long assetId);

    @Modifying
    @Query(value = "DELETE FROM user_assets WHERE user_id = :userId AND asset_id = :assetId", nativeQuery = true)
    void deleteUserAsset(@Param("userId") Long userId, @Param("assetId") Long assetId);

    @Modifying
    @Query(value = "DELETE FROM user_assets WHERE user_id = :userId", nativeQuery = true)
    void deleteAllUserAssets(@Param("userId") Long userId);
}
