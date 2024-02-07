package com.twentyfive.twentyfivedb.fidelity.controller;

import com.twentyfive.twentyfivedb.fidelity.service.CardGroupService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/card-group")
public class CardGroupController {

    private final CardGroupService cardGroupService;

        public CardGroupController(CardGroupService cardGroupService) {this.cardGroupService = cardGroupService;}
}
