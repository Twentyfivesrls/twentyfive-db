package com.twentyfive.twentyfivedb.tictic.controller;

import com.twentyfive.twentyfivedb.tictic.service.AnimalOwnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimalOwner;

@RestController
@RequestMapping("/animal-owner")
public class AnimalOwnerController {

    private final AnimalOwnerService ownerService;

    public AnimalOwnerController(AnimalOwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PostMapping("/create")
    public ResponseEntity<TTAnimalOwner> createOwner(@RequestBody TTAnimalOwner animalOwner){
        return ResponseEntity.ok(ownerService.createOwner(animalOwner));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteOwner(@RequestParam(name = "email") String email){
        ownerService.deleteOwner(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update-owner/{email}")
    public ResponseEntity<Void> updateOwner(@PathVariable String email, @RequestBody TTAnimalOwner animalOwner){
        ownerService.updateOwner(email, animalOwner);
        return ResponseEntity.ok().build();
    }
}

