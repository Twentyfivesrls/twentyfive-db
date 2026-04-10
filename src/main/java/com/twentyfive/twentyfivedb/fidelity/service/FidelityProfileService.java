package com.twentyfive.twentyfivedb.fidelity.service;

import com.twentyfive.twentyfivedb.fidelity.repository.FidelityProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.fidelityModels.ProfileFidelity;

@Service
@Slf4j
public class FidelityProfileService {
    private final FidelityProfileRepository profileRepository;

    public FidelityProfileService(FidelityProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public ProfileFidelity createImageName(ProfileFidelity imageName) {
        ProfileFidelity existingImage = profileRepository.findAllByOwnerId(imageName.getOwnerId());

        if (existingImage != null) {
            existingImage.setImageName(imageName.getImageName());
            return profileRepository.save(existingImage);
        }

        return profileRepository.save(imageName);
    }

    public ProfileFidelity getImageName(String ownerId){
        return profileRepository.findByOwnerId(ownerId);
    }

    public void deleteImageName(String ownerId){
        profileRepository.deleteByOwnerId(ownerId);
    }
}
