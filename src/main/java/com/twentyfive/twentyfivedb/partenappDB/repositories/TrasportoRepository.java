package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.Atk;
import com.twentyfive.twentyfivemodel.models.partenupModels.BaseDiCarico;
import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import com.twentyfive.twentyfivemodel.models.partenupModels.Trasporto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TrasportoRepository extends JpaRepository<Trasporto,Long> {
    Page<Trasporto> findAll(Pageable pageable);
    Optional<Trasporto> findById(long id);

    Optional<Trasporto> findByFabbisogno(Fabbisogno fabbisogno);

    List<Trasporto> findAllByAtk(Atk atk);

    List<Trasporto> findAllByDatadicaricazione(Date datadicaricazione);

    List<Trasporto> findAllByFabbisogno_Basedicarico(BaseDiCarico baseDiCarico);

    List<Trasporto> findAllByAtkAndDatadicaricazione(Atk atk, Date datadicaricazione);

    List<Trasporto> findAllByAtkAndFabbisogno_Basedicarico(Atk atk, BaseDiCarico baseDiCarico);

    List<Trasporto> findAllByAtkAndDatadicaricazioneAndFabbisogno_Basedicarico(Atk atk, Date datadicaricazione, BaseDiCarico basedicarico);

}
