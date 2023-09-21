package com.twentyfive.twentyfivedb.ticketDB.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestClass {

    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }
}
