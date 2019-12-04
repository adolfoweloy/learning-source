package com.aeloy.learningsource.controller;

import com.aeloy.learningsource.model.LearningSource;
import com.aeloy.learningsource.model.LearningSources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/import")
public class BatchImportSourcesController {

    private final LearningSources learningSources;

    public BatchImportSourcesController(LearningSources learningSources) {
        this.learningSources = learningSources;
    }

    @PostMapping
    public ResponseEntity<Void> importLearningSources(@RequestBody List<LearningSource> sources) {
        learningSources.saveBatchItems(sources);
        return ResponseEntity.noContent().build();
    }

}
