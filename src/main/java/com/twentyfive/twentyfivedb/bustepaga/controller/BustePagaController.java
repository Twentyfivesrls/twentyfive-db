package com.twentyfive.twentyfivedb.bustepaga.controller;

import com.twentyfive.twentyfivedb.bustepaga.service.BustePagaService;
import com.twentyfive.twentyfivemodel.dto.bustepagaDto.UpdateBPSettingRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import twentyfive.twentyfiveadapter.models.bustepagaModels.BPConfiguration;
import twentyfive.twentyfiveadapter.models.bustepagaModels.BPFile;
import twentyfive.twentyfiveadapter.models.bustepagaModels.Dipendente;
import twentyfive.twentyfiveadapter.models.bustepagaModels.BPSetting;

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

    @GetMapping(value = "/get-by-id")
    public ResponseEntity<Dipendente> getDipendenteById(@RequestParam(name = "userId") String userId, @RequestParam(name = "employeeId") String employeeId) {
        return ResponseEntity.ok(bustePagaService.getDipendenteById(userId, employeeId));
    }

    //lavorazione
    @GetMapping(value = "/get-by-employeeId")
    public ResponseEntity<Dipendente> getByEmployeeId(@RequestParam(name = "employeeId") String employeeId) {

        return ResponseEntity.ok(bustePagaService.getByEmployeeId(employeeId));
    }

    @GetMapping(value = "/get-by-email")
    public ResponseEntity<Dipendente> getDipendenteByEmail(@RequestParam(name = "email") String email) {
        return ResponseEntity.ok(bustePagaService.getDipendenteByEmail(email));
    }

    @PostMapping("/create")
    public ResponseEntity<Dipendente> createDipendente(@RequestBody Dipendente dipendente) {
        return ResponseEntity.ok(bustePagaService.createDipendente(dipendente));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteDipendente(@PathVariable String id) {

        return ResponseEntity.ok().body(bustePagaService.deleteDipendente(id));
    }


    /*----------- SETTINGS ENDPOINTS ----------*/
    @GetMapping("/get-settings")
    public ResponseEntity<BPSetting> getSettings(@RequestParam(name = "userId") String userId) {
        BPSetting setting = bustePagaService.getSettings(userId);
        return ResponseEntity.ok(setting);
    }

    @PostMapping("/update-setting")
    public ResponseEntity<Boolean> updateSetting(@RequestBody UpdateBPSettingRequest request) {
        bustePagaService.updateSetting(request);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/get-all-configurations")
    public ResponseEntity<List<BPConfiguration>> getConfigurationsByType(@RequestParam(name = "type", defaultValue = "PAYBOLT_FILENAME") String type) {
        List<BPConfiguration> configuration = bustePagaService.getConfigurationsByType(type);
        return ResponseEntity.ok(configuration);
    }



    /*------------- FILE ENDPOINTS ---------*/

    @GetMapping(value = "/get-files-by-id")
    public ResponseEntity<Page<BPFile>> getFilesByDipendenteId(@RequestParam(name = "employeeId") String employeeId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "5") int size,
                                                               @RequestParam(defaultValue = "lastname") String sortColumn,
                                                               @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(bustePagaService.getFilesByDipendenteId(employeeId, page, size, sortColumn, sortDirection));
    }

    @GetMapping(value = "/get-files-by-email")
    public ResponseEntity<Page<BPFile>> getFilesByDipendenteEmail(@RequestParam(name = "employeeEmail") String employeeEmail,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "5") int size,
                                                               @RequestParam(defaultValue = "lastname") String sortColumn,
                                                               @RequestParam(defaultValue = "asc") String sortDirection) {
        return ResponseEntity.ok(bustePagaService.getFilesByDipendenteEmail(employeeEmail, page, size, sortColumn, sortDirection));
    }

    @PostMapping(value="/save-file")
    public ResponseEntity<BPFile> saveFile(@RequestBody BPFile file) {
        return ResponseEntity.ok(bustePagaService.saveFile(file));
    }

    @DeleteMapping("/delete-file/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        bustePagaService.deleteFile(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/set-as-read/{id}")
    public ResponseEntity<Boolean> setAsRead(@PathVariable String id) {
        return ResponseEntity.ok(bustePagaService.setAsRead(id));
    }

    @GetMapping("/set-as-confirmed/{id}")
    public ResponseEntity<Boolean> setAsConfirmed(@PathVariable String id) {
        return ResponseEntity.ok(bustePagaService.setAsConfirmed(id));
    }

    @GetMapping("/get-file-by-id")
    public ResponseEntity<BPFile> getFileById(@RequestParam(name = "id") String id) {
        return ResponseEntity.ok(bustePagaService.getById(id));
    }


}
