package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.BaseDiCarico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaseDiCaricoRepository extends JpaRepository<BaseDiCarico,Long> {
    Page<BaseDiCarico> findAll(Pageable pageable);
    Optional<BaseDiCarico> findByIdbasedicarico(long idbasedicarico);

    Optional<BaseDiCarico> findByNomebasedicarico(String nomebasedicarico);
}
