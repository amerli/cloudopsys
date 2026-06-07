package hu.agilexpert.core.service;

import hu.agilexpert.core.exception.DuplicateIdentifierException;
import hu.agilexpert.core.model.User;
import hu.agilexpert.core.model.UserAsset;
import hu.agilexpert.core.model.Visibility;
import hu.agilexpert.core.repository.UserAssetRepository;
import hu.agilexpert.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAssetService {

    private final UserAssetRepository userAssetRepository;
    private final UserRepository userRepository;

    public UserAsset addAsset(User user, UserAsset.AssetType type, String fileName, Visibility visibility) {
        if (userAssetRepository.findByFileName(fileName).isPresent()) {
            throw new DuplicateIdentifierException("Asset with fileName '" + fileName + "' already exists.");
        }
        UserAsset asset = userAssetRepository.save(new UserAsset(type, fileName, visibility));
        userRepository.insertUserAssetIfAbsent(user.getId(), asset.getId());
        return asset;
    }

    public void linkAssetToUser(User user, UserAsset asset) {
        userRepository.insertUserAssetIfAbsent(user.getId(), asset.getId());
    }

    public List<UserAsset> getAssetsByUser(User user) {
        return userAssetRepository.findByUser(user);
    }

    public List<UserAsset> getAssetsByUserAndType(User user, UserAsset.AssetType type) {
        return userAssetRepository.findByUserAndType(user, type);
    }

    public List<UserAsset> getPublicAssets() {
        return userAssetRepository.findByVisibility(Visibility.PUBLIC);
    }

    public Optional<UserAsset> findById(Long id) {
        return userAssetRepository.findById(id);
    }

    public Optional<UserAsset> findByFileName(String fileName) {
        return userAssetRepository.findByFileName(fileName);
    }

    public void makePublic(Long assetId) {
        userAssetRepository.findById(assetId).ifPresent(asset -> {
            asset.setVisibility(Visibility.PUBLIC);
            userAssetRepository.save(asset);
        });
    }

    public void removeAssetFromUser(User user, Long assetId) {
        userRepository.deleteUserAsset(user.getId(), assetId);
    }

    public void addIcon(User user, String fileName) {
        addAsset(user, UserAsset.AssetType.ICON, fileName, Visibility.PRIVATE);
    }

    public void addBackground(User user, String fileName) {
        addAsset(user, UserAsset.AssetType.BACKGROUND, fileName, Visibility.PRIVATE);
    }

    public void setActiveIcon(User user, Long assetId) {
        userAssetRepository.findById(assetId).ifPresent(asset -> {
            user.setActiveIcon(asset);
            userRepository.save(user);
        });
    }

    public void setActiveBackground(User user, Long assetId) {
        userAssetRepository.findById(assetId).ifPresent(asset -> {
            user.setActiveBackground(asset);
            userRepository.save(user);
        });
    }

    public List<UserAsset> getAvailableIconsForUser(User user) {
        List<UserAsset> available = new java.util.ArrayList<>(userAssetRepository.findByUser(user));
        userAssetRepository.findByVisibility(Visibility.PUBLIC).stream()
                .filter(a -> a.getType() == UserAsset.AssetType.ICON && available.stream().noneMatch(x -> x.getId().equals(a.getId())))
                .forEach(available::add);
        return available.stream().filter(a -> a.getType() == UserAsset.AssetType.ICON).toList();
    }

    public List<UserAsset> getAvailableBackgroundsForUser(User user) {
        List<UserAsset> available = new java.util.ArrayList<>(userAssetRepository.findByUser(user));
        userAssetRepository.findByVisibility(Visibility.PUBLIC).stream()
                .filter(a -> a.getType() == UserAsset.AssetType.BACKGROUND && available.stream().noneMatch(x -> x.getId().equals(a.getId())))
                .forEach(available::add);
        return available.stream().filter(a -> a.getType() == UserAsset.AssetType.BACKGROUND).toList();
    }

    public void removeAllAssetsForUser(User user) {
        userRepository.deleteAllUserAssets(user.getId());
    }
}
