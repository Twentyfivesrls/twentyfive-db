package com.twentyfive.twentyfivedb;

import lombok.Data;
import org.bson.Document;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import twentyfive.twentyfiveadapter.dto.fidelityDto.FilterCardGroupRequest;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class Utility {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 6;

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

    public static List<AggregationOperation> parseOtherFiltersForFidelityCard(
            FilterCardGroupRequest filterObject,
            String ownerId,
            boolean withPagination,
            int page,
            int size) {

        List<AggregationOperation> operations = new ArrayList<>();

        // Filtro base su ownerId
        operations.add(Aggregation.match(Criteria.where("ownerId").is(ownerId)));

        if (filterObject != null) {
            // Filtro su isActive
            if (filterObject.getIsActive() != null) {
                operations.add(Aggregation.match(Criteria.where("isActive").is(filterObject.getIsActive())));
            }

            // Filtro su cardGroupId
            if (filterObject.getCardGroupId() != null && !filterObject.getCardGroupId().isEmpty()) {
                operations.add(Aggregation.match(Criteria.where("cardGroupId").is(filterObject.getCardGroupId())));
            }

            // Filtro su name OR surname OR email (case-insensitive)
            if (filterObject.getName() != null && !filterObject.getName().isEmpty()) {
                String value = filterObject.getName();
                operations.add(Aggregation.match(new Criteria().orOperator(
                        Criteria.where("name").regex("^" + Pattern.quote(value) + "$", "i"),
                        Criteria.where("surname").regex("^" + Pattern.quote(value) + "$", "i"),
                        Criteria.where("email").regex("^" + Pattern.quote(value) + "$", "i")
                )));
            }

            // Filtri su date (fromDate / toDate) relativi al cardGroup
            if (filterObject.getFromDate() != null || filterObject.getToDate() != null) {
                // Converto cardGroupId in ObjectId per il lookup
                operations.add(Aggregation.addFields()
                        .addField("cardGroupIdAsObjectId")
                        .withValueOf(new Document("$toObjectId", "$cardGroupId"))
                        .build());

                LookupOperation lookupOperation = LookupOperation.newLookup()
                        .from("fidelity_card_group")
                        .localField("cardGroupIdAsObjectId")
                        .foreignField("_id")
                        .as("cardGroup");
                operations.add(lookupOperation);

                operations.add(Aggregation.unwind("cardGroup"));

                // Calcolo intervallo date
                LocalDateTime startDate = filterObject.getFromDate() != null
                        ? filterObject.getFromDate().toLocalDate().atStartOfDay()
                        : null;
                LocalDateTime endDate = filterObject.getToDate() != null
                        ? filterObject.getToDate().toLocalDate().atTime(LocalTime.MAX)
                        : null;

                if (startDate != null && endDate != null) {
                    operations.add(Aggregation.match(
                            Criteria.where("cardGroup.expirationDate").gte(startDate).lte(endDate)
                    ));
                } else if (startDate != null) {
                    operations.add(Aggregation.match(
                            Criteria.where("cardGroup.expirationDate").gte(startDate).lte(startDate.plusDays(1))
                    ));
                } else if (endDate != null) {
                    operations.add(Aggregation.match(
                            Criteria.where("cardGroup.expirationDate").gte(endDate).lte(endDate.plusDays(1))
                    ));
                }
            }
        }

        // Paginazione
        if (withPagination) {
            operations.add(Aggregation.skip((long) page * size));
            operations.add(Aggregation.limit(size));
        }

        return operations;
    }

    public static String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }

        return sb.toString();
    }

    public static String generateCode() {
        return generateCode(DEFAULT_LENGTH);
    }


    @Data
    public static class CountResult {
        private long total;
    }

}

