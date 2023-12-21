package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import com.twentyfive.twentyfivemodel.models.partenupModels.Preventivo;
import com.twentyfive.twentyfivemodel.models.partenupModels.Riepilogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface RiepilogoRepository extends JpaRepository<Riepilogo,Long> {
    Optional<Riepilogo> findByFabbisogno(Fabbisogno fabbisogno);

    List<Riepilogo> findAllByFabbisogno_DataBetween(Date data1, Date data2);

    Optional<Riepilogo> findByPreventivo(Preventivo preventivo);
}
