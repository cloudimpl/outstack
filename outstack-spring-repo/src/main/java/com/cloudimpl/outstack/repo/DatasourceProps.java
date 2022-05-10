package com.cloudimpl.outstack.repo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("postgres.datasource")
public class DatasourceProps {

    private String prefix;

    private String host;

    private String port;

    private String database;

    private String username;

    private String password;

}