package com.twentyfive.twentyfivedb.tictic.service;

import com.twentyfive.twentyfivedb.tictic.repository.AnimalRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimal;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public TTAnimal createAnimal(TTAnimal animal) {
        if (animal == null || animal.getMicrochipCode() == null || animal.getAnimalOwnerId() == null || animal.getAnimalOwnerId().isEmpty()) {
            log.error("Animal or animal owner cannot be null");
            return null;
        }

        TTAnimal existingAnimal = animalRepository.findByMicrochipCode(animal.getMicrochipCode());
        if (existingAnimal != null) {
            log.error("Animal with this microchip already exists: {}", animal.getMicrochipCode());
            return null;
        } else {
            return animalRepository.save(animal);
        }
    }

    public void deleteAnimal(String microchipCode) {
        if (microchipCode == null || microchipCode.isEmpty()) {
            log.error("Microchip cannot be null or empty");
            return;
        }
        TTAnimal existingAnimal = animalRepository.findByMicrochipCode(microchipCode);
        if (existingAnimal == null) {
            log.error("Animal with this microchip already exists: {}", microchipCode);
            return;
        } else {
            animalRepository.delete(existingAnimal);
            log.info("Animal with microchip {} deleted successfully", microchipCode);
        }
    }

    public void updateAnimal(String microchipCode, TTAnimal animal){
        if(animal == null){
            log.error("Animal is null");
            throw new IllegalArgumentException("Animal is null");
        }
        if(StringUtils.isBlank(microchipCode)){
            log.error("Animal is null or empty");
            throw new IllegalArgumentException("Animal is null or empty");
        }

        TTAnimal animal1 = animalRepository.findByMicrochipCode(microchipCode);

        if(animal1 == null){
            this.createAnimal(animal);
        }else{
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
        }
    }

    public TTAnimal getAnimalById(String id){
        return animalRepository.findById(id).orElse(null);
    }

    public List<TTAnimal> findAllByAnimalOwnerId(String animalOwnerId){
        return animalRepository.findAllByAnimalOwnerId(animalOwnerId);
    }
}
