package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.Atk;
import com.twentyfive.twentyfivemodel.models.partenupModels.Autista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutistaRepository extends JpaRepository<Autista, Long> {
    Page<Autista> findAll (Pageable pageable);
    Optional<Autista> findByIdautista(long idautista);

    Optional<Autista> findByNomeautista(String nomeautista);
}
