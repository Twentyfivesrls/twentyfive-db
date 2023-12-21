package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.TrasportoService;
import com.twentyfive.twentyfivemodel.dto.partenupDto.TrasportoFilter;
import com.twentyfive.twentyfivemodel.dto.partenupDto.Viaggio;
import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import com.twentyfive.twentyfivemodel.models.partenupModels.Trasporto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/trasporto")
@RequiredArgsConstructor
@Slf4j
public class TrasportoController {
    private final TrasportoService trasportoService;



    @GetMapping("/getall")
    public ResponseEntity<Object> getall(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){
        return new ResponseEntity<>(trasportoService.getalltrasporti(nPage, nDimension), HttpStatus.OK);
    }

    @GetMapping("/findbyfabbisognoid/{id}")
    public ResponseEntity<Object> findbyid(@PathVariable long id){

        try {
            return new ResponseEntity<>(trasportoService.getbyfabbisognoid(id), HttpStatus.OK);
        } catch (Exception e){
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/salva")
    public ResponseEntity<Object> salva(@RequestBody Trasporto trasporto){
        try {
            return new ResponseEntity<>(trasportoService.salva(trasporto), HttpStatus.OK);
        }catch (Exception e){
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/rimuovi")
    public ResponseEntity<Object> rimuovi(@RequestBody Trasporto trasporto){

        try {
            return new ResponseEntity<>(trasportoService.rimuovi(trasporto), HttpStatus.OK);
        }catch (Exception e){
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/rimuovidafabbisogno")
    public ResponseEntity<Object> rimuovidafabbisogno(@RequestBody Fabbisogno fabbisogno){
        try {
            Trasporto dacancellare = trasportoService.getbyfabbisognoid(fabbisogno.getId());
            return new ResponseEntity<>(rimuovi(dacancellare), HttpStatus.OK);
        }catch (Exception e){
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/findviaggio")
    public ResponseEntity<Object> findviaggio(@RequestBody TrasportoFilter trasportoFilter){
        return new ResponseEntity<>(trasportoService.findbyfilter(trasportoFilter), HttpStatus.OK);
    }
}
