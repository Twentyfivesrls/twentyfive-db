package com.twentyfive.twentyfivedb.linktreeDB.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/testLinkTree")
public class TestController {

    @GetMapping("/prova")
    public ResponseEntity<String> hello(){return ResponseEntity.ok().body("test");}
}
