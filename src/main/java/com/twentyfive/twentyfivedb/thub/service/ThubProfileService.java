package com.twentyfive.twentyfivedb.thub.service;

import com.twentyfive.twentyfivedb.thub.repository.ThubProfileRepository;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.thubModels.ThubProfile;


import java.util.List;

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
        ThubProfile tP = profileRepository.findByUsername(thubProfile.getUsername()).orElse(null);

        if(tP != null) {
            tP.setUsername(thubProfile.getUsername());
            tP.setTitle(thubProfile.getTitle());
            tP.setDescription(thubProfile.getDescription());
            tP.setHasProPic(thubProfile.getHasProPic());
            tP.setProPicUrl(thubProfile.getProPicUrl());
            tP.setLinks(thubProfile.getLinks());
            tP.setTemplate(thubProfile.getTemplate());
            tP.setCustomTheme(thubProfile.getCustomTheme());

            return profileRepository.save(tP);
        }

        return profileRepository.save(thubProfile);
    }
    public ThubProfile getProfile(String username) {
        return profileRepository.findByUsername(username).orElse(null);
    }
}
