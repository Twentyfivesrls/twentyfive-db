package com.twentyfive.twentyfivedb.tictic.service;

import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeGroupRepository;
import com.twentyfive.twentyfivedb.tictic.repository.AnimalRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import io.micrometer.common.util.StringUtils;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import twentyfive.twentyfiveadapter.dto.ticticDto.MinimalQrCode;
import twentyfive.twentyfiveadapter.dto.ticticDto.TTAnimalAndQrCode;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimal;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    private final MongoTemplate mongoTemplate;

    private static final String USER_KEY = "ownerId";
    private final QrCodeGroupRepository qrCodeGroupRepository;

    public AnimalService(AnimalRepository animalRepository,MongoTemplate mongoTemplate, QrCodeGroupRepository qrCodeGroupRepository) {
        this.animalRepository = animalRepository;
        this.mongoTemplate = mongoTemplate;
        this.qrCodeGroupRepository = qrCodeGroupRepository;
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
            animal1.setBirthDate(animal.getBirthDate());
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

    public Set<AutoCompleteRes> filterSearch(String find, String ownerId){
        Set<TTAnimal> animals = animalRepository.findByNameContainingIgnoreCaseAndOwnerIdContainingIgnoreCase(find, ownerId);
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

    public TTAnimalAndQrCode getAnimalAndAssociatedQr(String id) {
        TTAnimalAndQrCode animalAndQrCode = new TTAnimalAndQrCode();
        QrCodeGroup qrCodeGroup = qrCodeGroupRepository.findByAnimalId(id).orElseThrow(NotFoundException::new);
        TTAnimal animal = this.getAnimalById(id);
        MinimalQrCode qrCode = new MinimalQrCode();
        qrCode.setId(qrCodeGroup.getIdQrCode());
        qrCode.setImgUrl(qrCodeGroup.getLink());
        animalAndQrCode.setQrCode(qrCode);
        animalAndQrCode.setAnimal(animal);
        return animalAndQrCode;

    }

  public void deleteAnimalsWithoutQrCode() {
    // Trova tutti gli ID degli animali che hanno un QR Code associato
    List<String> associatedAnimalIds = mongoTemplate.findDistinct(
      new Query(), // Nessun filtro, prendiamo tutti i QR code
      "animalId", // Campo da estrarre
      QrCodeGroup.class, // Collezione di origine
      String.class // Tipo del campo estratto
    );

    // Crea un criterio per trovare gli animali non inclusi negli ID associati
    Query query = new Query();
    query.addCriteria(Criteria.where("_id").nin(associatedAnimalIds));

    // Elimina tutti gli animali che non sono associati a un QR code
    mongoTemplate.remove(query, TTAnimal.class);

    log.info("Eliminati tutti gli animali senza un QR code associato.");
  }
}
