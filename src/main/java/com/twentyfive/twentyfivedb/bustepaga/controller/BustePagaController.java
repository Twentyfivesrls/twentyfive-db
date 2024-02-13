package com.twentyfive.twentyfivedb.bustepaga.controller;

import com.twentyfive.twentyfivedb.bustepaga.service.BustePagaService;
import com.twentyfive.twentyfivemodel.dto.bustepagaDto.UpdateBPSettingRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.adapter.Document.BustePagaDocumentDB.BPConfiguration;
import twentyfive.twentyfiveadapter.adapter.Document.BustePagaDocumentDB.BPSetting;
import twentyfive.twentyfiveadapter.adapter.Document.BustePagaDocumentDB.Dipendente;

import java.util.List;


@RestController
@RequestMapping("/buste-paga")
public class BustePagaController {

    private final BustePagaService bustePagaService;

    public BustePagaController(BustePagaService bustePagaService) {
        this.bustePagaService = bustePagaService;
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Dipendente>> getAllDipendenti(
            @RequestParam(name = "userId") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "lastname") String sortColumn,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(bustePagaService.getAllDipendenti(userId, page, size, sortColumn, sortDirection));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Dipendente>> getAllDipendenti(
            @RequestParam(name = "userId") String userId) {
        return ResponseEntity.ok(bustePagaService.getAllDipendentiList(userId));
    }

    @PostMapping("/create")
    public ResponseEntity<Dipendente> createDipendente(@RequestBody Dipendente dipendente) {
        return ResponseEntity.ok(bustePagaService.createDipendente(dipendente));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDipendente(@PathVariable String id) {
        bustePagaService.deleteDipendente(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-settings")
    public ResponseEntity<BPSetting> getSettings(@RequestParam(name = "userId") String userId){
        BPSetting setting = bustePagaService.getSettings(userId);
        return ResponseEntity.ok(setting);
    }

    @PostMapping("/update-setting")
    public ResponseEntity<Boolean> updateSetting(@RequestBody UpdateBPSettingRequest request){
        bustePagaService.updateSetting(request);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/get-all-configurations")
    public ResponseEntity<List<BPConfiguration>> getConfigurationsByType(@RequestParam(name="type", defaultValue = "PAYBOLT_FILENAME") String type){
        List<BPConfiguration> configuration = bustePagaService.getConfigurationsByType(type);
        return ResponseEntity.ok(configuration);
    }


}
