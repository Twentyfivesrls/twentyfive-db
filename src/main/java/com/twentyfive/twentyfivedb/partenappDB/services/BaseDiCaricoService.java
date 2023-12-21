package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.BaseDiCaricoRepository;
import com.twentyfive.twentyfivemodel.models.partenupModels.BaseDiCarico;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BaseDiCaricoService {
    private final BaseDiCaricoRepository baseDiCaricoRepository;




    public Page<BaseDiCarico> tuttelebasidicarico(int nPage, int nDimension){
        Pageable pageable= PageRequest.of(nPage, nDimension);
        return baseDiCaricoRepository.findAll(pageable);
    }

    public BaseDiCarico getbasedicarico(String nomebase) throws RuntimeException{
        return baseDiCaricoRepository.findByNomebasedicarico(nomebase).orElse(null);
    }

    public BaseDiCarico getvocedaid(long id) throws RuntimeException{
        return baseDiCaricoRepository.findByIdbasedicarico(id).orElse(null);
    }

    public boolean aggiungibasedicarico(BaseDiCarico baseDiCarico) throws RuntimeException{
        try {
            baseDiCaricoRepository.save(baseDiCarico);
            return true;
        } catch (Exception e){
            throw e;
        }
    }

    public boolean rimuovibasedicarico(String nomebase) throws RuntimeException{
        BaseDiCarico darimuovere = getbasedicarico(nomebase);
        try{
            baseDiCaricoRepository.delete(darimuovere);
            return true;
        }catch(Exception e){
            throw e;
        }
    }

    public boolean rimuovibasedicarico(long idbase) throws RuntimeException{
        BaseDiCarico darimuovere = getvocedaid(idbase);
        try{
            baseDiCaricoRepository.delete(darimuovere);
            return true;
        }catch(Exception e){
            throw e;
        }
    }
}
