package com.twentyfive.twentyfivedb.tictic.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.fidelity.exceptions.NotFoundException;
import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeGroupRepository;
import com.twentyfive.twentyfivedb.tictic.repository.CustomerRepository;
import com.twentyfive.twentyfivedb.tictic.repository.ShopperRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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


    public ShopperService(ShopperRepository shopperRepository, CustomerRepository customerRepository, QrCodeGroupRepository qrCodeGroupRepository, AnimalService animalService) {
        this.shopperRepository = shopperRepository;
        this.customerRepository = customerRepository;
        this.qrCodeGroupRepository = qrCodeGroupRepository;
        this.animalService = animalService;
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

    public Page<TicTicCustomer> getShopperCustomers(String ownerId, int page, int size, String sortColumn, String sortDirection) {
        Pageable p = Utility.makePageableObj(sortDirection, sortColumn, page, size);
        return this.customerRepository.findAllByOwnerId(ownerId, p);
    }

    public TicTicShopper saveShopper(TicTicShopper shopper) {
        return this.shopperRepository.save(shopper);
    }

    public TicTicCustomer saveCustomer(TicTicCustomer customer) {
        String ownerId = customer.getOwnerId();
        if (customerRepository.existsByEmailAndOwnerId(customer.getEmail(), ownerId)) {
            throw new RuntimeException("Customer already exists");
        } else {
            updateShopperCounter(ownerId, "customerCount", "1");
            return this.customerRepository.save(customer);
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

    public Page<TicTicCustomer> getShopperCustomersWithEmail(String ownerId, String email, int page, int size, String sortColumn, String sortDirection) {
      Pageable p = Utility.makePageableObj(sortDirection, sortColumn, page, size);
      return this.customerRepository.findAllByOwnerIdAndEmailContainingIgnoreCase(ownerId, email, p);
    }

    public Set<AutoCompleteRes> filterAutocompleteCustomer(String find, String ownerId) {
        Set<TicTicCustomer> customers = customerRepository.findByOwnerIdAndAnyMatchingFields(ownerId, find);

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
        qrCodeGroup.setAssociationDate(LocalDateTime.now());
        qrCodeGroupRepository.save(qrCodeGroup);
        updateShopperCounter(ownerId, "soldPlates", "1");
        updateShopperCounter(ownerId, "remainingPlates", "-1");

    }


    public String checkCustomerAndQRCodeExists(String ownerId) {
        boolean customerExists = customerRepository.existsByOwnerId(ownerId);
        boolean qrCodeExists = qrCodeGroupRepository.existsByOwnerId(ownerId);

        if (!customerExists && !qrCodeExists) {
            return "Nessun cliente e nessun QR code trovato per lo shopper specificato.";
        } else if (!customerExists) {
            return "Nessun cliente trovato per lo shopper specificato.";
        } else if (!qrCodeExists) {
            return "Nessun QR code trovato per lo shopper specificato.";
        }

        return "Cliente e QR code trovati.";
    }


  public Page<QrCodeGroup> getQrCodes(String ownerId, int page, int size, String sortColumn, String sortDirection) {
    Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
    Pageable pageable = PageRequest.of(page, size, sort);
    return qrCodeGroupRepository.findByOwnerId(ownerId, pageable);
  }

  public Page<QrCodeGroup> getQrCodesByCustomer(String ownerId, String customerId, int page, int size, String sortColumn, String sortDirection) {
    Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
    Pageable pageable = PageRequest.of(page, size, sort);
    return qrCodeGroupRepository.findByOwnerIdAndCustomerId(ownerId, customerId, pageable);
  }

    public List<QrCodeGroup> getQrCodesForShopper(String usernameShopper) {
        return qrCodeGroupRepository.findAllByUsername(usernameShopper);
    }

    public List<QrCodeGroup> getUnassignedQrCodes(String ownerId, String name) {
      // Recupera tutti i QR codes per l'ownerId
     return qrCodeGroupRepository.findAllByOwnerIdAndNameQrCodeContainsIgnoreCaseAndCustomerIdNull(ownerId, name);

    }

}


