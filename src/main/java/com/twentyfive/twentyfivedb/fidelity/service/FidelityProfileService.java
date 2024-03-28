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
        if (imageName == null) {
            throw new IllegalArgumentException("Image name cannot be null");
        }

        if (imageName.getId() != null) {
            throw new IllegalArgumentException("Image name already has an ID assigned");
        }

        if (profileRepository.existsById(imageName.getId())) {
            throw new IllegalArgumentException("Image name already exists in the repository");
        }

        return profileRepository.save(imageName);
    }

}
