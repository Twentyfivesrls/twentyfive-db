package com.twentyfive.twentyfivedb.tictic.controller;

import com.twentyfive.twentyfivedb.tictic.service.AutoCompleteService;
import com.twentyfive.twentyfivemodel.filterTicket.AutoCompleteRes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/tictic/autocomplete")
public class AutoCompleteController {

  private final AutoCompleteService autoCompleteService;

  public AutoCompleteController(AutoCompleteService autoCompleteService) {
    this.autoCompleteService = autoCompleteService;
  }

  @GetMapping("/filter/customerandqrcode/autocomplete")
  public ResponseEntity<Set<AutoCompleteRes>> filterAutocomplete(
    @RequestParam("filterObject") String filterObject,
    @RequestParam("ownerId") String ownerId) {

    Set<AutoCompleteRes> results = autoCompleteService.getAutoCompleteResults(filterObject, ownerId);
    return ResponseEntity.ok(results);
  }
}
