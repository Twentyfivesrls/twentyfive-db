package com.twentyfive.twentyfivedb.tictic.service;

import com.twentyfive.twentyfivedb.qrGenDB.repository.QrCodeGroupRepository;
import com.twentyfive.twentyfivedb.tictic.repository.CustomerRepository;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.models.qrGenModels.QrCodeGroup;
import twentyfive.twentyfiveadapter.models.tictickModels.TicTicCustomer;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class AutoCompleteService {

  private final QrCodeGroupRepository qrCodeGroupRepository;
  private final CustomerRepository customerRepository;

  public AutoCompleteService(QrCodeGroupRepository qrCodeGroupRepository, CustomerRepository customerRepository) {
    this.qrCodeGroupRepository = qrCodeGroupRepository;
    this.customerRepository = customerRepository;
  }

  public Set<AutoCompleteRes> getAutoCompleteResults(String filterObject, String ownerId) {
    Set<AutoCompleteRes> results = new LinkedHashSet<>();

    // Recupera le email dei customer
    Set<TicTicCustomer> customers = customerRepository.findByAnyMatchingFields(filterObject);
    customers.forEach(customer -> {
      AutoCompleteRes res = new AutoCompleteRes();
      res.setValue(customer.getEmail());
      results.add(res);
    });

    // Recupera i gruppi di QR code
    Set<QrCodeGroup> qrCodeGroups = qrCodeGroupRepository.findByOwnerIdAndAnyMatchingFields(ownerId, filterObject);
    qrCodeGroups.forEach(qrCodeGroup -> {
      AutoCompleteRes res = new AutoCompleteRes();
      res.setValue(qrCodeGroup.getIdQrCode());
      results.add(res);
    });

    return results;
  }
}
