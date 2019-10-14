package com.upgrade.islandrsvsrv.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

  @Autowired
  private ObjectMapper objectMapper;

  @PostConstruct
  public void setUp() {
    objectMapper.registerModule(new JavaTimeModule());
  }
}
