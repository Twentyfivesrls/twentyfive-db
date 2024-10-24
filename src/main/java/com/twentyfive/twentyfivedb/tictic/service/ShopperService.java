package com.twentyfive.twentyfivedb.tictic.service;

import com.twentyfive.twentyfivedb.Utility;
import com.twentyfive.twentyfivedb.tictic.repository.CustomerRepository;
import com.twentyfive.twentyfivedb.tictic.repository.ShopperRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.TicketObjDocumentDB.EventDocumentDB;
import twentyfive.twentyfiveadapter.models.fidelityModels.CardGroup;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicCustomer;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicShopper;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ShopperService {

    private final ShopperRepository shopperRepository;
    private final CustomerRepository customerRepository;

    public ShopperService(ShopperRepository shopperRepository, CustomerRepository customerRepository) {
        this.shopperRepository = shopperRepository;
        this.customerRepository = customerRepository;
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
}
