package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.PreventivoService;
import com.twentyfive.twentyfivedb.partenappDB.services.RiepilogoService;
import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import com.twentyfive.twentyfivemodel.models.partenupModels.Preventivo;
import com.twentyfive.twentyfivemodel.models.partenupModels.VoceDiRettificaConValore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/preventivo")
@RequiredArgsConstructor
@Slf4j
public class PreventivoController {
    private final PreventivoService preventivoService;


    private final RiepilogoService riepilogoService;

    @GetMapping("/getAll")
    public ResponseEntity<Object> getAll(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){
        return new ResponseEntity<>(preventivoService.getall(nPage, nDimension), HttpStatus.OK);
    }


    @PostMapping("/getpreventivodifabbisogno")
    public ResponseEntity<Object> getpreventivodifabbisogno(@RequestBody Fabbisogno fabbisogno){
        try{
            return new ResponseEntity<>(preventivoService.getpreventivofromfabbisogno(fabbisogno), HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            log.error("Errore nel DB");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/aggiungimodifica")
    public ResponseEntity<Object> aggiungimodificapreventivo(@RequestBody Preventivo preventivo){
        try{
            return new ResponseEntity<>(preventivoService.aggiungimodificapreventivo(preventivo), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Errore nell'inserimento del preventivo");
            return new ResponseEntity<>("DB error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @PostMapping("/cancella")
    public ResponseEntity<Object> cancellapreventivo(@RequestBody Preventivo preventivo){
        try{
            riepilogoService.cancellariepilogo(preventivo);
            return new ResponseEntity<>(preventivoService.cancellapreventivo(preventivo), HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            log.error("Errore nell'eliminazione del preventivo");
            return new ResponseEntity<>("DB error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cancelladafabbisogno")
    public ResponseEntity<Object> cancellapreventivodafabbisogno(@RequestBody Fabbisogno fabbisogno){
        try{
            Preventivo dacancellare = preventivoService.getpreventivofromfabbisogno(fabbisogno);
           return new ResponseEntity<>(cancellapreventivo(dacancellare), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            log.error("Errore nell'eliminazione del preventivo da fabbisogno");
            return new ResponseEntity<>("DB error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/cancellavocedirettificadapreventivo/{idpreventivo}")
    public ResponseEntity<Object> cancellavocedirettificadapreventivo(@RequestBody VoceDiRettificaConValore voceDiRettificaConValore, @PathVariable long idpreventivo){
        try{
            return new ResponseEntity<>(preventivoService.cancellavocedirettificadapreventivo(idpreventivo,voceDiRettificaConValore), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            log.error("Errore nell'eliminazione del preventivo");
            return new ResponseEntity<>("DB error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
