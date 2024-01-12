package com.twentyfive.twentyfivedb.bustepaga.service;

import com.twentyfive.twentyfivedb.bustepaga.repository.BustePagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.BustePagaDocumentDB.Dipendente;

import java.awt.print.Pageable;

@Slf4j
@Service
public class BustePagaService {
    private final BustePagaRepository bustePagaRepository;

    public BustePagaService(BustePagaRepository bustePagaRepository) {
        this.bustePagaRepository = bustePagaRepository;
    }

    public Page<Dipendente> getAllDipendenti(String userId, int page, int size) {
        //create a pageable object
        PageRequest pageable = PageRequest.of(page, size);
        return this.bustePagaRepository.getAllByUserId(userId, pageable);
    }

    public Dipendente createDipendente(Dipendente dipendente) {
        return this.bustePagaRepository.save(dipendente);
    }

    public void deleteDipendente(String id) {
        this.bustePagaRepository.deleteById(id);
    }
}
