package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.RiepilogoRepository;
import com.twentyfive.twentyfivemodel.dto.partenupDto.DateRange;
import com.twentyfive.twentyfivemodel.dto.partenupDto.RiepilogoPerFrontEnd;
import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import com.twentyfive.twentyfivemodel.models.partenupModels.Preventivo;
import com.twentyfive.twentyfivemodel.models.partenupModels.Riepilogo;
import com.twentyfive.twentyfivemodel.models.partenupModels.Trasporto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiepilogoService {
    private final FabbisognoService fabbisognoService;

    private final RiepilogoRepository riepilogoRepository;

    private final PreventivoService preventivoService;

    private final TrasportoService trasportoService;


    public List<Riepilogo> getallriepiloghiindaterange(DateRange range) throws Exception {
        List<Fabbisogno> listainrange = fabbisognoService.getindaterange(range);
        creariepiloghimancanti(listainrange);
        return riepilogoRepository.findAllByFabbisogno_DataBetween(range.getData1(),range.getData2());
    }

    public boolean salvariepilogo(RiepilogoPerFrontEnd riepilogo) throws RuntimeException{
        try {
            if(riepilogo.getDas() == null || riepilogo.getDas().isEmpty()) {
            } else {
                Fabbisogno fabbisogno = riepilogo.getFabbisogno();
                fabbisogno.setSmaltito(true);
                fabbisognoService.aggiungifabbisogno(fabbisogno);
            }
            riepilogoRepository.save(riepilogo.toriepilogo());
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }


    private void creariepiloghimancanti(List<Fabbisogno> trovati) throws Exception {
        for(Fabbisogno curr: trovati){

            if(!curr.isSmaltito()) {
                Riepilogo riep = riepilogoRepository.findByFabbisogno(curr).orElse(null);
                if (riep == null) {
                    creariepilogodafabbisogno(curr);
                }
            }

        }
    }


    private boolean creariepilogodafabbisogno(Fabbisogno fabbisogno) throws Exception {
        Preventivo preventivo = preventivoService.getpreventivofromfabbisogno(fabbisogno);
        Trasporto trasporto = trasportoService.getbyfabbisogno(fabbisogno);
        if(preventivo == null || trasporto == null) return false;
        Riepilogo dacreare = new Riepilogo(fabbisogno,trasporto,preventivo);
        try{
            riepilogoRepository.save(dacreare);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public boolean cancellariepilogodafabbisogno(Fabbisogno fabbisogno) throws RuntimeException{
        Riepilogo riepilogo = riepilogoRepository.findByFabbisogno(fabbisogno).orElse(null);
        return cancellariepilogo(riepilogo);
    }


    public boolean cancellariepilogo(Preventivo preventivo) throws RuntimeException{
        Riepilogo riepilogo = riepilogoRepository.findByPreventivo(preventivo).orElse(null);
        if(riepilogo == null) return false;
        return cancellariepilogo(riepilogo);
    }


    public boolean cancellariepilogo(Riepilogo riepilogo) throws RuntimeException{
        try{
            riepilogoRepository.delete(riepilogo);
            return true;
        }catch(Exception e){
            throw e;
        }
    }
}
