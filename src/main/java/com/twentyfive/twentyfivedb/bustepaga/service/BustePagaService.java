package com.twentyfive.twentyfivedb.bustepaga.service;

import com.twentyfive.twentyfivedb.bustepaga.repository.BustePagaRepository;
import com.twentyfive.twentyfivedb.bustepaga.repository.ConfigurationsRepository;
import com.twentyfive.twentyfivedb.bustepaga.repository.FileRepository;
import com.twentyfive.twentyfivedb.bustepaga.repository.SettingsRepository;
import com.twentyfive.twentyfivemodel.dto.bustepagaDto.UpdateBPSettingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import twentyfive.twentyfiveadapter.models.bustepagaModels.BPConfiguration;
import twentyfive.twentyfiveadapter.models.bustepagaModels.BPFile;
import twentyfive.twentyfiveadapter.models.bustepagaModels.Dipendente;
import twentyfive.twentyfiveadapter.models.bustepagaModels.BPSetting;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BustePagaService {
    private final BustePagaRepository bustePagaRepository;
    private final SettingsRepository settingsRepository;
    private final ConfigurationsRepository configurationsRepository;
    private final FileRepository fileRepository;

    public BustePagaService(BustePagaRepository bustePagaRepository, SettingsRepository settingsRepository, ConfigurationsRepository configurationsRepository, FileRepository fileRepository) {
        this.bustePagaRepository = bustePagaRepository;
        this.settingsRepository = settingsRepository;
        this.configurationsRepository = configurationsRepository;
        this.fileRepository = fileRepository;
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

    public String deleteDipendente(String id) {
        String email = "";
        this.deleteFileFromDipendenteId(id); //delete all records regarding files
        //TODO we've to delete all the files from the media manager

        Optional<Dipendente> dipendente = bustePagaRepository.findById(id);

        if(dipendente.isPresent()) {
            email =dipendente.get().getEmail();
        }

        this.bustePagaRepository.deleteById(id);
        return email;
    }

    private void deleteFileFromDipendenteId(String id) {
        this.fileRepository.deleteAllByEmployeeId(id);
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

    public List<BPConfiguration> getConfigurationsByType(String type) {
        return this.configurationsRepository.getAllByType(type);
    }

    public void updateSetting(UpdateBPSettingRequest request) {
        Optional<BPSetting> bpSetting = this.settingsRepository.getByUserId(request.getUserId());

        List<BPConfiguration> confList = request.getConfigurations().stream().map(conf -> {
            BPConfiguration configuration = new BPConfiguration();
            configuration.setLabel(conf.getLabel());
            configuration.setValue(conf.getValue());
            configuration.setType(conf.getType());
            configuration.setOrder(conf.getOrder());
            return configuration;
        }).toList();

        BPSetting setting;
        if (bpSetting.isPresent()) {
            setting = bpSetting.get();
        } else {
            setting = new BPSetting();
            setting.setUserId(request.getUserId());
        }
        setting.setMailText(request.getMailText());
        setting.setFileName(confList);
        this.settingsRepository.save(setting);
    }

    public Dipendente getDipendenteById(String userId, String employeeId) {
        return this.bustePagaRepository.getDipendenteByUserIdAndId(userId, employeeId);
    }

    public Dipendente getDipendenteByEmail(String email) {
        return this.bustePagaRepository.getDipendenteByEmail(email);
    }


    public Page<BPFile> getFilesByDipendenteId(String employeeId, int page, int size, String sortColumn, String sortDirection) {
        Sort.Direction direction;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        } else {
            direction = Sort.Direction.ASC;
        }

        // Create a Pageable instance
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortColumn));
        return this.fileRepository.getAllByEmployeeId(employeeId, pageable);
    }

    public Page<BPFile> getFilesByDipendenteEmail(String employeeEmail, int page, int size, String sortColumn, String sortDirection) {
        Optional<Dipendente> dipendente = this.bustePagaRepository.findDipendenteByEmail(employeeEmail);
        if (dipendente.isEmpty()) {
            return null;
        }
        String employeeId = dipendente.get().getId();
        Sort.Direction direction;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        } else {
            direction = Sort.Direction.ASC;
        }
        // Create a Pageable instance
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortColumn));
        return this.fileRepository.getAllByEmployeeId(employeeId, pageable);
    }

    public BPFile saveFile(BPFile file) {
        return this.fileRepository.save(file);
    }

    public void deleteFile(String id) {
        this.fileRepository.deleteById(id);
    }

    public Boolean setAsRead(String id) {
        Optional<BPFile> file = this.fileRepository.findById(id);
        if (file.isPresent()) {
            BPFile bpFile = file.get();
            bpFile.setConfirmed(true);
            this.fileRepository.save(bpFile);
            return true;
        }
        return false;
    }

}
