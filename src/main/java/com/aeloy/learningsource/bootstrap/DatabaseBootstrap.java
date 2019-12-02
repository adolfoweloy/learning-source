package com.aeloy.learningsource.bootstrap;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import org.springframework.stereotype.Component;

@Component
class DatabaseBootstrap {

    private final AmazonDynamoDB amazonDynamoDB;

    DatabaseBootstrap(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    void createTable() {
        CreateTableRequest createTableRequest = new CreateTableRequest();
        CreateTableResult table = amazonDynamoDB.createTable(
                createTableRequest
                        .withTableName("LearningSource")
                        .withKeySchema(
                                new KeySchemaElement("hash", KeyType.HASH),
                                new KeySchemaElement("registryDate", KeyType.RANGE))
                        .withAttributeDefinitions(
                                new AttributeDefinition("hash", ScalarAttributeType.S),
                                new AttributeDefinition("registryDate", ScalarAttributeType.S)
                        )
                        .withBillingMode(BillingMode.PROVISIONED)
                        .withProvisionedThroughput(
                                new ProvisionedThroughput()
                                        .withReadCapacityUnits(5l)
                                        .withWriteCapacityUnits(5l))
        );
        waitUntilActive(table.getTableDescription());
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
