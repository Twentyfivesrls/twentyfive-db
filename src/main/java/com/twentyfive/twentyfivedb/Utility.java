package com.twentyfive.twentyfivedb;

import lombok.Data;
import org.bson.Document;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import twentyfive.twentyfiveadapter.dto.fidelityDto.FilterCardGroupRequest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    public static <T> Page<T> convertListToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    public static String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(formatter);
    }

    public static String formatDateToLocalDateTime(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M dd, yyyy");
        return date.format(formatter);
    }

    public static List<Criteria> parseOtherFiltersForFidelity(FilterCardGroupRequest filterObject) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (filterObject == null) {
            return criteriaList;
        }
        if (filterObject.getIsActive() != null) {
            criteriaList.add(Criteria.where("isActive").is(filterObject.getIsActive()));
        }
        if (filterObject.getFromDate() != null && filterObject.getToDate() != null) {
            criteriaList.add(Criteria.where("expirationDate").gte(filterObject.getFromDate()).lte(filterObject.getToDate()));
        }
        if (filterObject.getToDate() != null && filterObject.getFromDate() == null) {
            criteriaList.add(Criteria.where("expirationDate").is(filterObject.getToDate()));
        }
        if (filterObject.getFromDate() != null && filterObject.getToDate() == null) {
            criteriaList.add(Criteria.where("expirationDate").is(filterObject.getFromDate()));
        }
        return criteriaList;
    }

    public static List<AggregationOperation> parseOtherFiltersForFidelityCard(FilterCardGroupRequest filterObject, String ownerId, Pageable pageable) {
        List<AggregationOperation> operations = new ArrayList<>();

        // Match criteria on ownerId
        operations.add(Aggregation.match(Criteria.where("ownerId").is(ownerId)));

        // Match criteria on Card
        if (filterObject != null) {
            if (filterObject.getIsActive() != null) {
                operations.add(Aggregation.match(Criteria.where("isActive").is(filterObject.getIsActive())));
            }

            if (filterObject.getToDate() != null || filterObject.getFromDate() != null) {

                operations.add(Aggregation.addFields().addField("cardGroupIdAsObjectId").withValueOf(
                        new Document("$toObjectId", "$cardGroupId")
                ).build());

                // Lookup to join CardGroup
                LookupOperation lookupOperation = LookupOperation.newLookup()
                        .from("fidelity_card_group")
                        .localField("cardGroupIdAsObjectId")
                        .foreignField("_id")
                        .as("cardGroup");
                operations.add(lookupOperation);

                // Unwind the joined CardGroup array
                operations.add(Aggregation.unwind("cardGroup"));

                // Match criteria on CardGroup's expirationDate
                if (filterObject.getFromDate() != null && filterObject.getToDate() != null) {
                    LocalDateTime startDate = filterObject.getFromDate().toLocalDate().atStartOfDay();
                    LocalDateTime endDate = filterObject.getToDate().toLocalDate().atTime(LocalTime.MAX);
                    operations.add(Aggregation.match(Criteria.where("cardGroup.expirationDate").gte(startDate).lte(endDate)));
                } else if (filterObject.getToDate() != null) {
                    LocalDateTime startDate = filterObject.getToDate().toLocalDate().atStartOfDay();
                    LocalDateTime endDate = filterObject.getToDate().toLocalDate().atTime(LocalTime.MAX);
                    operations.add(Aggregation.match(Criteria.where("cardGroup.expirationDate").gte(startDate).lte(endDate)));
                } else if (filterObject.getFromDate() != null) {
                    LocalDateTime startDate = filterObject.getFromDate().toLocalDate().atStartOfDay();
                    LocalDateTime endDate = filterObject.getFromDate().toLocalDate().atTime(LocalTime.MAX);
                    operations.add(Aggregation.match(Criteria.where("cardGroup.expirationDate").gte(startDate).lte(endDate)));
                }

            }

        }
        // Add pagination
        operations.add(Aggregation.skip(pageable.getOffset()));
        operations.add(Aggregation.limit(pageable.getPageSize()));
        return operations;
    }

    @Data
    public static class CountResult {
        private long total;
    }

}

