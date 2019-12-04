package com.aeloy.learningsource.bootstrap;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bootstrap")
public class BootstrapController {

    private final DatabaseBootstrap databaseBootstrap;

    public BootstrapController(DatabaseBootstrap databaseBootstrap) {
        this.databaseBootstrap = databaseBootstrap;
    }

    @PostMapping
    public void init() {
        databaseBootstrap.createTableIfNotAvailable();
        databaseBootstrap.createGlobalSecondaryIndexIfNotAvailable();
    }

}
