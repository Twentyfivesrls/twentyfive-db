package com.twentyfive.twentyfivedb.thub.controller;

import com.twentyfive.twentyfivedb.thub.service.ThubProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.thubModels.ThubProfile;

import java.util.List;

@RestController
@RequestMapping("/thubprofile")
public class ThubProfileController {

    private final ThubProfileService profileService;

    public ThubProfileController(ThubProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/getall")
    public ResponseEntity<List<ThubProfile>> getAllProfile() {
        return ResponseEntity.ok(profileService.getAllProfile());
    }

    @PostMapping("/save")
    public ResponseEntity<ThubProfile> saveThubProfile(@RequestBody ThubProfile thubProfile) {
        return ResponseEntity.ok(profileService.saveThubProfile(thubProfile));
    }

    @GetMapping("/getprofile/{username}")
    public ResponseEntity<ThubProfile> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(profileService.getProfile(username));
    }
}
