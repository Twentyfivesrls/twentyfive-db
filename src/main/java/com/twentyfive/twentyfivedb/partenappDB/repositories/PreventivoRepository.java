package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.Fabbisogno;
import com.twentyfive.twentyfivemodel.models.partenupModels.Preventivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreventivoRepository extends JpaRepository<Preventivo, Long> {
    Page<Preventivo> findAll(Pageable pageable);
    Optional<Preventivo> findByRiferimento(Fabbisogno riferimento);

    Optional<Preventivo> findById(long id);
}
