package com.twentyfive.twentyfivedb;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class Utility {

    private Utility() {
    }

    public static Pageable makePageableObj(String sortDirection, String sortColumn, int page, int size) {
        Sort.Direction direction;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        } else {
            direction = Sort.Direction.ASC;
        }

        return PageRequest.of(page, size, Sort.by(direction, sortColumn));
    }
}
