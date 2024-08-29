package com.twentyfive.twentyfivedb.tictic.service;

import com.twentyfive.twentyfivedb.tictic.repository.AnimalRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimal;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    private final MongoTemplate mongoTemplate;

    private static final String USER_KEY = "ownerId";

    public AnimalService(AnimalRepository animalRepository, MongoTemplate mongoTemplate) {
        this.animalRepository = animalRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<TTAnimal> getAnimalFiltered(TTAnimal filterObject, String ownerId){
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where(USER_KEY).is(ownerId));
        criteriaList.addAll(parseOtherFilters(filterObject));
        return this.pageMethod(criteriaList);
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
        // Recupera gli animali senza ordinamento
        List<TTAnimal> animals = animalRepository.findByOwnerIdContainingIgnoreCase(ownerId, Sort.unsorted());

        // Ordinamento case-insensitive in Java
        return animals.stream()
                .sorted((a1, a2) -> {
                    String name1 = a1.getName().toLowerCase(Locale.ROOT);
                    String name2 = a2.getName().toLowerCase(Locale.ROOT);
                    return name1.compareTo(name2);
                })
                .collect(Collectors.toList());
    }

    public Set<AutoCompleteRes> filterSearch(String find){
        //Set<Contact> contacts = contactRepository.findAllByNameContainingIgnoreCase(find);
        //Set<Contact> contacts = contactRepository.findAllByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(find, find);
        Set<TTAnimal> animals = animalRepository.findAllByNameContainingIgnoreCase(find);
        Set<AutoCompleteRes> setCombinato = new HashSet<>();
        for (TTAnimal animal : animals) {
            AutoCompleteRes temp = new AutoCompleteRes(animal.getName());
            setCombinato.add(temp);
        }
        return setCombinato;
    }

    private List<Criteria> parseOtherFilters(TTAnimal filterObject){
        List<Criteria> criteriaList = new ArrayList<>();
        if(filterObject == null){
            return criteriaList;
        }
        if(StringUtils.isNotBlank(filterObject.getName())){
            criteriaList.add(Criteria.where("name").regex(filterObject.getName(), "i"));
        }
        return criteriaList;
    }

    private List<TTAnimal> pageMethod(List<Criteria> criteriaList) {
        Query query = new Query();

        if (CollectionUtils.isEmpty(criteriaList)) {
            log.info("criteria empty");
        } else {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return mongoTemplate.find(query, TTAnimal.class);
    }

    public List<TTAnimal> getAnimalsSorted(String ownerId, String campo, String ordine) {
        // Recupera gli animali senza ordinamento
        List<TTAnimal> animals = animalRepository.findByOwnerIdContainingIgnoreCase(ownerId, Sort.unsorted());

        // Determina la direzione dell'ordinamento
        Sort.Direction direction = "asc".equalsIgnoreCase(ordine) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Ordinamento case-insensitive in Java
        return animals.stream()
                .sorted((a1, a2) -> {
                    String value1 = getFieldValue(a1, campo).toLowerCase(Locale.ROOT);
                    String value2 = getFieldValue(a2, campo).toLowerCase(Locale.ROOT);
                    int comparison = value1.compareTo(value2);
                    return direction == Sort.Direction.ASC ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    private String getFieldValue(TTAnimal animal, String campo) {
        switch (campo.toLowerCase(Locale.ROOT)) {
            case "name":
                return animal.getName();
            // Aggiungi altri casi se hai altri campi da ordinare
            default:
                return "";
        }
    }

}
