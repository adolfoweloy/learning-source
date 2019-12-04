package com.aeloy.learningsource.bootstrap;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import org.springframework.stereotype.Component;

@Component
class DatabaseBootstrap {
    private static final long RCU = 5;
    private static final long WCU = 5;

    private final AmazonDynamoDB amazonDynamoDB;

    DatabaseBootstrap(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    void createTableIfNeeded() {
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
                            .withProvisionedThroughput(
                                    new ProvisionedThroughput()
                                            .withReadCapacityUnits(RCU)
                                            .withWriteCapacityUnits(WCU))
            );
            waitUntilActive(table.getTableDescription());
        }
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

}
