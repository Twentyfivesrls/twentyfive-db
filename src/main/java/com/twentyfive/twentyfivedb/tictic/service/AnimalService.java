package com.twentyfive.twentyfivedb.tictic.service;

import com.twentyfive.twentyfivedb.tictic.repository.AnimalRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimal;

import java.util.List;

@Slf4j
@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public TTAnimal createAnimal(TTAnimal animal) {
        if (animal == null || animal.getName() == null || animal.getName().isEmpty() || animal.getSpecies() == null || animal.getSpecies().isEmpty()) {
            log.error("Assicurati di inserire tutti i campi obbligatori: nome e specie.");
        }
        assert animal != null;
        return animalRepository.save(animal);
    }

    public void deleteAnimal(String id) {
        if (id == null || id.isEmpty()) {
            log.error("id cannot be null or empty");
            return;
        }
        TTAnimal existingAnimal = animalRepository.findById(id).orElse(null);
        if (existingAnimal == null) {
            log.error("Animal with this microchip already exists: {}", id);
        } else {
            animalRepository.delete(existingAnimal);
            log.info("Animal with microchip {} deleted successfully", id);
        }
    }

    public void updateAnimal(String id, TTAnimal animal) {
        if (animal == null) {
            log.error("Animal is null");
            throw new IllegalArgumentException("Animal is null");
        }
        if (StringUtils.isBlank(id)) {
            log.error("Animal is null or empty");
            throw new IllegalArgumentException("Animal is null or empty");
        }

        TTAnimal animal1 = animalRepository.findById(id).orElse(null);

        if (animal1 == null) {
            this.createAnimal(animal);
        } else {
            animal1.setName(animal.getName());
            animal1.setSpecies(animal.getSpecies());
            animal1.setRace(animal.getRace());
            animal1.setGender(animal.getGender());
            animal1.setMicrochipCode(animal.getMicrochipCode());
            animal1.setBloodGroup(animal.getBloodGroup());
            animal1.setAllergies(animal.getAllergies());
            animal1.setPathologies(animal.getPathologies());
            animal1.setSterilization(animal.isSterilization());
            animal1.setAge(animal.getAge());
            animal1.setWeight(animal.getWeight());
            animal1.setAddress(animal.getAddress());
            animal1.setPhone(animal.getPhone());
            animal1.setEmail(animal.getEmail());
            animalRepository.save(animal1);
        }
    }

    public TTAnimal getAnimalById(String id) {
        return animalRepository.findById(id).orElse(null);
    }

    public List<TTAnimal> findAllByOwnerId(String ownerId) {
        return animalRepository.findAllByOwnerId(ownerId);
    }
}
