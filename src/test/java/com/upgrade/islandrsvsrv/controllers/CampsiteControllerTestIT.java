package com.upgrade.islandrsvsrv.controllers;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;


@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {CampsiteControllerTestIT.Initializer.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CampsiteControllerTestIT {

  @ClassRule
  public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:latest")
      .withDatabaseName("island_resort_test")
      .withUsername("upgrade")
      .withPassword("upgrade");
  @Autowired
  private ObjectMapper mapper;
  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private CampsiteController campsiteController;

  @Test
  public void testReturnsDefaultAvailabilities() throws Exception {
    //given
    LocalDate tomorrow = now().plus(1, DAYS);
    //when
    webTestClient.get()
        .uri("/campsite/availabilities")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
        .expectBody()
        .consumeWith(response -> {
          List<LocalDate> dates = null;
          try {
            dates = mapper.readValue(response.getResponseBody(),
                new TypeReference<List<LocalDate>>() {
                });
          } catch (IOException e) {
            //noop
          }

          //then
          assertThat(dates).containsExactlyElementsOf(tomorrow.datesUntil(tomorrow.plus(1, MONTHS))
              .collect(toList()));
        });

  }

  @Test
  public void testAvailabilitiesForRequestedTimeFrame() throws Exception {
    //given
    LocalDate tomorrow = now().plus(1, DAYS);
    LocalDate tenDaysFromNow = now().plus(10, DAYS);
    //when
    webTestClient.get()
        .uri(uriBuilder -> uriBuilder.path("/campsite/availabilities")
            .queryParam("startDate", tomorrow)
            .queryParam("endDate", tenDaysFromNow)
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
        .expectBody()
        .consumeWith(response -> {
          List<LocalDate> dates = null;
          try {
            dates = mapper.readValue(response.getResponseBody(),
                new TypeReference<List<LocalDate>>() {
                });
          } catch (IOException e) {
            //noop
          }

          //then
          assertThat(dates).containsExactlyElementsOf(
              tomorrow.datesUntil(tenDaysFromNow).collect(toList()));
        });

  }


  static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues.of(
          "spring.datasource.url=" + postgres.getJdbcUrl(),
          "spring.datasource.username=" + postgres.getUsername(),
          "spring.datasource.password=" + postgres.getPassword()
      ).applyTo(configurableApplicationContext.getEnvironment());
    }
  }
}
