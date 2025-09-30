package org.example.deuknetpresentation.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HealthcheckController {

    @GetMapping
    public String healthcheck() {
        log.info("healthcheck");
        return "OK";
    }
}
