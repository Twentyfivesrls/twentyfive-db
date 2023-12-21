package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.AtkRepository;
import com.twentyfive.twentyfivedb.partenappDB.repositories.AutistaRepository;
import com.twentyfive.twentyfivedb.partenappDB.repositories.RimorchiRepository;
import com.twentyfive.twentyfivedb.partenappDB.repositories.TrasportatoreRepository;
import com.twentyfive.twentyfivemodel.models.partenupModels.Atk;
import com.twentyfive.twentyfivemodel.models.partenupModels.Autista;
import com.twentyfive.twentyfivemodel.models.partenupModels.Rimorchio;
import com.twentyfive.twentyfivemodel.models.partenupModels.Trasportatore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrasportatoriService {
    private final AtkRepository atkRepository;

    private final AutistaRepository autistaRepository;

    private final RimorchiRepository rimorchiRepository;

    private final TrasportatoreRepository trasportatoreRepository;


    // ********* TRASPORTATORI ***********
    public Page<Trasportatore> getalltrasportatori(int nPage, int nDimension){
        Pageable pageable =PageRequest.of(nPage, nDimension);
        return trasportatoreRepository.findAll(pageable);
    }


    public Trasportatore gettrasportatore(String nometrasportatore) throws RuntimeException{
        return trasportatoreRepository.findByNometrasportatore(nometrasportatore).orElse(null);
    }

    public boolean aggiungitrasportatore(Trasportatore trasportatore) throws RuntimeException{
        try{
            trasportatoreRepository.saveAndFlush(trasportatore);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public boolean rimuovitrasportatore(String idtrasportatore) throws RuntimeException{
        Trasportatore darimuovere = gettrasportatore(idtrasportatore);
        try{
            trasportatoreRepository.delete(darimuovere);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    // ******* FINE TRASPORTATORI  **********


    // ******* ATK *********
    public Page<Atk> getallatk(int nPage, int nDimensione){
        Pageable page =PageRequest.of(nPage,nDimensione);
        return atkRepository.findAll(page);
    }

    public Atk getatk(long idatk) throws RuntimeException{
        return atkRepository.findByIdatk(idatk).orElse(null);
    }

    public Atk getatkdacodice(String codice) throws RuntimeException{
        return atkRepository.findByCodice(codice).orElse(null);
    }

    public Atk getatkdatarga(String targa) throws RuntimeException{
        return atkRepository.findByTarga(targa).orElse(null);
    }

    public boolean aggiungiatk(Atk atk) throws RuntimeException{
        try{
            atkRepository.saveAndFlush(atk);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public boolean rimuoviatk(long idatk) throws RuntimeException{

        try {

            Atk darimuovere = getatk(idatk);

            Trasportatore trasp = trasportatoreRepository.findByListaatkContains(darimuovere).orElse(null);
            trasp.getListaatk().remove(darimuovere);
            trasportatoreRepository.save(trasp);

            return rimuoviatk(darimuovere);
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public boolean rimuoviatkdacodice(String codiceatk) throws RuntimeException{
        Atk darimuovere = getatkdacodice(codiceatk);
        return rimuoviatk(darimuovere);
    }

    public boolean rimuoviatkdatarga(String targa) throws RuntimeException{
        Atk darimuovere = getatkdatarga(targa);
        return rimuoviatk(darimuovere);
    }



    public boolean rimuoviatk(Atk atk) throws RuntimeException{
        try{
            atkRepository.delete(atk);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    // ******** FINE ATK **********


    // ******** AUTISTI *********

    public Page<Autista> getallautisti(int nPage, int nDimension){
        Pageable page =PageRequest.of(nPage, nDimension);
        return autistaRepository.findAll(page);
    }

    public Autista getautista(long idautista) throws RuntimeException{
        return autistaRepository.findByIdautista(idautista).orElse(null);
    }

    public Autista getautista(String nomeautista) throws RuntimeException{
        return autistaRepository.findByNomeautista(nomeautista).orElse(null);
    }

    public boolean aggiungiautista(Autista autista) throws RuntimeException{
        try{
            autistaRepository.saveAndFlush(autista);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public boolean rimuoviautista(long idautista) throws RuntimeException{
        try {
            Autista darimuovere = getautista(idautista);
            Trasportatore trasp = trasportatoreRepository.findByListaautistiContains(darimuovere).orElse(null);
            trasp.getListaautisti().remove(darimuovere);
            trasportatoreRepository.save(trasp);
            return rimuoviautista(darimuovere);
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public boolean rimuoviautista(String nomeautista) throws RuntimeException{
        Autista darimuovere = getautista(nomeautista);
        return rimuoviautista(darimuovere);
    }

    public boolean rimuoviautista(Autista autista) throws RuntimeException{
        try{
            autistaRepository.delete(autista);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // ******* FINE AUTISTI ********


    // ******* RIMORCHI ***********

    public Page<Rimorchio> getallrimorchi(int nPage, int nDimension){
        Pageable pageable=PageRequest.of(nPage, nDimension);
        return rimorchiRepository.findAll(pageable);
    }

    public List<Rimorchio> getrimorchiditrasportatore(String idtrasportatore) {
        Trasportatore t = gettrasportatore(idtrasportatore);
        return t.getListarimorchi();
    }

    public Rimorchio getrimorchio(String targa) throws RuntimeException{
        return rimorchiRepository.findByTarga(targa).orElse(null);
    }

    public Rimorchio aggiungirimorchio(Rimorchio rimorchio) throws RuntimeException{
        try{
            Rimorchio result = rimorchiRepository.saveAndFlush(rimorchio);
            return result;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }



    public boolean rimuovirimorchio(String targa) throws RuntimeException{
        try {
            Rimorchio darimuovere = getrimorchio(targa);

            Trasportatore trasp = trasportatoreRepository.findByListarimorchiContains(darimuovere).orElse(null);
            trasp.getListaautisti().remove(darimuovere);
            trasportatoreRepository.save(trasp);

            return rimuovirimorchio(darimuovere);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean rimuovirimorchio(Rimorchio rimorchio) throws RuntimeException{
        try{
            rimorchiRepository.delete(rimorchio);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // ********* FINE RIMORCHI **************
}
