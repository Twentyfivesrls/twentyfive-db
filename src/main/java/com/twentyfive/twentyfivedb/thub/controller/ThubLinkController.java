package com.twentyfive.twentyfivedb.thub.controller;

import com.twentyfive.twentyfivedb.thub.service.ThubLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.models.thubModels.ThubLink;

import java.util.List;

@RestController
@RequestMapping("/thublink")
public class ThubLinkController {

    private final ThubLinkService thubLinkService;
    public ThubLinkController(ThubLinkService thubLinkService) {
        this.thubLinkService = thubLinkService;
    }

    @GetMapping("/getlinks/{username}")
    private ResponseEntity<List<ThubLink>> getProfileLinks(@PathVariable String username) {
        List<ThubLink> l = thubLinkService.getProfileLinks(username);
        return ResponseEntity.ok(l);
    }


//fare un dto che ha username e lista links
//    SaveLinkRequest{
//        private String username;
//        private List<ThubLink> links;
//    }

    @PutMapping("/savelinks/{username}")
    private  ResponseEntity<List<ThubLink>> saveLinks(@PathVariable String username, @RequestBody List<ThubLink> links) {
        return ResponseEntity.ok(thubLinkService.saveLinks(username,links));
    }

    @PostMapping("/addlink/{username}")
    private ResponseEntity<List<ThubLink>> addLink(@PathVariable String username, @RequestBody ThubLink link) {
        return ResponseEntity.ok(thubLinkService.addLink(username, link));
    }

    @PutMapping("/updatelink/{username}")
    private ResponseEntity<List<ThubLink>> updateLink(@PathVariable String username, @RequestBody ThubLink link) {
        return ResponseEntity.ok(thubLinkService.updateLink(username, link));
    }

    @DeleteMapping("/deletelink/{username}")
    private ResponseEntity<List<ThubLink>> deleteLink(@PathVariable String username, @RequestParam("linkId") String linkId) {
        return ResponseEntity.ok(thubLinkService.deleteLink(username, linkId));
    }
}
