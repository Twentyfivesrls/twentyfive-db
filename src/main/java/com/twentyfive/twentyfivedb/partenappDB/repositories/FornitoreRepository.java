package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.Fornitore;
import com.twentyfive.twentyfivemodel.models.partenupModels.QuotazioneGiornaliera;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FornitoreRepository extends JpaRepository<Fornitore, Long> {
    Page<Fornitore> findAll(Pageable pageable);
    Optional<Fornitore> findByNomefornitore(String nomefornitore);

    Optional<Fornitore> findByIdfornitore(long idfornitore);

    Optional<Fornitore> findByQuotazioniContains(QuotazioneGiornaliera quotazioneGiornaliera);
}
