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
        // Verifica se esiste già un'immagine nel repository con lo stesso nome
        ProfileFidelity existingImage = profileRepository.findAllByImageName(imageName.getImageName());

        // Se esiste un'immagine con lo stesso nome, solleva un'eccezione o gestisci il caso di errore
        if (existingImage != null) {
            throw new IllegalArgumentException("An image with the same name already exists");
            // oppure puoi gestire il caso di errore in un altro modo, ad esempio restituendo null
            // oppure loggando un messaggio di errore e restituendo null
        }

        // Se non esiste un'immagine con lo stesso nome, procedi con il salvataggio
        return profileRepository.save(imageName);
    }

    public ProfileFidelity getImageName(String ownerId){
        return profileRepository.findByOwnerId(ownerId);
    }

}
