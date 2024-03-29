package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.FidelityProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.fidelityModels.ProfileFidelity;

@RestController
@RequestMapping("/profile")
public class FidelityProfileController {

    private final FidelityProfileService profileService;

    public FidelityProfileController(FidelityProfileService profileService) {
        this.profileService = profileService;
    }


    @PostMapping("/create")
    public ResponseEntity<ProfileFidelity> createImageName(@RequestBody ProfileFidelity imageName){
        return ResponseEntity.ok(profileService.createImageName(imageName));
    }

    @GetMapping("/name-image")
    public ResponseEntity<ProfileFidelity> getImageName(@RequestParam(name = "ownerId") String ownerId){
        return ResponseEntity.ok(profileService.getImageName(ownerId));
    }
}
