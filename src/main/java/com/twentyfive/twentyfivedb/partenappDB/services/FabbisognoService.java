package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.FabbisognoRepository;
import com.twentyfive.twentyfivemodel.dto.partenupDto.DateRange;
import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FabbisognoService {
    private final FabbisognoRepository fabbisognoRepository;




    public Page<Fabbisogno> getallfabbisogni(int nPage, int nDimension){
        Pageable pageable= PageRequest.of(nPage, nDimension);
        return fabbisognoRepository.findAllBySmaltitoFalse(pageable);
    }

    public Fabbisogno getfabbisognodaid(long id) throws RuntimeException{
            return fabbisognoRepository.findById(id).orElse(null);
    }

    public boolean aggiungifabbisogno(Fabbisogno fabbisogno) throws RuntimeException{
        try{
            fabbisognoRepository.save(fabbisogno);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }


    public boolean eliminafabbisogno(Fabbisogno fabbisogno)throws RuntimeException{
        try {
            fabbisognoRepository.delete(fabbisogno);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }


    public List<Fabbisogno> getindaterange(DateRange dateRange){
        return fabbisognoRepository.findAllByDataBetween(dateRange.getData1(),dateRange.getData2());
    }
}
