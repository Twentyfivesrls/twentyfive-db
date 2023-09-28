package com.twentyfive.twentyfivedb.testController;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class PublicController {

    @GetMapping("/prova")
    public String prova(){
        return "prova";
    }
}
