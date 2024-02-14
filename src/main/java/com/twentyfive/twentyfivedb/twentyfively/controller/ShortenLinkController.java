package com.twentyfive.twentyfivedb.twentyfively.controller;

import com.twentyfive.twentyfivedb.twentyfively.service.ShortenLinkService;
import com.twentyfive.twentyfivemodel.dto.twentyfiveLyDto.RequestValue;
import com.twentyfive.twentyfivemodel.dto.twentyfiveLyDto.ResponseValue;
import com.twentyfive.twentyfivemodel.models.twentyfiveLyModels.ShortenLink;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/shorten-link")
@RestController
public class ShortenLinkController {
    private final ShortenLinkService shortenLinkService;

    public ShortenLinkController(ShortenLinkService shortenLinkService) {
        this.shortenLinkService = shortenLinkService;
    }


    @PostMapping("/generate")
    public ResponseEntity<ResponseValue> generateShortenLink(@RequestBody RequestValue requestValue /*, @RequestParam(value = "username") String username*/) {
        ShortenLink result = shortenLinkService.generateShortUrl(requestValue);
        return ResponseEntity.ok(new ResponseValue(result.getShortUrl()));
    }


    @GetMapping("/get-complete-link/{shortUrl}")
    public ResponseEntity<String> getCompleteShortenLink(@PathVariable String shortUrl) {
        String result = shortenLinkService.getCompleteShortenLink(shortUrl);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/list/{userId}")
    public ResponseEntity<List<ShortenLink>> getAllLinksForUserId(@PathVariable String userId) {
        List<ShortenLink> result = shortenLinkService.getAllLinksForUserId(userId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseValue> deleteLink(@PathVariable String id) {
        shortenLinkService.deleteLink(id);
        return ResponseEntity.ok(new ResponseValue("DeletedByAction"));
    }

}
