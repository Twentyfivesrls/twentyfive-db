package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.BaseDiCaricoService;
import com.twentyfive.twentyfivemodel.models.partenupModels.BaseDiCarico;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/basidicarico")
@RequiredArgsConstructor
@Slf4j
public class BaseDiCaricoController {
    private final BaseDiCaricoService baseDiCaricoService;
    @GetMapping("/getall")
    public ResponseEntity<Object> getallbasidicarico(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){
        return new ResponseEntity<>(baseDiCaricoService.tuttelebasidicarico(nPage, nDimension), HttpStatus.OK);
    }

    @GetMapping("/getbasedicarico/{nomebase}")
    public ResponseEntity<Object> prendibasedicarico(@PathVariable String nomebase){
        try{
            return new ResponseEntity<>(baseDiCaricoService.getbasedicarico(nomebase), HttpStatus.OK);
        } catch(Exception e){
            log.error("base di carico non presente in db");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/aggiungi")
    public ResponseEntity<Object> aggiungibasedicarico(@RequestBody BaseDiCarico basedicarico){
        try{
            return new ResponseEntity<>(baseDiCaricoService.aggiungibasedicarico(basedicarico), HttpStatus.OK);
        } catch(Exception e){
            log.error("Impossibile salvare questa base di carico");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/delete/{idbase}")
    public ResponseEntity<Object> eliminabasedicarico(@PathVariable Long idbase){
        try {
            return new ResponseEntity<>(baseDiCaricoService.rimuovibasedicarico(idbase), HttpStatus.OK);
        }catch(Exception e){
            log.error("Non esiste questa Base Di Carico ID");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
