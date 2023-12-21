package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.ClienteService;
import com.twentyfive.twentyfivemodel.models.partenupModels.Cliente;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/cliente")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {
    private final ClienteService clienteService;


    @GetMapping("/getall")
    public ResponseEntity<Object> getallclienti(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){
        return new ResponseEntity<>(clienteService.getallclienti(nPage, nDimension), HttpStatus.OK);
    }

    @PostMapping("/aggiungi")
    public ResponseEntity<Object> aggiungicliente(@RequestBody Cliente cliente){
        try {
            return new ResponseEntity<>(clienteService.aggiungicliente(cliente), HttpStatus.OK);
        } catch(Exception e){
            log.error("Problemi ad aggiungere il cliente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getcliente/{idcliente}")
    public ResponseEntity<Object> getcliente(@PathVariable long idcliente){
        try {
            return new ResponseEntity<>(clienteService.getclientedaid(idcliente), HttpStatus.OK);
        }catch(Exception e){
            log.error("Cliente non esistente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/aggiornacliente")
    public ResponseEntity<Object> aggiornacliente(@RequestBody Cliente cliente) {
        try {
            return new ResponseEntity<>(clienteService.updatecliente(cliente), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Cliente non esistente");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/eliminacliente/{idcliente}")
    public ResponseEntity<Object> eliminacliente(@PathVariable long idcliente){
        try {
            return new ResponseEntity<>(clienteService.eliminacliente(idcliente), HttpStatus.OK);
        } catch(Exception e){
            log.error("Cliente non presente in db");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
