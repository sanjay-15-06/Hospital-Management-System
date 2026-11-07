package com.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HospitalApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(HospitalApplication.class, args);
        String port = context.getEnvironment().getProperty("local.server.port",
                context.getEnvironment().getProperty("server.port", "8080"));
        System.out.println("\nHospital Patient Management System is running!");
        System.out.println("Open http://localhost:" + port + " in your browser.\n");
    }
}
