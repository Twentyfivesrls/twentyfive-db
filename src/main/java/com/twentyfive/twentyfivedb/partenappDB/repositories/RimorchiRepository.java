package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.Rimorchio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RimorchiRepository extends JpaRepository<Rimorchio, Long> {
    Page<Rimorchio> findAll(Pageable pageable);
    Optional<Rimorchio> findByTarga(String targa);
}
