package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.ClienteService;
import com.twentyfive.twentyfivedb.partenappDB.services.PuntiVenditaService;
import com.twentyfive.twentyfivemodel.dto.partenupDto.PuntoVenditaConCliente;
import com.twentyfive.twentyfivemodel.models.partenupModels.Cliente;
import com.twentyfive.twentyfivemodel.models.partenupModels.PuntoVendita;
import com.twentyfive.twentyfivemodel.models.partenupModels.VoceDiRettificaConValore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/puntivendita")
@RequiredArgsConstructor
@Slf4j
public class PuntiVenditaController {
    private final PuntiVenditaService puntiVenditaService;

    private final ClienteService clienteService;



    @GetMapping("/getall")
    public ResponseEntity<Object> getallpuntivendita(@RequestParam("nPage")int nPage, @RequestParam("nDimension")int nDimension){

        return new ResponseEntity<>(puntiVenditaService.getallpuntivendita(nPage, nDimension), HttpStatus.OK);
    }




    @GetMapping("/getpuntovendita/{id}")
    public ResponseEntity<Object> getpuntovendita(@PathVariable long id){
        try{
            return new ResponseEntity<>(puntiVenditaService.getpuntovenditadaid(id), HttpStatus.OK);
        } catch (Exception e){
            log.error("Errore nella ricerca del punto vendita");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @PostMapping("/aggiungipuntovendita")
    public ResponseEntity<Object> aggiungipuntovendita(@RequestBody PuntoVendita puntoVendita){
        try{
            return new ResponseEntity<>(puntiVenditaService.aggiungipuntovendita(puntoVendita), HttpStatus.OK);
        } catch (Exception e){
            log.error("Errore nell'aggiunta del punto vendita");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @GetMapping("/eliminapuntovendita/{idpuntovendita}")
    public ResponseEntity<Object> elimminapuntovendita(@PathVariable long idpuntovendita){
        try{
            puntiVenditaService.deletepuntovendita(idpuntovendita);
            return tuttipuntivenditaconcliente();
        } catch (Exception e){
            log.error("Errore nell'eliminazione del punto vendita");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @GetMapping("/tuttipuntivenditavuoti")
    public ResponseEntity<Object> tuttipuntivenditaascavallo(){
        try{
            return new ResponseEntity<>(clienteService.getallpuntivenditasenzacliente(), HttpStatus.OK);
        }  catch (Exception e){
            log.error("Errore nella ricerca dei punti vendita");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @GetMapping("/tuttipuntivenditaconcliente")
    public ResponseEntity<Object> tuttipuntivenditaconcliente(){
        try {
            List<PuntoVendita> tuttiquelliascavallo = (List<PuntoVendita>) tuttipuntivenditaascavallo().getBody();

            List<Cliente> listaclienti = clienteService.getallclientiwithoutpage();

            List<PuntoVenditaConCliente> result = new LinkedList<>();

            // AGGIUNGO TUTTI I PUNTI VENDITA A SCAVALLO
            for (PuntoVendita scavallo : tuttiquelliascavallo) {
                PuntoVenditaConCliente nuovo = new PuntoVenditaConCliente();
                nuovo.setPuntovendita(scavallo);
                result.add(nuovo);
            }

            // AGGIUNGO TUTTI I PUNTI VENDITA CON IL CLIENTE
            for (Cliente c : listaclienti) {
                for (PuntoVendita corr : c.getListapuntivendita()) {
                    PuntoVenditaConCliente nuovo = new PuntoVenditaConCliente();
                    nuovo.setPuntovendita(corr);
                    nuovo.setNomecliente(c.getNomecliente());
                    result.add(nuovo);
                }
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch (Exception e){
            log.error("Errore nella ricerca dei punti vendita");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @PostMapping("/cercaproprietario")
    public ResponseEntity<Object> cercaproprietario(@RequestBody PuntoVendita puntoVendita){
        try{
            return new ResponseEntity<>(puntiVenditaService.cercaproprietario(puntoVendita), HttpStatus.OK);
        } catch (Exception e){
            log.error("Errore nella ricerca del proprietario");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/cancellavocedirettifica/{idvoce}")
    public ResponseEntity<Object> cancellavocedirettifica(@RequestBody PuntoVendita puntoVendita,@PathVariable long idvoce){
        try{
            return new ResponseEntity<>(puntiVenditaService.cancellavocedirettifica(puntoVendita,idvoce), HttpStatus.OK);
        } catch (Exception e){
            log.error("Errore nella cancellazione della voce di rettifica");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/aggiungivocedirettifica/{idpunto}")
    public ResponseEntity<Object> aggiungivocedirettifica(@RequestBody VoceDiRettificaConValore voceDiRettificaConValore,@PathVariable long idpunto){
        try{
            return new ResponseEntity<>(puntiVenditaService.aggiungivocedirettifica(idpunto,voceDiRettificaConValore), HttpStatus.OK);
        } catch (Exception e){
            log.error("Errore nell'aggiunta della voce di rettifica");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
