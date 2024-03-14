package com.twentyfive.twentyfivedb.fidelity.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    private static final String UPLOAD_DIR = "uploads/";


    /*spring.servlet.multipart.enabled=true
    upload.dir=uploads/
     */
    @PostMapping("/upload")
    public String uploadImage(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return "Nessun file caricato.";
        }

        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            Files.write(path, bytes);
            return "File uploaded successfully.";
        } catch (IOException e) {
            e.printStackTrace();
            return "Errore durante il caricamento del file.";
        }
    }
}
