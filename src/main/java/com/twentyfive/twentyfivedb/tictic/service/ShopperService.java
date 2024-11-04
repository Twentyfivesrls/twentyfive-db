package com.twentyfive.twentyfivedb.tictic.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.fidelity.exceptions.NotFoundException;
import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeGroupRepository;
import com.twentyfive.twentyfivedb.tictic.repository.AnimalRepository;
import com.twentyfive.twentyfivedb.tictic.repository.CustomerRepository;
import com.twentyfive.twentyfivedb.tictic.repository.ShopperRepository;
import com.twentyfive.twentyfivedb.tictic.repository.TicTicCodeCustomerAssociationRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimal;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicCustomer;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicQrCodeCustomerAssociations;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicShopper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ShopperService {

    private final ShopperRepository shopperRepository;
    private final CustomerRepository customerRepository;
    private final TicTicCodeCustomerAssociationRepository codeCustomerAssociationRepository;
    private final QrCodeGroupRepository qrCodeGroupRepository;
    private final AnimalRepository animalRepository;


    public ShopperService(ShopperRepository shopperRepository, CustomerRepository customerRepository, TicTicCodeCustomerAssociationRepository codeCustomerAssociationRepository, QrCodeGroupRepository qrCodeGroupRepository, AnimalRepository animalRepository) {
        this.shopperRepository = shopperRepository;
        this.customerRepository = customerRepository;
        this.codeCustomerAssociationRepository = codeCustomerAssociationRepository;
        this.qrCodeGroupRepository = qrCodeGroupRepository;
        this.animalRepository = animalRepository;
    }

    public TicTicShopper getShopperCounters(String ownerId) {
        Optional<TicTicShopper> opt = this.shopperRepository.findByOwnerId(ownerId);
        //TODO when shopper account is created, create a new TicTicShopper entity in order to return it
        return opt.orElse(null);
    }

    public Page<TicTicCustomer> getShopperCustomers(String ownerId, int page, int size, String sortColumn, String sortDirection) {
        Pageable p = Utility.makePageableObj(sortDirection, sortColumn, page, size);
        return this.customerRepository.findAllByOwnerId(ownerId, p);
    }

    public TicTicShopper saveShopper(TicTicShopper shopper) {
        return this.shopperRepository.save(shopper);
    }

    public TicTicCustomer saveCustomer(TicTicCustomer customer) {
        return this.customerRepository.save(customer);
    }

    public void deleteShopper(String shopperId) {
        this.shopperRepository.deleteById(shopperId);
    }

    public void deleteCustomer(String customerId) {
        this.customerRepository.deleteById(customerId);
    }

    public TicTicCustomer getCustomer(String customerId) {
        Optional<TicTicCustomer> opt = this.customerRepository.findById(customerId);
        return opt.orElse(null);
    }


    public Set<AutoCompleteRes> filterAutocompleteCustomer(String find, String ownerId) {
        Set<TicTicCustomer> customers = customerRepository.findByOwnerIdAndAnyMatchingFields(ownerId, find);

        return customers.stream().map(c -> {
            AutoCompleteRes res = new AutoCompleteRes();
            res.setValue(c.getEmail());
            return res;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public TicTicQrCodeCustomerAssociations associateQRCodeWithCustomer(String ownerId, String qrCodeId, String customerId) {
        Optional<TicTicCustomer> customerOpt = customerRepository.findByEmail(customerId);
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Customer not found");
        }

        Optional<TTAnimal> animalOpt = animalRepository.findByEmail(customerOpt.get().getEmail());
        if (animalOpt.isEmpty()) {
            throw new RuntimeException("Animal associated with customer not found");
        }


        TicTicQrCodeCustomerAssociations association = new TicTicQrCodeCustomerAssociations();
        association.setQrCodeId(qrCodeId);
        association.setCustomerId(customerOpt.get().getId());
        association.setCustomerEmail(customerOpt.get().getEmail());
        association.setOwnerId(ownerId);
        association.setStatus("ATTIVO");
        association.setAssociationDate(LocalDateTime.now());
        association.setAnimalName(animalOpt.get().getName());

        return codeCustomerAssociationRepository.save(association);
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

    public Page<TicTicQrCodeCustomerAssociations> getQrCodesAssociated(String ownerId, Pageable pageable) {
        return codeCustomerAssociationRepository.findByOwnerId(ownerId, pageable);
    }

    public Page<QrCodeGroup> getQrCodes(String ownerId, Pageable pageable) {
        return qrCodeGroupRepository.findByOwnerId(ownerId, pageable);
    }

    public List<TicTicQrCodeCustomerAssociations> getAssociatedQRCodesForCustomer(String customerId) {
        return codeCustomerAssociationRepository.findByCustomerId(customerId);
    }
}


