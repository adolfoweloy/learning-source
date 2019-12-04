package com.aeloy.learningsource.bootstrap;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.amazonaws.services.dynamodbv2.model.CreateGlobalSecondaryIndexAction;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexUpdate;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateTableResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class DatabaseBootstrap {
    private static final long RCU = 5;
    private static final long WCU = 5;

    private final AmazonDynamoDB amazonDynamoDB;

    DatabaseBootstrap(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    void createTableIfNotAvailable() {
        LearningSourceTable tableDefinition = LearningSourceTable.instance();

        try {
            amazonDynamoDB.describeTable(tableDefinition.getTableName());
        } catch (ResourceNotFoundException e) {

            CreateTableRequest createTableRequest = new CreateTableRequest();
            CreateTableResult table = amazonDynamoDB.createTable(
                    createTableRequest
                            .withTableName(tableDefinition.getTableName())
                            .withKeySchema(
                                    new KeySchemaElement(tableDefinition.getPartitionKey(), KeyType.HASH),
                                    new KeySchemaElement(tableDefinition.getSortKey(), KeyType.RANGE))
                            .withAttributeDefinitions(
                                    new AttributeDefinition(tableDefinition.getPartitionKey(), ScalarAttributeType.S),
                                    new AttributeDefinition(tableDefinition.getSortKey(), ScalarAttributeType.S)
                            )
                            .withBillingMode(BillingMode.PROVISIONED)
                            .withProvisionedThroughput(throughput())
            );
            waitUntilActive(table.getTableDescription());
        }
    }

    /**
     * Provides a global secondary index. Global because it spans to all data throughout all partitions.
     */
    void createGlobalSecondaryIndexIfNotAvailable() {
        LearningSourceTable tableDefinition = LearningSourceTable.instance();

        try {
            DescribeTableResult describeTableResult = amazonDynamoDB.describeTable(tableDefinition.getTableName());

            // I do not like this code :'(
            boolean hasGSI = Optional.ofNullable(describeTableResult.getTable().getGlobalSecondaryIndexes())
                    .map(List::size)
                    .map(size -> size > 0)
                    .orElse(false);

            if (hasGSI) {
                return;
            }

            createGlobalSecondaryIndex(tableDefinition);
        } catch (ResourceNotFoundException e) {
            createGlobalSecondaryIndex(tableDefinition);
        }
    }

    private void createGlobalSecondaryIndex(LearningSourceTable tableDefinition) {
        CreateGlobalSecondaryIndexAction gsiAction = new CreateGlobalSecondaryIndexAction()
                .withIndexName(tableDefinition.uuid())
                .withKeySchema(new KeySchemaElement(tableDefinition.uuid(), KeyType.HASH))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withProvisionedThroughput(throughput());

        GlobalSecondaryIndexUpdate update = new GlobalSecondaryIndexUpdate().withCreate(gsiAction);
        UpdateTableResult result = amazonDynamoDB.updateTable(
                new UpdateTableRequest()
                        .withTableName(tableDefinition.getTableName())
                        .withAttributeDefinitions(new AttributeDefinition(
                                tableDefinition.uuid(), ScalarAttributeType.S
                        ))
                        .withGlobalSecondaryIndexUpdates(update));

        waitUntilActive(result.getTableDescription());
    }

    private void waitUntilActive(TableDescription tableDescription) {
        int timeout = 0;

        try {
            while (!tableDescription.getTableStatus().equals("ACTIVE") && timeout < 5000) {
                    Thread.sleep(1000);
                    timeout += 1000;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Table " + tableDescription.getTableName() + " is now ACTIVE");
    }

    private ProvisionedThroughput throughput() {
        return new ProvisionedThroughput()
                .withReadCapacityUnits(RCU)
                .withWriteCapacityUnits(WCU);
    }
}
