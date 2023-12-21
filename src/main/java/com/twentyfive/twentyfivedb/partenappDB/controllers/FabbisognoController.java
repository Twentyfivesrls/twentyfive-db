package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.FabbisognoService;
import com.twentyfive.twentyfivedb.partenappDB.services.PreventivoService;
import com.twentyfive.twentyfivedb.partenappDB.services.RiepilogoService;
import com.twentyfive.twentyfivedb.partenappDB.services.TrasportoService;
import com.twentyfive.twentyfivemodel.dto.partenupDto.DateRange;
import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import com.twentyfive.twentyfivemodel.models.partenupModels.Preventivo;
import com.twentyfive.twentyfivemodel.models.partenupModels.Trasporto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/fabbisogno")
@RequiredArgsConstructor
@Slf4j
public class FabbisognoController {
    private final FabbisognoService fabbisognoService;

    private final TrasportoService trasportoService;

    private final PreventivoService preventivoService;

    private final RiepilogoService riepilogoService;


    @GetMapping("/getall")
    public ResponseEntity<Object> getallfabbisogni(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){
        return new ResponseEntity<>(fabbisognoService.getallfabbisogni(nPage, nDimension), HttpStatus.OK);
    }

    @PostMapping("/getallindaterange")
    public ResponseEntity<Object> getallfabbisogniindaterange(@RequestBody DateRange range) {
        return new ResponseEntity<>(fabbisognoService.getindaterange(range), HttpStatus.OK);
    }

    @GetMapping("/getfabbisogno/{id}")
    public ResponseEntity<Object> getfabbisogno(@PathVariable long id){
        try {
            return new ResponseEntity<Object>(fabbisognoService.getfabbisognodaid(id), HttpStatus.OK);
        } catch (Exception e){
            log.error("Non esiste Fabbisogno con questo ID");
            return new ResponseEntity<Object>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/aggiungi")
    public ResponseEntity<Object> aggiungifabbisogno(@RequestBody Fabbisogno fabbisogno){
        try {
            return new ResponseEntity<>(fabbisognoService.aggiungifabbisogno(fabbisogno), HttpStatus.OK);
        } catch (Exception e){
            log.error("Non esiste Fabbisogno con questo ID");
            return new ResponseEntity<Object>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/elimina")
    public ResponseEntity<Object> eliminafabbisogno(@RequestBody Fabbisogno fabbisogno){

        try{
            Trasporto t = trasportoService.getbyfabbisognoid(fabbisogno.getId());
            t.setFabbisogno(null);
            trasportoService.salva(t);
            trasportoService.rimuovi(t);
        }catch(Exception e){
            e.printStackTrace();
        }


        try {
            Preventivo preventivo = preventivoService.getpreventivofromfabbisogno(fabbisogno);
            preventivo.setRiferimento(null);
            preventivoService.aggiungimodificapreventivo(preventivo);
            preventivoService.cancellapreventivo(preventivo);
        } catch(Exception e){
            e.printStackTrace();
        }

        try{
            riepilogoService.cancellariepilogodafabbisogno(fabbisogno);
        }catch(Exception e){
            e.printStackTrace();
        }
        try {
            return new ResponseEntity<>(fabbisognoService.eliminafabbisogno(fabbisogno), HttpStatus.OK);
        } catch (Exception e){
            log.error("Non esiste Fabbisogno con questo ID");
            return new ResponseEntity<Object>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
