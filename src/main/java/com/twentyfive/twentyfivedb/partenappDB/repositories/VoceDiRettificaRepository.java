package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.VoceDiRettifica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoceDiRettificaRepository extends JpaRepository<VoceDiRettifica, Long> {
    Page<VoceDiRettifica> findAll(Pageable pageable);
    Optional<VoceDiRettifica> findByNomevoce(String nomevoce);

    Optional<VoceDiRettifica> findByIdvocedirettifica(long id);
}
