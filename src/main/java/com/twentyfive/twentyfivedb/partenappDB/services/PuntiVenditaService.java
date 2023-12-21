package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.ClienteRepository;
import com.twentyfive.twentyfivedb.partenappDB.repositories.PuntiVenditaRepository;
import com.twentyfive.twentyfivedb.partenappDB.repositories.VoceDiRettificaConValoreRepository;
import com.twentyfive.twentyfivemodel.models.partenupModels.Cliente;
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
public class PuntiVenditaService {
    private final PuntiVenditaRepository puntiVenditaRepository;

    private final ClienteRepository clienteRepository;

    private final VoceDiRettificaService voceDiRettificaService;

    private final VoceDiRettificaConValoreRepository voceDiRettificaConValoreRepository;


    public PuntoVendita getpuntovenditadaid(long id) throws RuntimeException{
        return puntiVenditaRepository.findByIdpunto(id).orElse(null);
    }

    public boolean aggiungipuntovendita(PuntoVendita puntoVendita) throws RuntimeException{
        try{
            puntiVenditaRepository.saveAndFlush(puntoVendita);
            return true;
        }catch(Exception e){
            throw e;
        }
    }

    public Page<PuntoVendita> getallpuntivendita(int nPage, int nDimension){
        Pageable pageable= PageRequest.of(nPage, nDimension);
        return puntiVenditaRepository.findAll(pageable);
    }

    public List<PuntoVendita> getallpuntivenditawithoutpage(){
        return puntiVenditaRepository.findAll();
    }

    public boolean deletepuntovendita(long idpuntovendita) throws RuntimeException{
        try{
            PuntoVendita daeliminare = getpuntovenditadaid(idpuntovendita);
            return deletepuntovendita(daeliminare);
        } catch (Exception e) {
            throw e;
        }

    }

    public boolean deletepuntovendita(PuntoVendita puntoVendita) throws RuntimeException{
        try {

            eliminatuttelevocidirettificadiunpuntovendita(puntoVendita);
            puntoVendita.getListavocidirettifica().clear();


            Cliente c = cercaproprietario(puntoVendita);
            if(c != null) {
                c.getListapuntivendita().remove(puntoVendita);
                clienteRepository.save(c);
            }


            puntiVenditaRepository.delete(puntoVendita);
            return true;
        }catch(Exception e){
            throw e;
        }
    }


    public void eliminatuttelevocidirettificadiunpuntovendita(PuntoVendita p){
        for(VoceDiRettificaConValore voce : p.getListavocidirettifica()){
            voceDiRettificaConValoreRepository.delete(voce);
        }
    }


    public Cliente cercaproprietario(PuntoVendita puntovendita) throws RuntimeException{
        return clienteRepository.findByListapuntivenditaContains(puntovendita).orElse(null);
    }


    public PuntoVendita cancellavocedirettifica(PuntoVendita puntoVendita, long idvocedirettifica) throws RuntimeException{
        for(VoceDiRettificaConValore voce : puntoVendita.getListavocidirettifica()){
            if(voce.getId() == idvocedirettifica){
                puntoVendita.getListavocidirettifica().remove(voce);
                break;
            }
        }
        try {
            puntiVenditaRepository.save(puntoVendita);
        }catch(Exception e){
            throw e;
        }
        return puntoVendita;
    }


    public PuntoVendita aggiungivocedirettifica(long idpuntovendita, VoceDiRettificaConValore voceDiRettificaConValore) throws RuntimeException{
        Optional<PuntoVendita> puntovendita = puntiVenditaRepository.findByIdpunto(idpuntovendita);
        if(puntovendita.isPresent())
            return aggiungivocedirettifica(puntovendita.get(),voceDiRettificaConValore);
        return null;
    }

    public PuntoVendita aggiungivocedirettifica(PuntoVendita puntoVendita, VoceDiRettificaConValore voceDiRettificaConValore) throws RuntimeException{
        try{
            puntoVendita.getListavocidirettifica().add(voceDiRettificaConValore);
            puntiVenditaRepository.save(puntoVendita);
        }catch(Exception e){
            throw e;
        }
        return puntoVendita;
    }
}
