package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.FornitoreService;
import com.twentyfive.twentyfivemodel.models.partenupModels.Fornitore;
import com.twentyfive.twentyfivemodel.models.partenupModels.QuotazioneGiornaliera;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/fornitori")
@RequiredArgsConstructor
@Slf4j
public class FornitoreController {
    private final FornitoreService fornitoreService;


    @GetMapping("/getall")
    public ResponseEntity<Object> getallfornitori(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){
        return new ResponseEntity<>(fornitoreService.tuttifornitori(nPage,nDimension), HttpStatus.OK);
    }

    @GetMapping("/getfornitore/{nomefornitore}")
    public ResponseEntity<Object> prendifornitore(@PathVariable String nomefornitore){
        try{
            return new ResponseEntity<>(fornitoreService.getfornitore(nomefornitore), HttpStatus.OK);
        }catch (Exception e){
            log.error("Non esiste questo fornitore");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/aggiungi")
    public ResponseEntity<Object> aggiungifornitore(@RequestBody Fornitore fornitore){
        try {
            return new ResponseEntity<>(fornitoreService.aggiungifornitore(fornitore), HttpStatus.OK);
        }catch (Exception e){
            log.error("Non è stato possibile aggiungere questo fornitore");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/delete/{idfornitore}")
    public ResponseEntity<Object> eliminafornitore(@PathVariable long idfornitore){
        try {
            return new ResponseEntity<>(fornitoreService.rimuovifornitore(idfornitore), HttpStatus.OK);
        }catch (Exception e){
            log.error("Non è stato possibile rimuovere questo fornitore");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/eliminaquotazione")
    public ResponseEntity<Object> eliminaquotazione(@RequestBody QuotazioneGiornaliera quotazioneGiornaliera){
        try {
            return new ResponseEntity<>(fornitoreService.rimuoviquotazione(quotazioneGiornaliera), HttpStatus.OK);
        }catch (Exception e){
            log.error("Non è stato possibile rimuovere questa quotazione");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
