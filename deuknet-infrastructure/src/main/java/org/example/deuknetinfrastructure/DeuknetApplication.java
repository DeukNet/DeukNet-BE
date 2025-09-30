package org.example.deuknetinfrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "org.example.deuknetinfrastructure",
    "org.example.deuknetpresentation",
    "org.example.deuknetapplication"
})
public class DeuknetApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeuknetApplication.class, args);
    }

}
