package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.TrasportoRepository;
import com.twentyfive.twentyfivemodel.dto.partenupDto.TrasportoFilter;
import com.twentyfive.twentyfivemodel.dto.partenupDto.Viaggio;
import com.twentyfive.twentyfivemodel.models.partenupModels.Atk;
import com.twentyfive.twentyfivemodel.models.partenupModels.BaseDiCarico;
import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import com.twentyfive.twentyfivemodel.models.partenupModels.Trasporto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrasportoService {
    private final TrasportoRepository trasportoRepository;

    private final FabbisognoService fabbisognoService;



    public Page<Trasporto> getalltrasporti(int nPage, int nDimension){
        Pageable pageable= PageRequest.of(nPage, nDimension);
        return trasportoRepository.findAll(pageable);
    }

    public Trasporto getbyid(long id) throws RuntimeException{
        return trasportoRepository.findById(id).orElse(null);
    }

    public Trasporto getbyfabbisogno(Fabbisogno fabbisogno) throws RuntimeException{
        return trasportoRepository.findByFabbisogno(fabbisogno).orElse(null);
    }

    public Trasporto getbyfabbisognoid(long idfabbisogno) throws RuntimeException{
        Fabbisogno fabbisogno = fabbisognoService.getfabbisognodaid(idfabbisogno);
        return getbyfabbisogno(fabbisogno);
    }

    public boolean salva(Trasporto trasporto) throws RuntimeException{
        try{
            trasporto.getFabbisogno().setTrasportoesistente(true);
            fabbisognoService.aggiungifabbisogno(trasporto.getFabbisogno());
            trasportoRepository.save(trasporto);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }


    public boolean rimuovi(Trasporto trasporto) throws RuntimeException{
        try{
            trasporto.getFabbisogno().setTrasportoesistente(false);
            trasportoRepository.delete(trasporto);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public Set<Viaggio> findbyfilter(TrasportoFilter filter){
        if(filter.getData() == null) return new HashSet<>();
        if(filter.getAtk() == null && filter.getBaseDiCarico() == null) return findallviaggiindata(filter.getData());
        if(filter.getBaseDiCarico() == null) return findallviaggidiatkindata(filter.getAtk(),filter.getData());
        if(filter.getAtk() == null) return findallviaggindataconbasedicarico(filter.getBaseDiCarico(),filter.getData());
        Set<Trasporto> result = new HashSet<>(findallbyatkanddateandbasedicarico(filter.getAtk(),filter.getData(),filter.getBaseDiCarico()));
        Viaggio darestituire = new Viaggio();
        darestituire.setListaviaggi(result);
        Set<Viaggio> res = new HashSet<>();
        if(darestituire.getListaviaggi().size() > 0) {
            res.add(darestituire);
        }
        return res;
    }



    public List<Trasporto> findbyatk(Atk atk){
        return trasportoRepository.findAllByAtk(atk);
    }

    public List<Trasporto> findbydata(Date data){
        return trasportoRepository.findAllByDatadicaricazione(data);
    }


    public List<Trasporto> findbybasedicarico(BaseDiCarico baseDiCarico){
        return trasportoRepository.findAllByFabbisogno_Basedicarico(baseDiCarico);
    }

    public List<Trasporto> findallbyatkanddate(Atk atk, Date data){
        return trasportoRepository.findAllByAtkAndDatadicaricazione(atk,data);
    }

    public List<Trasporto> findallbyatkandbasedicarico(Atk atk, BaseDiCarico baseDiCarico){
        return trasportoRepository.findAllByAtkAndFabbisogno_Basedicarico(atk,baseDiCarico);
    }

    public List<Trasporto> findallbyatkanddateandbasedicarico(Atk atk, Date data, BaseDiCarico baseDiCarico){
        List<Trasporto> result = trasportoRepository.findAllByAtkAndDatadicaricazioneAndFabbisogno_Basedicarico(atk, data, baseDiCarico);
        return result;
    }

    public Set<Viaggio> findallviaggindataconbasedicarico(BaseDiCarico base, Date data){
        Set<Atk> atkdiversi = prendilistadiatkindata(data);
        Set<Viaggio> result = new HashSet<>();
        for(Atk atk : atkdiversi){
            Viaggio daaggiungere = new Viaggio();
            daaggiungere.setListaviaggi(new HashSet<>(findallbyatkanddateandbasedicarico(atk,data,base)));
            if(daaggiungere.getListaviaggi().size() > 0) {
                result.add(daaggiungere);
            }
        }
        return result;
    }


    public Set<Viaggio> findallviaggidiatkindata(Atk atk,Date data){
        Set<BaseDiCarico> basidicarico = prendibasidicaricodiverseindata(data);
        Set<Viaggio> result = new HashSet<>();
        for(BaseDiCarico base : basidicarico){
            Viaggio daaggiungere = new Viaggio();
            daaggiungere.setListaviaggi(new HashSet<>(findallbyatkanddateandbasedicarico(atk,data,base)));
            if(daaggiungere.getListaviaggi().size() > 0) {
                result.add(daaggiungere);
            }
        }
        return result;
    }

    public Set<Viaggio> findallviaggiindata(Date data){
        Set<Atk> atkdiversi = prendilistadiatkindata(data);
        Set<BaseDiCarico> basidicarico = prendibasidicaricodiverseindata(data);

        Set<Viaggio> result = new HashSet<>();

        for(Atk atk : atkdiversi){
            for(BaseDiCarico base : basidicarico){
                Viaggio daaggiungere = new Viaggio();
                daaggiungere.setListaviaggi(new HashSet<>(findallbyatkanddateandbasedicarico(atk,data,base)));
                if(daaggiungere.getListaviaggi().size() > 0) {
                    result.add(daaggiungere);
                }
            }
        }
        return result;
    }

    private Set<BaseDiCarico> prendibasidicaricodiverseindata(Date data) {
        Set<BaseDiCarico> result = new HashSet<>();
        List<Trasporto> listatotale = findbydata(data);

        for(Trasporto t : listatotale){
            result.add(t.getFabbisogno().getBasedicarico());
        }
        return result;
    }

    private Set<Atk> prendilistadiatkindata(Date data) {
        Set<Atk> result = new HashSet<>();
        List<Trasporto> listatotale = findbydata(data);

        for(Trasporto t : listatotale){
            result.add(t.getAtk());
        }
        return result;
    }
}
