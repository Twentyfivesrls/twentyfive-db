package com.twentyfive.twentyfivedb.tictic.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.fidelity.exceptions.NotFoundException;
import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeGroupRepository;
import com.twentyfive.twentyfivedb.tictic.repository.CustomerRepository;
import com.twentyfive.twentyfivedb.tictic.repository.ShopperRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimal;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicCustomer;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicShopper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShopperService {

    private final ShopperRepository shopperRepository;
    private final CustomerRepository customerRepository;
    private final QrCodeGroupRepository qrCodeGroupRepository;
    private final AnimalService animalService;

    private final MongoTemplate mongoTemplate;


    public ShopperService(ShopperRepository shopperRepository, CustomerRepository customerRepository, QrCodeGroupRepository qrCodeGroupRepository, AnimalService animalService, MongoTemplate mongoTemplate) {
        this.shopperRepository = shopperRepository;
        this.customerRepository = customerRepository;
        this.qrCodeGroupRepository = qrCodeGroupRepository;
        this.animalService = animalService;
        this.mongoTemplate = mongoTemplate;
    }

    public TicTicShopper getShopperCounters(String ownerId) {
        Optional<TicTicShopper> opt = this.shopperRepository.findByOwnerId(ownerId);
        //TODO when shopper account is created, create a new TicTicShopper entity in order to return it
        return opt.orElse(null);
    }

    public void updateShopperCounter(String ownerId, String type, String c) {
        //Last param is the counter value in string format because we can manage also negative values (decrement)
        int counter = Integer.parseInt(c);
        Optional<TicTicShopper> opt = this.shopperRepository.findByOwnerId(ownerId);
        TicTicShopper shopper = opt.orElseThrow(() -> new NotFoundException("Shopper not found"));

        switch (type) {
            case "customerCount":
                shopper.setCustomerCount(shopper.getCustomerCount() + counter);
                break;
            case "orderedPlates":
                shopper.setOrderedPlates(shopper.getOrderedPlates() + counter);
                break;
            case "soldPlates":
                shopper.setSoldPlates(shopper.getSoldPlates() + counter);
                break;
            case "remainingPlates":
                shopper.setRemainingPlates(shopper.getRemainingPlates() + counter);
                break;
            default:
                throw new RuntimeException("Invalid type");
        }

        this.shopperRepository.save(shopper);
    }

    public Page<TicTicCustomer> getAllCustomers(int page, int size, String sortColumn, String sortDirection) {
      Pageable pageable = Utility.makePageableObj(sortDirection, sortColumn, page, size);
      return this.customerRepository.findAll(pageable);
    }

  public Page<TicTicCustomer> getCustomersByOwner(String ownerId, int page, int size, String sortColumn, String sortDirection) {
    Pageable pageable = Utility.makePageableObj(sortDirection, sortColumn, page, size);
    return this.customerRepository.findByOwnerId(ownerId, pageable);
  }

    public TicTicShopper saveShopper(TicTicShopper shopper) {
        return this.shopperRepository.save(shopper);
    }

    public TicTicCustomer saveCustomer(TicTicCustomer customer) {
        String ownerId = customer.getOwnerId();
        if (customerRepository.existsByEmail(customer.getEmail(), ownerId)) {
            throw new RuntimeException("Customer already exists");
        } else {
            updateShopperCounter(ownerId, "customerCount", "1");
            return this.customerRepository.save(customer);
        }
    }

    public TTAnimal getAnimalByIdQrCode(String idQrCode){
        Optional<QrCodeGroup> optQrCode = qrCodeGroupRepository.findByIdQrCode(idQrCode);
        if (optQrCode.isPresent() && optQrCode.get().getAnimalId() != null) {
            QrCodeGroup qrCodeGroup = optQrCode.get();
            return animalService.getAnimalById(qrCodeGroup.getAnimalId());
        } else {
            throw new NotFoundException("Animal not found");
        }
    }

    public void deleteShopper(String shopperId) {
        this.shopperRepository.deleteById(shopperId);
    }

    public void deleteCustomer(String customerId, String ownerId) {
        //TODO check if the customer has associated QR codes and decide which policy to apply
        this.customerRepository.deleteByIdAndOwnerId(customerId, ownerId);
        updateShopperCounter(ownerId, "customerCount", "-1");
    }

    public TicTicCustomer getCustomer(String customerId) {
        Optional<TicTicCustomer> opt = this.customerRepository.findByEmail(customerId);
        return opt.orElse(null);
    }

  public Page<TicTicCustomer> getShopperCustomersWithEmail(String email, int page, int size, String sortColumn, String sortDirection) {
    Pageable pageable = Utility.makePageableObj(sortDirection, sortColumn, page, size);
    return this.customerRepository.findAllByEmailContainingIgnoreCase(email, pageable);
  }

  public Set<AutoCompleteRes> filterAutocompleteCustomer(String find) {
    Set<TicTicCustomer> customers = customerRepository.findByAnyMatchingFields(find);

    return customers.stream().map(c -> {
      AutoCompleteRes res = new AutoCompleteRes();
      res.setValue(c.getEmail());
      return res;
    }).collect(Collectors.toCollection(LinkedHashSet::new));
  }
    public void associateQRCodeWithCustomer(String ownerId, String qrCodeId, String customerId, TTAnimal animal) {
        Optional<TicTicCustomer> customerOpt = customerRepository.findByEmail(customerId);
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Customer not found");
        }

        animal.setOwnerId(customerId);
        TTAnimal animalSaved = animalService.createAnimal(animal);

        String animalId = animalSaved.getId();

        Optional<QrCodeGroup> opt = qrCodeGroupRepository.findByOwnerIdAndNameQrCode(ownerId, qrCodeId);
        if (opt.isEmpty()) {
            throw new RuntimeException("QR code not found");
        }
        QrCodeGroup qrCodeGroup = opt.get();
        qrCodeGroup.setCustomerId(customerId);
        qrCodeGroup.setAnimalId(animalId);
        qrCodeGroup.setAssociationDate(LocalDateTime.now().plusHours(1));
        qrCodeGroupRepository.save(qrCodeGroup);
        updateShopperCounter(ownerId, "soldPlates", "1");
        updateShopperCounter(ownerId, "remainingPlates", "-1");

    }


  public boolean checkCustomerAndQRCodeExists(String ownerId) {
    //boolean customerExists = customerRepository.existsByOwnerId(ownerId);
    boolean qrCodeExists = qrCodeGroupRepository.existsByOwnerId(ownerId);

    // Ritorna true solo se entrambi esistono
    return qrCodeExists;
  }

  public Page<QrCodeGroup> getQrCodes(String ownerId, int page, int size, String sortColumn, String sortDirection) {
    System.out.println(sortDirection);
    Sort.Direction direction = Sort.Direction.fromString(sortDirection);

    List<AggregationOperation> operations = new ArrayList<>();
    operations.add(Aggregation.match(Criteria.where("ownerId").is(ownerId)));

    if ("idQrCode".equalsIgnoreCase(sortColumn)) {
      operations.add(
        Aggregation.project("idQrCode", "animalId", "nameQrCode", "groupName", "link", "type", "username",
            "isActivated", "ownerId", "customerId", "associationDate")
          .andExpression("toDouble(idQrCode)").as("idNumeric")
      );
      operations.add(Aggregation.sort(Sort.by(direction, "idNumeric")));
    } else {
      operations.add(Aggregation.sort(Sort.by(direction, sortColumn)));
    }

    Aggregation aggregation = Aggregation.newAggregation(operations);
    AggregationResults<QrCodeGroup> results = mongoTemplate.aggregate(aggregation, "tictic_qrcode_group", QrCodeGroup.class);

    List<QrCodeGroup> mappedResults = results.getMappedResults();
    Pageable pageable = PageRequest.of(page, size);

    return Utility.convertListToPage(mappedResults, pageable);
  }

  public Page<QrCodeGroup> getQrCodesBySearchString(String ownerId, String searchString, int page, int size, String sortColumn, String sortDirection) {
    Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
    Pageable pageable = PageRequest.of(page, size, sort);
    return qrCodeGroupRepository.findByOwnerIdAndSearchString(ownerId, searchString, pageable);
  }


    public List<QrCodeGroup> getQrCodesForCustomer(String customerId) {
        return qrCodeGroupRepository.findAllByCustomerId(customerId);
    }

    public List<QrCodeGroup> getUnassignedQrCodes(String ownerId, String name) {
      // Recupera tutti i QR codes per l'ownerId
     return qrCodeGroupRepository.findAllByOwnerIdAndNameQrCodeContainsIgnoreCaseAndCustomerIdNull(ownerId, name);

    }


  public Page<QrCodeGroup> getAssociatedQrCodes(String ownerId, int page, int size, String sortColumn, String sortDirection) {
    Sort.Direction direction = Sort.Direction.fromString(sortDirection);
    List<AggregationOperation> operations = new ArrayList<>();
    // Filtra per ownerId e solo QR Code associati (customerId != null)
    operations.add(Aggregation.match(Criteria.where("ownerId").is(ownerId)
      .and("customerId").ne(null)));

    if ("idQrCode".equalsIgnoreCase(sortColumn)) {
      operations.add(
        Aggregation.project("idQrCode", "animalId", "nameQrCode", "groupName", "link", "type", "username",
            "isActivated", "ownerId", "customerId", "associationDate")
          .and(ConvertOperators.valueOf("idQrCode").convertToDouble()).as("idNumeric")
      );
      operations.add(Aggregation.sort(Sort.by(direction, "idNumeric")));
    } else {
      operations.add(Aggregation.sort(Sort.by(direction, sortColumn)));
    }

    Aggregation aggregation = Aggregation.newAggregation(operations);
    AggregationResults<QrCodeGroup> results = mongoTemplate.aggregate(aggregation, "tictic_qrcode_group", QrCodeGroup.class);
    List<QrCodeGroup> mappedResults = results.getMappedResults();
    Pageable pageable = PageRequest.of(page, size);
    return Utility.convertListToPage(mappedResults, pageable);
  }

  public Page<QrCodeGroup> getNonAssociatedQrCodes(String ownerId, int page, int size, String sortColumn, String sortDirection) {
    Sort.Direction direction = Sort.Direction.fromString(sortDirection);
    List<AggregationOperation> operations = new ArrayList<>();
    // Filtra per ownerId e solo QR Code non associati (customerId == null)
    operations.add(Aggregation.match(Criteria.where("ownerId").is(ownerId)
      .and("customerId").is(null)));

    if ("idQrCode".equalsIgnoreCase(sortColumn)) {
      operations.add(
        Aggregation.project("idQrCode", "animalId", "nameQrCode", "groupName", "link", "type", "username",
            "isActivated", "ownerId", "customerId", "associationDate")
          .and(ConvertOperators.valueOf("idQrCode").convertToDouble()).as("idNumeric")
      );
      operations.add(Aggregation.sort(Sort.by(direction, "idNumeric")));
    } else {
      operations.add(Aggregation.sort(Sort.by(direction, sortColumn)));
    }

    Aggregation aggregation = Aggregation.newAggregation(operations);
    AggregationResults<QrCodeGroup> results = mongoTemplate.aggregate(aggregation, "tictic_qrcode_group", QrCodeGroup.class);
    List<QrCodeGroup> mappedResults = results.getMappedResults();
    Pageable pageable = PageRequest.of(page, size);
    return Utility.convertListToPage(mappedResults, pageable);
  }

}


