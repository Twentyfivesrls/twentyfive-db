package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.FornitoreRepository;
import com.twentyfive.twentyfivedb.partenappDB.repositories.QuotazioneRepository;
import com.twentyfive.twentyfivemodel.models.partenupModels.Fornitore;
import com.twentyfive.twentyfivemodel.models.partenupModels.QuotazioneGiornaliera;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornitoreService {
    private final FornitoreRepository fornitoreRepository;


    private final QuotazioneRepository quotazioneRepository;


    public Page<Fornitore> tuttifornitori(int nPage, int nDimension){
        Pageable pageable= PageRequest.of(nPage, nDimension);
        return fornitoreRepository.findAll(pageable);
    }

    public Fornitore getfornitore(String nomefornitore) throws RuntimeException{
        return fornitoreRepository.findByNomefornitore(nomefornitore).orElse(null);
    }

    public Fornitore getfornitoredaid(long id) throws RuntimeException{
        return fornitoreRepository.findByIdfornitore(id).orElse(null);
    }

    public boolean aggiungifornitore(Fornitore fornitore) throws RuntimeException{
        try{
            fornitoreRepository.save(fornitore);
            return true;
        }catch(Exception e){
            throw e;
        }
    }

    public boolean rimuovifornitore(String fornitore) throws RuntimeException{
        Fornitore darimuovere = getfornitore(fornitore);
        try{
            fornitoreRepository.delete(darimuovere);
            return true;
        }catch(Exception e){
            throw e;
        }
    }

    public boolean rimuovifornitore(long idfornitore) throws RuntimeException{
        Fornitore darimuovere = getfornitoredaid(idfornitore);
        try{
            fornitoreRepository.delete(darimuovere);
            return true;
        }catch(Exception e){
            throw e;
        }
    }

    public boolean rimuoviquotazione(QuotazioneGiornaliera quotazioneGiornaliera) throws RuntimeException{
        Fornitore fornitore = fornitoreRepository.findByQuotazioniContains(quotazioneGiornaliera).orElse(null);

        try{
            fornitore.getQuotazioni().remove(quotazioneGiornaliera);
            fornitoreRepository.save(fornitore);
            quotazioneRepository.delete(quotazioneGiornaliera);
            return true;
        }catch(Exception e){
            throw e;
        }
    }

}
