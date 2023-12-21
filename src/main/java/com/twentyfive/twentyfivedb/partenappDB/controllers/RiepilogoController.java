package com.twentyfive.twentyfivedb.partenappDB.controllers;

import com.twentyfive.twentyfivedb.partenappDB.services.RiepilogoService;
import com.twentyfive.twentyfivedb.partenappDB.utils.CalcoliPreventivo;
import com.twentyfive.twentyfivedb.partenappDB.utils.GeneraExcel;
import com.twentyfive.twentyfivemodel.dto.partenupDto.DateRange;
import com.twentyfive.twentyfivemodel.dto.partenupDto.RiepilogoPerFrontEnd;
import com.twentyfive.twentyfivemodel.models.partenupModels.Riepilogo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/riepilogo")
@RequiredArgsConstructor
@Slf4j
public class RiepilogoController {
    private final RiepilogoService riepilogoService;

    private final CalcoliPreventivo calcolipreventivo;

    @PostMapping("/getall")
    public ResponseEntity<Object> getallbydaterange(@RequestBody DateRange range) {
        try{
            List<Riepilogo> lista = riepilogoService.getallriepiloghiindaterange(range);
            List<RiepilogoPerFrontEnd> result = trasformalista(lista);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e){
            log.error("Errore nella getallbydaterange");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/save")
    public ResponseEntity<Object> salva(@RequestBody RiepilogoPerFrontEnd riepilogoPerFrontend){
        try{
            return new ResponseEntity<>(riepilogoService.salvariepilogo(riepilogoPerFrontend), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Errore nel salvataggio del riepilogo");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    public List<RiepilogoPerFrontEnd> trasformalista(List<Riepilogo> lista){
        List<RiepilogoPerFrontEnd> result = new LinkedList<>();

        for(Riepilogo curr : lista){
            result.add(calcolipreventivo.trasformariepilogo(curr));
        }

        return result;
    }



    @PostMapping("/generaexcel")
    public ResponseEntity<Object> esportaexcel(@RequestBody DateRange range) {
        try{
            ResponseEntity<Object> listariepilogo = getallbydaterange(range);
            GeneraExcel generatore = new GeneraExcel((List<RiepilogoPerFrontEnd>) listariepilogo.getBody());
            XSSFWorkbook fileresult = generatore.export();


            ByteArrayOutputStream os = null;

            byte[] result = {};

            try {
                os = new ByteArrayOutputStream();
                fileresult.write(os);
                result = os.toByteArray();
                fileresult.close();
            }catch(Exception e){
                e.printStackTrace();
                try{
                    fileresult.close();
                }catch(Exception ex){

                }
            }


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // Here you have to set the actual filename of your pdf

            headers.setContentDispositionFormData("recap", "recap.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            ResponseEntity<Object> response = new ResponseEntity<>(result, headers, HttpStatus.OK);
            return response;
        } catch (Exception e) {
            log.error("Errore nela generazione dell'excell");
            return new ResponseEntity<>("DB Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }



    }
}
