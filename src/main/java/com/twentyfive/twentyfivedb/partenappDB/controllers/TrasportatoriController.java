package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.TrasportatoriService;
import com.twentyfive.twentyfivemodel.models.partenupModels.Atk;
import com.twentyfive.twentyfivemodel.models.partenupModels.Autista;
import com.twentyfive.twentyfivemodel.models.partenupModels.Rimorchio;
import com.twentyfive.twentyfivemodel.models.partenupModels.Trasportatore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/trasportatori")
@RequiredArgsConstructor
@Slf4j
public class TrasportatoriController {
    private final TrasportatoriService trasportatoriService;



    // ********** TRASPORTATORI *************

    @GetMapping("/getall")
    private ResponseEntity<Object> listatrasportatori(@RequestParam("nPage")int nPage, @RequestParam("nDimensione")int nDimensione){
        return new ResponseEntity<>(trasportatoriService.getalltrasportatori(nPage, nDimensione), HttpStatus.OK);
    }

    @GetMapping("/gettrasportatore/{idtrasportatore}")
    private ResponseEntity<Object> gettrasportatore(@PathVariable String idtrasportatore){
        try {
            return new ResponseEntity<>(trasportatoriService.gettrasportatore(idtrasportatore), HttpStatus.OK);
        }catch(Exception e){
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PostMapping("/aggiungitrasportatore")
    public ResponseEntity<Object> aggiungitrasportatore(@RequestBody Trasportatore trasportatore){

    /*


        for(Rimorchio r : trasportatore.getListarimorchi()){
            aggiungirimorchio(r);
        }


        for(Atk a : trasportatore.getListaatk()){
            aggiungiatk(a);
        }



        for(Autista a : trasportatore.getListaautisti()){
            aggiungiautista(a);
        }

        */





        try {
            return new ResponseEntity<>(trasportatoriService.aggiungitrasportatore(trasportatore), HttpStatus.OK);
        } catch(Exception e){
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/rimuovitrasportatore/{idtrasportatore}")
    public ResponseEntity<Object> rimuovitrasportatore(@PathVariable String idtrasportatore){
        try {
            return new ResponseEntity<>(trasportatoriService.rimuovitrasportatore(idtrasportatore), HttpStatus.OK);
        } catch(Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // *********** FINE TRASPORTATORI *************


    // *********** ATK ***************

    @GetMapping("/getallatk")
    public ResponseEntity<Object> getallatk(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){
        return new ResponseEntity<>(trasportatoriService.getallatk(nPage,nDimension), HttpStatus.OK);
    }

    @GetMapping("/getatk/{idatk}")
    public ResponseEntity<Object> getatk(@PathVariable long idatk){
            try {
                return new ResponseEntity<>(trasportatoriService.getatk(idatk), HttpStatus.OK);
            } catch (Exception e) {
                log.error("Problemi ad aggiungere il cliente");
                return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    @GetMapping("/getatkdacodice/{codice}")
    public ResponseEntity<Object> getatkdacodice(@PathVariable String codice){
        try {
            return new ResponseEntity<>(trasportatoriService.getatkdacodice(codice), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getatkdatarga/{targa}")
    public ResponseEntity<Object> getatkdatarga(@PathVariable String targa){
        try {
            return new ResponseEntity<>(trasportatoriService.getatkdatarga(targa), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/aggiungiatk")
    public ResponseEntity<Object> aggiungiatk(@RequestBody Atk atk){
        try {
            return new ResponseEntity<>(trasportatoriService.aggiungiatk(atk), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere l'atk");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/rimuoviatk/{idatk}")
    public ResponseEntity<Object> rimuoviatk(@PathVariable long idatk){
        // todo
        // staccare l'atk dal trasportatore

        try {
            return new ResponseEntity<>(trasportatoriService.rimuoviatk(idatk), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ******** FINE ATK **********

    @GetMapping("/getallrimorchi")
    public ResponseEntity<Object> getallrimorchi(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){
        return new ResponseEntity<>(trasportatoriService.getallrimorchi(nPage, nDimension), HttpStatus.OK);
    }



    @GetMapping("/getrimorchiodatarga/{targa}")
    public ResponseEntity<Object> getrimorchiodatarga(@PathVariable String targa){
        try {
            return new ResponseEntity<>(trasportatoriService.getrimorchio(targa), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/aggiungirimorchio")
    public ResponseEntity<Object> aggiungirimorchio(@RequestBody Rimorchio rimorchio){
        try {
            return new ResponseEntity<>(trasportatoriService.aggiungirimorchio(rimorchio), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/rimuovirimorchio/{targa}")
    public ResponseEntity<Object> rimuovirimorchio(@PathVariable String targa){
        // todo
        // staccare rimorchio dal trasportatore
        try {
            return new ResponseEntity<>(trasportatoriService.rimuovirimorchio(targa), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    // ********** FINE RIMORCHI **********


    // ********** AUTISTI *************

    @GetMapping("/getallautisti")
    public ResponseEntity<Object> getallautisti(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){
        return new ResponseEntity<>(trasportatoriService.getallautisti(nPage,nDimension), HttpStatus.OK);
    }


    @GetMapping("/getautista/{idautista}")
    public ResponseEntity<Object> getautista(@PathVariable long idautista){
        try {
            return new ResponseEntity<>(trasportatoriService.getautista(idautista), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getautistadanome/{nomeautista}")
    public ResponseEntity<Object> getautistadanome(@PathVariable String nomeautista){
        try {
            return new ResponseEntity<>(trasportatoriService.getautista(nomeautista), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/aggiungiautista")
    public ResponseEntity<Object> aggiungiautista(@RequestBody Autista autista){
        try {
            return new ResponseEntity<>(trasportatoriService.aggiungiautista(autista), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/rimuoviautista/{idautista}")
    public ResponseEntity<Object> rimuoviautista(@PathVariable long idautista){
        // todo
        // staccare autista dal trasportatore
        try {
            return new ResponseEntity<>(trasportatoriService.rimuoviautista(idautista), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ******** FINE AUTISTI **********


    @GetMapping("/getrimorchiditrasportatore/{idtrasportatore}")
    public ResponseEntity<Object> getrimorchiditrasportatore(@PathVariable String idtrasportatore){
        try {
            return new ResponseEntity<>(trasportatoriService.getrimorchiditrasportatore(idtrasportatore), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
