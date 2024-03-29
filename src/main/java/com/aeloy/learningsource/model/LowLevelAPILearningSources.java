package com.aeloy.learningsource.model;

import com.aeloy.learningsource.bootstrap.LearningSourceTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.PutRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ReturnItemCollectionMetrics;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class LowLevelAPILearningSources implements LearningSources {
    private static final Logger logger = LoggerFactory.getLogger(LowLevelAPILearningSources.class);
    private final AmazonDynamoDB amazonDynamoDB;

    LowLevelAPILearningSources(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    @Async
    public void saveBatchItems(List<LearningSource> sources) {
        // creating the request so the consumed capacity and metrics about size can be retrieved.
        BatchWriteItemRequest request = new BatchWriteItemRequest(
                ImmutableMap.of(
                    LearningSourceTable.instance().getTableName(),
                    getListOfWrites(sources)))
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .withReturnItemCollectionMetrics(ReturnItemCollectionMetrics.SIZE);

        try {
            BatchWriteItemResult result = amazonDynamoDB.batchWriteItem(request);
            result.getConsumedCapacity()
                    .forEach(item -> {
                        logger.info("consumed {} total capacity", item.getCapacityUnits());
                        logger.info("consumed {} RCUs", item.getReadCapacityUnits());
                        logger.info("consumed {} for GSI", item.getGlobalSecondaryIndexes().size());
                    });

            // in case of unprocessed items, it should retry.
            logger.info("{} out of {} processed", result.getUnprocessedItems().size(), sources.size());
        } catch (Exception e) {
            logger.error("Error processing batch writes.", e);
        }
    }

    private List<WriteRequest> getListOfWrites(List<LearningSource> sources) {
        return sources.stream()
                .map(learningSource -> new WriteRequest(new PutRequest(dynamoDBItemFrom(learningSource))))
                .collect(Collectors.toList());
    }

    private Map<String, AttributeValue> dynamoDBItemFrom(LearningSource item) {
        LearningSourceTable sourceTable = LearningSourceTable.instance();
        return ImmutableMap.of(
                sourceTable.getPartitionKey(), new AttributeValue().withS(item.hash()),
                sourceTable.getSortKey(), new AttributeValue().withS(item.formattedRegistryDate()),
                sourceTable.description(), new AttributeValue().withS(item.description()),
                sourceTable.link(), new AttributeValue().withS(item.link()),
                sourceTable.uuid(), new AttributeValue().withS(item.uuid())
        );
    }
}
