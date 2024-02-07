package com.twentyfive.twentyfivedb.bustepaga.service;

import com.twentyfive.twentyfivedb.bustepaga.repository.BustePagaRepository;
import com.twentyfive.twentyfivedb.bustepaga.repository.SettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.BustePagaDocumentDB.BPSetting;
import twentyfive.twentyfiveadapter.adapter.Document.BustePagaDocumentDB.Dipendente;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BustePagaService {
    private final BustePagaRepository bustePagaRepository;
    private final SettingsRepository settingsRepository;

    public BustePagaService(BustePagaRepository bustePagaRepository, SettingsRepository settingsRepository) {
        this.bustePagaRepository = bustePagaRepository;
        this.settingsRepository = settingsRepository;
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

    public BPSetting getSettings(String userId) {
        Optional<BPSetting> bpSetting = this.settingsRepository.getByUserId(userId);
        return bpSetting.orElse(null);
    }
    public String getMailText(String userId) {
        Optional<BPSetting> bpSetting = this.settingsRepository.getByUserId(userId);
        return bpSetting.map(BPSetting::getMailText).orElse("");
    }

    public void updateMailText(String userId, String mailText) {
        Optional<BPSetting> bpSetting = this.settingsRepository.getByUserId(userId);
        if (bpSetting.isPresent()) {
            BPSetting setting = bpSetting.get();
            setting.setMailText(mailText);
            this.settingsRepository.save(setting);
        } else {
            BPSetting setting = new BPSetting();
            setting.setUserId(userId);
            setting.setMailText(mailText);
            this.settingsRepository.save(setting);
        }
    }
}
