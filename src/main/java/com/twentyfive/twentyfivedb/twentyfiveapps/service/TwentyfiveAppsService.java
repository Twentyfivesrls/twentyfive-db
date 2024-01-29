package com.twentyfive.twentyfivedb.twentyfiveapps.service;

import com.twentyfive.twentyfivedb.twentyfiveapps.repository.TwentyfiveAppsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.adapter.Document.TwentyfiveAppsDocumentDB.TwentyfiveApp;

import java.util.List;

@Slf4j
@Service
public class TwentyfiveAppsService {

    private final TwentyfiveAppsRepository twentyfiveAppsRepository;

    public TwentyfiveAppsService(TwentyfiveAppsRepository twentyfiveAppsRepository) {
        this.twentyfiveAppsRepository = twentyfiveAppsRepository;
    }

    public Page<TwentyfiveApp> getAllApps(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return this.twentyfiveAppsRepository.findAll(pageable);
    }

    public List<TwentyfiveApp> getAllApps() {
        return this.twentyfiveAppsRepository.findAll();
    }

}
