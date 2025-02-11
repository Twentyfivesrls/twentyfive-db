package com.twentyfive.twentyfivedb.tictic.util;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

public class CustomAggregationOperation implements AggregationOperation {

  private final Document operation;

  public CustomAggregationOperation(Document operation) {
    this.operation = operation;
  }

  @Override
  public Document toDocument(AggregationOperationContext context) {
    return operation;
  }
}
