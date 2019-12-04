package com.aeloy.learningsource.bootstrap;

public class LearningSourceTable {
    private static final LearningSourceTable instance = new LearningSourceTable();

    private LearningSourceTable() {}

    public static LearningSourceTable instance() {
        return instance;
    }

    public String getTableName() {
        return "LearningSource";
    }

    public String getPartitionKey() {
        return "hash";
    }

    public String getSortKey() {
        return "registryDate";
    }

    public String uuid() {
        return "uuid";
    }

    public String link() {
        return "link";
    }

    public String description() {
        return "description";
    }
}
