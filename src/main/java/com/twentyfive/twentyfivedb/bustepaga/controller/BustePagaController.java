package com.twentyfive.twentyfivedb.bustepaga.controller;

import com.twentyfive.twentyfivedb.bustepaga.service.BustePagaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.BustePagaDocumentDB.Dipendente;

import java.util.List;

@RestController
@RequestMapping("/buste-paga")
public class BustePagaController {

    private final BustePagaService bustePagaService;

    public BustePagaController(BustePagaService bustePagaService) {
        this.bustePagaService = bustePagaService;
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<List<Dipendente>> getAllDipendenti(@PathVariable String userId){
        return ResponseEntity.ok(bustePagaService.getAllDipendenti(userId));
    }

    @PostMapping("/create")
    public ResponseEntity<Dipendente> createDipendente(@RequestParam("userId") String userId, @RequestBody Dipendente dipendente){
        return ResponseEntity.ok(bustePagaService.createDipendente(userId, dipendente));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDipendente(@PathVariable String id){
        bustePagaService.deleteDipendente(id);
        return ResponseEntity.ok().build();
    }


}
