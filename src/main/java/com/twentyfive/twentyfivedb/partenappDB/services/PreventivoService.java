package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.FabbisognoRepository;
import com.twentyfive.twentyfivedb.partenappDB.repositories.PreventivoRepository;
import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import com.twentyfive.twentyfivemodel.models.partenupModels.Preventivo;
import com.twentyfive.twentyfivemodel.models.partenupModels.PuntoVendita;
import com.twentyfive.twentyfivemodel.models.partenupModels.VoceDiRettificaConValore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PreventivoService {
    private final PreventivoRepository preventivoRepository;
    private final FabbisognoRepository fabbisognoRepository;

    private final PuntiVenditaService puntiVenditaService;




    public Page<Preventivo> getall(int nPage, int nDimension){
        Pageable pageable= PageRequest.of(nPage,nDimension);
        return preventivoRepository.findAll(pageable);
    }

    public Preventivo getpreventivofromfabbisogno(Fabbisogno f) throws RuntimeException{
        return preventivoRepository.findByRiferimento(f).orElse(null);
    }

    public Preventivo getpreventivo(Preventivo p){
        return preventivoRepository.findById(p.getId()).orElse(null);
    }


    public boolean aggiungimodificapreventivo(Preventivo preventivo) throws RuntimeException{
        try{

            preventivoRepository.save(preventivo);

            preventivo.getRiferimento().setPreventivoesistente(true);

            fabbisognoRepository.save(preventivo.getRiferimento());

            puntiVenditaService.aggiungipuntovendita(preventivo.getRiferimento().getPuntoVendita());

            toglituttelevocidirettifica(preventivo.getRiferimento().getPuntoVendita(),preventivo.getListavocidirettifica());

            return true;
        }catch(Exception e){
            throw e;
        }
    }

    public void toglituttelevocidirettifica(PuntoVendita puntovendita, List<VoceDiRettificaConValore> listavoci){
        for(VoceDiRettificaConValore corr : listavoci){
            puntiVenditaService.cancellavocedirettifica(puntovendita,corr.getId());
        }
    }

    public boolean cancellapreventivo(Preventivo preventivo){
        try{
            PuntoVendita punto = preventivo.getRiferimento().getPuntoVendita();
            List<VoceDiRettificaConValore> listavoci = preventivo.getListavocidirettifica();

            preventivo.getRiferimento().setPreventivoesistente(false);
            preventivoRepository.delete(preventivo);

            ripristinavocidirettifica(punto,listavoci);

            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }


    public void ripristinavocidirettifica(PuntoVendita punto, List<VoceDiRettificaConValore> listavoci){
        for(VoceDiRettificaConValore curr : listavoci){
            puntiVenditaService.aggiungivocedirettifica(punto,curr);
        }
    }


    public boolean cancellavocedirettificadapreventivo(long idpreventivo,VoceDiRettificaConValore voceDiRettificaConValore){
        try {
            Optional<Preventivo> p = preventivoRepository.findById(idpreventivo);
            if (p.isPresent()) {
                p.get().getListavocidirettifica().remove(voceDiRettificaConValore);
                preventivoRepository.save(p.get());
                PuntoVendita pv = p.get().getRiferimento().getPuntoVendita();
                puntiVenditaService.aggiungivocedirettifica(pv, voceDiRettificaConValore);
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
