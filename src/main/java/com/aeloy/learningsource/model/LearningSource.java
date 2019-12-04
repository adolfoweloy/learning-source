package com.aeloy.learningsource.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Value.Immutable
@JsonDeserialize(as = ImmutableLearningSource.class)
@Value.Style(jdkOnly = true)
public abstract class LearningSource {

    private static final DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyyMMdd");

    abstract String hash();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    abstract LocalDateTime registryDate();

    abstract String description();

    abstract String link();

    abstract String uuid();

    String formattedRegistryDate() {
        return registryDate().format(sdf);
    }

}
