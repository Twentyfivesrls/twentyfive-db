package com.twentyfive.twentyfivedb.tictic.controller;

import com.twentyfive.twentyfivedb.tictic.service.AnimalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.tictickModels.TTAnimal;

@RestController
@RequestMapping("/animal")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @PostMapping("/create")
    public ResponseEntity<TTAnimal> createAnimal(@RequestBody TTAnimal animal){
        return ResponseEntity.ok(animalService.createAnimal(animal));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteAnimal(@RequestParam(name = "microchip") String microchip){
        animalService.deleteAnimal(microchip);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update-animal/{microchip}")
    public ResponseEntity<Void> updateOwner(@PathVariable String microchip, @RequestBody TTAnimal animal){
        animalService.updateAnimal(microchip, animal);
        return ResponseEntity.ok().build();
    }
}
