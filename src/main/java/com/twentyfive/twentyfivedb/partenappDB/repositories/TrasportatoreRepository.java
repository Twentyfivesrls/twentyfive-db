package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.Atk;
import com.twentyfive.twentyfivemodel.models.partenupModels.Autista;
import com.twentyfive.twentyfivemodel.models.partenupModels.Rimorchio;
import com.twentyfive.twentyfivemodel.models.partenupModels.Trasportatore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrasportatoreRepository extends JpaRepository<Trasportatore, Long> {
    Page<Trasportatore> findAll(Pageable pageable);

    Optional<Trasportatore> findByNometrasportatore(String nometrasportatore);



    Optional<Trasportatore> findByListaatkContains(Atk atk);

    Optional<Trasportatore> findByListaautistiContains(Autista autista);

    Optional<Trasportatore> findByListarimorchiContains(Rimorchio rimorchio);
}
