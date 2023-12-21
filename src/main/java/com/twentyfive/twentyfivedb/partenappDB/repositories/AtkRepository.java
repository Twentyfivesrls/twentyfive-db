package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.Atk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AtkRepository extends JpaRepository<Atk,Long> {
    Page<Atk> findAll (Pageable pageable);
    Optional<Atk> findByIdatk(long idatk);

    Optional<Atk> findByCodice(String codice);

    Optional<Atk> findByTarga(String targa);

}
