package com.twentyfive.twentyfivedb.partenappDB.services;

import com.twentyfive.twentyfivedb.partenappDB.repositories.UtenteRepository;
import com.twentyfive.twentyfivemodel.models.partenupModels.Utente;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UtenteService {
    private final UtenteRepository utenteRepository;

    /*@Autowired
    private BCryptPasswordEncoder encoder;


    public List<Utente> creaUtente(String username, String password, int ruolo){
        String passwordencoded = encoder.encode(password);
        Utente u = new Utente(username,passwordencoded,password,ruolo);
        try{
            utenteRepository.save(u);
            return getallutenti();
        }catch(Exception e){
            e.printStackTrace();
            return getallutenti();
        }
    }


    public List<Utente> getallutenti(){
        return utenteRepository.findAll();
    }


    public Utente getUtenteDaUsername(String username){
        Optional<Utente> usersOptional = utenteRepository.findByUsername(username);
        usersOptional
                .orElseThrow(() -> new UsernameNotFoundException("Username non trovato!"));
        return usersOptional.get();
    }

    public List<Utente> rimuoviutente(Utente utente){
        if(utente.getUsername().equals("admin")) return getallutenti();
        try{
            utenteRepository.delete(utente);
            return getallutenti();
        }catch(Exception e){
            e.printStackTrace();
            return getallutenti();
        }
    }*/
}
