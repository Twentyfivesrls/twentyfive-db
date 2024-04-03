package com.twentyfive.twentyfivedb.tictic.service;

import com.twentyfive.twentyfivedb.tictic.repository.AnimalOwnerRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimalOwner;

@Slf4j
@Service
public class AnimalOwnerService {

    private final AnimalOwnerRepository ownerRepository;

    public AnimalOwnerService(AnimalOwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public TTAnimalOwner createOwner(TTAnimalOwner animalOwner) {
        if (animalOwner == null || animalOwner.getEmail() == null || animalOwner.getEmail().isEmpty()) {
            log.error("Animal owner or email cannot be null or empty");
            return null;
        }
        TTAnimalOwner existingOwner = ownerRepository.findByEmail(animalOwner.getEmail());
        if (existingOwner != null) {
            log.error("User already exists with email: {}", animalOwner.getEmail());
            return null;
        } else {
            return ownerRepository.save(animalOwner);
        }
    }

    public void deleteOwner(String email) {
        if (email == null || email.isEmpty()) {
            log.error("Email cannot be null or empty");
            return;
        }
        TTAnimalOwner existingOwner = ownerRepository.findByEmail(email);
        if (existingOwner == null) {
            log.error("User does not exist with email: {}", email);
            return;
        } else {
            ownerRepository.delete(existingOwner);
            log.info("User with email {} deleted successfully", email);
        }
    }

    public void updateOwner(String email, TTAnimalOwner animalOwner) {
        if (animalOwner == null) {
            log.error("Animal owner is null");
            throw new IllegalArgumentException("Animal owner is null");
        }
        if (StringUtils.isBlank(email)) {
            log.error("Animal owner is null or empty");
            throw new IllegalArgumentException("Animal owner is null or empty");
        }

        TTAnimalOwner animalOwner1 = ownerRepository.findByEmail(email);

        if (animalOwner1 == null) {
            this.createOwner(animalOwner);
        } else {
            animalOwner1.setName(animalOwner.getName());
            animalOwner1.setSurname(animalOwner.getSurname());
            animalOwner1.setPhone(animalOwner.getPhone());
            animalOwner1.setEmail(animalOwner.getEmail());
            animalOwner1.setAddress(animalOwner.getAddress());
            ownerRepository.save(animalOwner1);
        }
    }
}
