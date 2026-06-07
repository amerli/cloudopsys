package hu.agilexpert.core.repository;

import hu.agilexpert.core.model.User;
import hu.agilexpert.core.model.UserAsset;
import hu.agilexpert.core.model.Visibility;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserAssetRepository extends CrudRepository<UserAsset, Long> {
    List<UserAsset> findByVisibility(Visibility visibility);
    List<UserAsset> findByTypeAndVisibility(UserAsset.AssetType type, Visibility visibility);
    Optional<UserAsset> findByFileName(String fileName);
    @Query("SELECT a FROM UserAsset a JOIN a.users u WHERE u = :user")
    List<UserAsset> findByUser(User user);
    @Query("SELECT a FROM UserAsset a JOIN a.users u WHERE u = :user AND a.type = :type")
    List<UserAsset> findByUserAndType(User user, UserAsset.AssetType type);
}
