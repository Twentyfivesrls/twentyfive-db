package com.twentyfive.twentyfivedb.thub.service;

import com.twentyfive.twentyfivedb.thub.repository.ThubProfileRepository;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.thubModels.ThubProfile;


import java.util.List;
import java.util.Optional;

@Service
public class ThubProfileService {
    private final ThubProfileRepository profileRepository;

    public ThubProfileService(ThubProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<ThubProfile> getAllProfile() {
        return profileRepository.findAll();
    }

    public ThubProfile saveThubProfile(ThubProfile thubProfile) {
        Optional<ThubProfile> opt = profileRepository.findByUsername(thubProfile.getUsername());

        if (opt.isEmpty()) {
            return profileRepository.save(thubProfile);
        }
        ThubProfile tP = opt.get();

        tP.setUsername(thubProfile.getUsername());
        tP.setTitle(thubProfile.getTitle());
        tP.setDescription(thubProfile.getDescription());
        tP.setHasProPic(thubProfile.getHasProPic());
        tP.setProPicUrl(thubProfile.getProPicUrl());
        tP.setLinks(thubProfile.getLinks());
        tP.setCustomTheme(thubProfile.getCustomTheme());

        return profileRepository.save(tP);

    }
    public ThubProfile getProfile(String username) {
        return profileRepository.findByUsername(username).orElse(null);
    }
}
