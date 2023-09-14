package com.twentyfive.twentyfivedb.controllers;

import com.twentyfive.twentyfivedb.services.UserLinkService;
import com.twentyfive.twentyfivemodel.models.LinkTree;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/linkTree")
public class UserLinkController {

    private final UserLinkService userLinkService;
    @PostMapping("/add")
    public ResponseEntity<Object> add(@RequestBody LinkTree linkTree, @RequestParam("username") String username){
        try {
            return new ResponseEntity<>(userLinkService.add(linkTree, username), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getClass().getSimpleName(), HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping("/delete")
    public ResponseEntity<Object> delete(@RequestParam("id") String id, @RequestParam("username") String username){
        try {
            return new ResponseEntity<>(userLinkService.delete(id, username), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getClass().getSimpleName(), HttpStatus.BAD_REQUEST);
        }
    }
    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestParam("id")String id, @RequestBody LinkTree linkTree, @RequestParam("username") String username){
        try {
            return new ResponseEntity<>(userLinkService.update(id, linkTree, username), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getClass().getSimpleName(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/findByUser")
    public ResponseEntity<Object> findByUserId(@RequestParam("username") String username){
        try {
            return new ResponseEntity<>(userLinkService.findByUsername(username), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getClass().getSimpleName(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/findByUsername")
    public ResponseEntity<Object> findByUsername(@RequestParam("username") String username){
        try {
            return new ResponseEntity<>(userLinkService.findByUsername(username), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getClass().getSimpleName(), HttpStatus.BAD_REQUEST);
        }
    }
}
