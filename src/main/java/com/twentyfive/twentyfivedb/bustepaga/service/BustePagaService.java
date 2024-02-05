package com.twentyfive.twentyfivedb.bustepaga.service;

import com.twentyfive.twentyfivedb.bustepaga.repository.BustePagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<Dipendente> getAllDipendenti(String userId, int page, int size, String sortColumn, String sortDirection) {
        Sort.Direction direction;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        } else {
            direction = Sort.Direction.ASC;
        }

        // Create a Pageable instance
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortColumn));
        return bustePagaRepository.getAllByUserId(userId, pageable);


    }

    public Dipendente createDipendente(Dipendente dipendente) {
        return this.bustePagaRepository.save(dipendente);
    }

    public void deleteDipendente(String id) {
        this.bustePagaRepository.deleteById(id);
    }

    public List<Dipendente> getAllDipendentiList(String userId) {
        return this.bustePagaRepository.getAllByUserId(userId);
    }
}
