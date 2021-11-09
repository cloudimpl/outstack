package com.cloudimpl.outstack.spring.service.config;

import com.cloudimpl.outstack.runtime.configs.ConfigEntity;
import com.cloudimpl.outstack.runtime.configs.ConfigGroupEntity;
import com.cloudimpl.outstack.runtime.domain.PolicyRef;
import com.cloudimpl.outstack.runtime.domain.Role;
import com.cloudimpl.outstack.runtime.repo.RepoStreamingReq;
import com.cloudimpl.outstack.runtime.repo.StreamEvent;
import com.cloudimpl.outstack.runtime.repo.StreamEvent.Action;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.component.StreamClient;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author iroshan.h
 */
@Component
@Slf4j
public class GlobalConfigurationService {
  @Value("${outstack.configuration.globalendpoint:#{null}}")
  private String globalEndPoint;
  
  @Autowired
  private Cluster cluster;
  
  private Map<String,String> configMap = new ConcurrentHashMap<String,String>();
  private StreamClient streamClient;
  private String globalGroupEntityId;
  
  @PostConstruct
  private void init(){
    
    if (globalEndPoint == null) {
      log.info("global configuration endpoint not setup");
      return;
    }
    streamClient = new StreamClient(cluster);
    String[] configs = globalEndPoint.split("/");
    if(configs.length !=2){
      throw new RuntimeException("Invalid global endpoint configuration "+globalEndPoint);
    }
    loadGlobalGroupEntity(configs[0].trim(),configs[1].trim());
    
  }
  
  private void loadGlobalGroupEntity(String domainOwner,String domainContext){
    streamClient.subscribeToMicroService("global configurations sync ", domainOwner, domainContext,
        new RepoStreamingReq(Arrays
            .asList(new RepoStreamingReq.ResourceInfo(ConfigGroupEntity.class.getName(), "Global",
                null)),
            Arrays.asList(new RepoStreamingReq.ResourceInfo(ConfigGroupEntity.class.getName(), "Global", null))))
        .doOnNext(e -> updateCache(e))
        .doOnError(err -> log.error("error syncing global configuration ", err))
        .subscribe();
  }
  
  private void updateGlobalGroupEntityId(ConfigGroupEntity configGroupEntity,String domainOwner,String domainContext){
    if(globalGroupEntityId != null){
      return ;
    }
    if(configGroupEntity.getGroupName().equals("Global")){
      globalGroupEntityId  = configGroupEntity.id();
      log.info("updated globalGroupEntityId {}",globalGroupEntityId);
    }
    
    if(globalGroupEntityId!=null){
      streamClient.subscribeToMicroService("global configurations sync ", domainOwner,domainContext,
          new RepoStreamingReq(Arrays
              .asList(new RepoStreamingReq.ResourceInfo(ConfigGroupEntity.class.getName(), "Global",
                  ConfigEntity.class.getName(),"*", null)),
              Arrays.asList(new RepoStreamingReq.ResourceInfo(ConfigEntity.class.getName(),"*", null))))
          .doOnNext(e -> updateCache(e))
          .doOnError(err -> log.error("error syncing global configuration ", err))
          .subscribe();
    }
  }
  private void updateCache(StreamEvent streamEvent){
    if(streamEvent.getAction() == Action.ADD){
      ConfigEntity configEntity = (ConfigEntity) streamEvent.getEvent();
      if(configEntity.id().equals(globalGroupEntityId)){
        configMap.put(configEntity.getConfigName(),configEntity.getConfigValue());
        log.info("inserted/updated global config {}:{}",configEntity.getConfigName(),configEntity.getConfigValue());
      }
    }
  }
  
    public String getConfigString(String name){
    return configMap.get(name);
  }
  
  
  public Integer getConfigInteger(String name){
    return Integer.valueOf(configMap.getOrDefault(name,"0"));
  }

  public Boolean getConfigBoolean(String name){
    return Boolean.valueOf(configMap.getOrDefault(name,"false"));
  }
}
