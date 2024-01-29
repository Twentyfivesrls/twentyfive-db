package com.twentyfive.twentyfivedb.twentyfiveapps.controller;

import com.twentyfive.twentyfivedb.twentyfiveapps.service.TwentyfiveAppsService;
import com.twentyfive.twentyfivemodel.models.twentyfiveappsModels.TwentyfiveApp;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/twentyfive-apps")
public class TwentyfiveAppsController {

    private final TwentyfiveAppsService twentyfiveAppsService;

    public TwentyfiveAppsController(TwentyfiveAppsService twentyfiveAppsService) {
        this.twentyfiveAppsService = twentyfiveAppsService;
    }

    @GetMapping("/page")
    public ResponseEntity<Page<TwentyfiveApp>> getAllApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(twentyfiveAppsService.getAllApps(page, size));
    }

    @GetMapping("/list")
    public ResponseEntity<List<TwentyfiveApp>> getAllApps() {
        return ResponseEntity.ok(twentyfiveAppsService.getAllApps());
    }


}
