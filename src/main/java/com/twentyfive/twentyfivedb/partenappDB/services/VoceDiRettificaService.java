package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.VoceDiRettificaRepository;
import com.twentyfive.twentyfivemodel.models.partenupModels.VoceDiRettifica;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoceDiRettificaService {
    private final VoceDiRettificaRepository voceDiRettificaRepository;


    public Page<VoceDiRettifica> tuttelevocidirettifica(int nPage, int nDimension){
        Pageable pageable= PageRequest.of(nPage, nDimension);
        return voceDiRettificaRepository.findAll(pageable);
    }

    public VoceDiRettifica getvocedirettifica(String nomevoce) throws RuntimeException{
        return voceDiRettificaRepository.findByNomevoce(nomevoce).orElse(null);
    }

    public VoceDiRettifica getvocedaid(long id) throws RuntimeException{
        return voceDiRettificaRepository.findByIdvocedirettifica(id).orElse(null);
    }

    public boolean aggiungivocedirettifica(VoceDiRettifica voceDiRettifica){
        try{
            voceDiRettificaRepository.save(voceDiRettifica);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public boolean rimuovivocedirettifica(String nomevoce){
        VoceDiRettifica darimuovere = getvocedirettifica(nomevoce);
        try{
            voceDiRettificaRepository.delete(darimuovere);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public boolean rimuovivocedirettifica(long idvoce) throws RuntimeException{
        VoceDiRettifica darimuovere = getvocedaid(idvoce);
        try{
            voceDiRettificaRepository.delete(darimuovere);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}
