package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface FabbisognoRepository extends JpaRepository<Fabbisogno, Long> {
    Page<Fabbisogno> findAll(Pageable pageable);
    Page<Fabbisogno> findAllBySmaltitoFalse(Pageable pageable);

    List<Fabbisogno> findAllByDataBetween(Date data1, Date data2);
}
