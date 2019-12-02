package com.aeloy.learningsource.model;

import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
public interface LearningSource {

    String hash();

    LocalDateTime registryDate();

    String description();

    String link();

    String uuid();

}
