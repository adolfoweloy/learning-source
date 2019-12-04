package com.aeloy.learningsource.model;

import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Value.Immutable
public abstract class LearningSource {

    private static final DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyyMMdd");

    abstract String hash();

    abstract LocalDateTime registryDate();

    abstract String description();

    abstract String link();

    abstract String uuid();

    String formattedRegistryDate() {
        return registryDate().format(sdf);
    }

}
