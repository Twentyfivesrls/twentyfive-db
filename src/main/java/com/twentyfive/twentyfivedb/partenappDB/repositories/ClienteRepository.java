package com.twentyfive.twentyfivedb.partenappDB.repositories;

import com.twentyfive.twentyfivemodel.models.partenupModels.Cliente;
import com.twentyfive.twentyfivemodel.models.partenupModels.PuntoVendita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdcliente(long idcliente);


    Optional<Cliente> deleteByIdcliente(long idcliente);

    Optional<Cliente> findByListapuntivenditaContains(PuntoVendita puntoVendita);

    Optional<Cliente> findByNomecliente(String nome);
    Page<Cliente> findAll(Pageable pageable);
}
