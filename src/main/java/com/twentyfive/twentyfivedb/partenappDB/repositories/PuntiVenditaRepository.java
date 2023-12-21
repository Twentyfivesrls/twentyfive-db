package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.PuntoVendita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PuntiVenditaRepository extends JpaRepository<PuntoVendita, Long> {
    Optional<PuntoVendita> findByIdpunto(long id);
    Page<PuntoVendita> findAll(Pageable pageable);
}
