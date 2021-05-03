/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.example;

//import com.cloudimpl.outstack.spring.security.JwtConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 *
 * @author nuwan
 */
@SpringBootApplication(scanBasePackages = "com.cloudimpl.outstack",exclude = {
  //  SecurityAutoConfiguration.class,
   // JwtConfiguration.class
})
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}