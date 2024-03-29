package com.twentyfive.twentyfivedb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@EntityScan("com.twentyfive.twentyfivemodel.models.*")
public class TwentyfiveDbApplication {

    public static void main(String[] args) {SpringApplication.run(TwentyfiveDbApplication.class, args);
    }

}
