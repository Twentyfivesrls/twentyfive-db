package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.VoceDiRettificaService;
import com.twentyfive.twentyfivemodel.models.partenupModels.VoceDiRettifica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/vocidirettifica")
@RequiredArgsConstructor
@Slf4j
public class VoceDiRettificaController {
    private final VoceDiRettificaService voceDiRettificaService;


    @GetMapping("/getall")
    public ResponseEntity<Object> getallvocidirettifica(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){
        return new ResponseEntity<>(voceDiRettificaService.tuttelevocidirettifica(nPage, nDimension), HttpStatus.OK);
    }

    @GetMapping("/getvocedirettifica/{nomevoce}")
    public ResponseEntity<Object> prendivocedirettifica(@PathVariable String nomevoce){
        try {
            return new ResponseEntity<>(voceDiRettificaService.getvocedirettifica(nomevoce), HttpStatus.OK);
        } catch(Exception e){
            log.error("Problemi ad ottenere la voce di rettifica");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/aggiungi")
    public ResponseEntity<Object> aggiungivoce(@RequestBody VoceDiRettifica voceDiRettifica){
        try {
            return new ResponseEntity<>(voceDiRettificaService.aggiungivocedirettifica(voceDiRettifica), HttpStatus.OK);
        }catch(Exception e){
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/delete/{idvoce}")
    public ResponseEntity<Object> eliminavocedirettifica(@PathVariable long idvoce){
        try {
            return new ResponseEntity<>(voceDiRettificaService.rimuovivocedirettifica(idvoce), HttpStatus.OK);
        }catch(Exception e){
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
