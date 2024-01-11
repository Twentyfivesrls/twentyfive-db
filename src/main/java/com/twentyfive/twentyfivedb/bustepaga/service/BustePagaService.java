package com.twentyfive.twentyfivedb.bustepaga.service;

import com.twentyfive.twentyfivedb.bustepaga.repository.BustePagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.BustePagaDocumentDB.Dipendente;

import java.util.List;

@Slf4j
@Service
public class BustePagaService {
    private final BustePagaRepository bustePagaRepository;

    public BustePagaService(BustePagaRepository bustePagaRepository) {
        this.bustePagaRepository = bustePagaRepository;
    }

    public List<Dipendente> getAllDipendenti(String userId) {
        return this.bustePagaRepository.getAllByUserId(userId);
    }

    public Dipendente createDipendente(String userId, Dipendente dipendente) {
        dipendente.setUserId(userId);
        return this.bustePagaRepository.save(dipendente);
    }

    public void deleteDipendente(String id) {
        this.bustePagaRepository.deleteById(id);
    }
}
