package com.galileo.cu.servicioimportador;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EntityScan({ "com.galileo.cu.commons.models" })
public class ServicioImportadorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ServicioImportadorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("**************************************");
        System.out.println("Importador V1.1-2024-10-17 10:42");
    }

}
