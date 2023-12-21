package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.ClienteRepository;
import com.twentyfive.twentyfivemodel.models.partenupModels.Cliente;
import com.twentyfive.twentyfivemodel.models.partenupModels.PuntoVendita;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;


    private final PuntiVenditaService puntiVenditaService;


    public Cliente aggiungicliente(Cliente cliente) throws RuntimeException{
        try{
            aggiungipuntivenditafdicliente(cliente.getListapuntivendita());
            return clienteRepository.saveAndFlush(cliente);
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }


    public Cliente getclientedaid(long idcliente) throws RuntimeException{
        return clienteRepository.findByIdcliente(idcliente).orElse(null);
    }


    public List<PuntoVendita> getpuntivenditadiattivita(long idcliente) throws RuntimeException{
        Cliente cliente = getclientedaid(idcliente);
        return cliente.getListapuntivendita();
    }


    public Page<Cliente> getallclienti(int nPage, int nDimension){
        Pageable pageable= PageRequest.of(nPage, nDimension);
        return clienteRepository.findAll(pageable);
    }

    public List<Cliente> getallclientiwithoutpage(){
        return clienteRepository.findAll();
    }


    public Cliente updatecliente(Cliente cliente) throws RuntimeException{
        return aggiungicliente(cliente);
    }


    public void aggiungipuntivenditafdicliente(List<PuntoVendita> listapuntivendita) throws RuntimeException{
        for(PuntoVendita p : listapuntivendita){
            try {
                puntiVenditaService.aggiungipuntovendita(p);
            }catch(Exception e){
                System.out.println("CLIENTE GIA' ESISTENTE");
                throw e;
            }
        }
    }

    public List<PuntoVendita> getallpuntivenditasenzacliente() throws RuntimeException{
        List<Cliente> tutticlienti = clienteRepository.findAll();
        List<PuntoVendita> tuttipuntivendita = puntiVenditaService.getallpuntivenditawithoutpage();
        for(Cliente c : tutticlienti){
            tuttipuntivendita.removeAll(c.getListapuntivendita());
        }
        return tuttipuntivendita;
    }

    @Transactional
    public boolean eliminacliente(long idcliente) throws RuntimeException{
        try {
            Cliente curr = clienteRepository.findByIdcliente(idcliente).orElse(null);
            curr.setListapuntivendita(new LinkedList<>());
            clienteRepository.delete(curr);
            return true;
        }catch(Exception e){
            throw e;
        }
    }


    public Cliente findbynome(String nomecliente) throws RuntimeException{
        return clienteRepository.findByNomecliente(nomecliente).orElse(null);
    }

}
