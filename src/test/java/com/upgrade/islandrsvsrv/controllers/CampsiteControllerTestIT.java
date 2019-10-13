package com.upgrade.islandrsvsrv.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {CampsiteControllerTestIT.Initializer.class})
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CampsiteControllerTestIT {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private CampsiteController campsiteController;

	@ClassRule
	public static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:latest")
			.withDatabaseName("island_resort_test")
			.withUsername("upgrade")
			.withPassword("upgrade");


	@Test
	public void testReturnsDefaultAvailabilities() throws Exception {
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
												 new TypeReference<List<LocalDate>>() {});
					} catch (IOException e) {
						//noop
					}

					//then
					assertThat(dates)
							.containsExactlyElementsOf(now().plus(1, DAYS)
															   .datesUntil(now().plus(1, MONTHS)
																				   .plus(1, DAYS))
															   .collect(toList()));
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
