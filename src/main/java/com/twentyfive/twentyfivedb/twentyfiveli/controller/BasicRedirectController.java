package com.twentyfive.twentyfivedb.twentyfiveli.controller;

import org.springframework.ui.Model;
import com.twentyfive.twentyfivedb.twentyfiveli.service.ShortenLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;

@Controller
public class BasicRedirectController {

    @Autowired
    private ShortenLinkService shortenLinkService;

    @GetMapping("{shortenUrl}")
    public String redirect(@PathVariable String shortenUrl) {
        String destinationUrl = shortenLinkService.getCompleteShortenLink(shortenUrl);

        try{
            URI uri = new URI(destinationUrl);
            if(uri.getScheme() == null){
                destinationUrl = "http://" + destinationUrl;
            }
            return "redirect:" + destinationUrl;
        } catch (Exception e){
            return "redirect:/";
        }

    }
}
