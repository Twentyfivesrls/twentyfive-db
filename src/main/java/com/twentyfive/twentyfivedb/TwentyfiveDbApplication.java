package com.twentyfive.twentyfivedb;

import com.twentyfive.twentyfivemodel.models.UserLink;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import twentyfive.twentyfiveadapter.Document.UserLinkDocumentDB;
import twentyfive.twentyfiveadapter.Mapper.TwentyFiveMapper;

@SpringBootApplication
public class TwentyfiveDbApplication {

    public static void main(String[] args) {SpringApplication.run(TwentyfiveDbApplication.class, args);
    }

}
