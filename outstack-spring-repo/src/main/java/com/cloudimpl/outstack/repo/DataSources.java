package com.cloudimpl.outstack.repo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties("repository")
public class DataSources {

   private Map<String,DataSource> datasources;

   public DataSource getDataSource(String dataSource)
   {
        return datasources.getOrDefault(dataSource,datasources.get("default"));
   }

    @Setter
    @Getter
    public  static final class DataSource
    {
        private String provider;

        private Map<String,String> configs;


//        private String prefix;
//
//        private String host;
//
//        private String port;
//
//        private String database;
//
//        private String username;
//
//        private String password;

    }
}