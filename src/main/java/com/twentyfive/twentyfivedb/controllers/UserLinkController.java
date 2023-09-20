package com.twentyfive.twentyfivedb.controllers;

import com.twentyfive.twentyfivedb.services.UserLinkService;
import com.twentyfive.twentyfivemodel.models.LinkTree;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.Document.UserLinkDocumentDB;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping("/linkTree")
public class UserLinkController {

    private final UserLinkService userLinkService;

    @PostMapping("/add")
    public ResponseEntity<Object> add(@RequestBody LinkTree linkTree, @RequestParam("username") String username){
            return new ResponseEntity<>(userLinkService.add(linkTree, username), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> delete(@RequestParam("id") String id, @RequestParam("username") String username){
            return new ResponseEntity<>(userLinkService.delete(id, username), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestParam("id")String id, @RequestBody LinkTree linkTree, @RequestParam("username") String username){
            return new ResponseEntity<>(userLinkService.update(id, linkTree, username), HttpStatus.OK);
    }

    @GetMapping("/findByUsername")
    public ResponseEntity<UserLinkDocumentDB> findByUsername(@RequestParam("username") String username){

        return new ResponseEntity(userLinkService.findByUsername(username), HttpStatus.OK);
    }
   /* @GetMapping("/update/")
    public wduudewiud (@RequestParam("id") String id)*/
}
