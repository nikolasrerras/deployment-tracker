
package com.nikolas.deploymenttracker.controller;

import com.nikolas.deploymenttracker.model.Application;
import com.nikolas.deploymenttracker.repository.ApplicationRepository;
import com.nikolas.deploymenttracker.repository.DeploymentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DeploymentControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    private Long applicationId;

    @BeforeEach
    void setUp() {

        deploymentRepository.deleteAll();
        applicationRepository.deleteAll();

        Application application = applicationRepository.save(
                new Application("payment-service")
        );

        applicationId = application.getId();
    }

    private String deploymentRequest(
            Long applicationId,
            String environment,
            String version,
            String status) {

        return """
                {
                  "applicationId": %d,
                  "environment": "%s",
                  "version": "%s",
                  "status": "%s"
                }
                """.formatted(
                applicationId,
                environment,
                version,
                status
        );
    }

    @Test
    void createDeploymentReturns201AndPersistsDeployment()
            throws Exception {

        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deploymentRequest(
                                applicationId,
                                "PRODUCTION",
                                "v1.0.0",
                                "SUCCESS"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.environment")
                        .value("PRODUCTION"))
                .andExpect(jsonPath("$.version")
                        .value("v1.0.0"))
                .andExpect(jsonPath("$.status")
                        .value("SUCCESS"))
                .andExpect(jsonPath("$.deployedAt")
                        .exists());

        assertEquals(1, deploymentRepository.count());
    }

    @Test
    void createDeploymentWithBlankVersionReturns400()
            throws Exception {

        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deploymentRequest(
                                applicationId,
                                "PRODUCTION",
                                "",
                                "SUCCESS"
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"))
                .andExpect(jsonPath("$.message")
                        .value("Version is required"));

        assertEquals(0, deploymentRepository.count());
    }

    @Test
    void createDeploymentWithNonexistentApplicationReturns404()
            throws Exception {

        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deploymentRequest(
                                9999L,
                                "PRODUCTION",
                                "v1.0.0",
                                "SUCCESS"
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Application not found: 9999"));

        assertEquals(0, deploymentRepository.count());
    }

    @Test
    void createDeploymentWithInvalidEnvironmentReturns400()
            throws Exception {

        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deploymentRequest(
                                applicationId,
                                "PROD",
                                "v1.0.0",
                                "SUCCESS"
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Malformed JSON or unsupported field value"));

        assertEquals(0, deploymentRepository.count());
    }

    @Test
    void latestSuccessfulDeploymentIgnoresNewerFailure()
            throws Exception {

        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deploymentRequest(
                                applicationId,
                                "PRODUCTION",
                                "v1.4.2",
                                "SUCCESS"
                        )))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deploymentRequest(
                                applicationId,
                                "PRODUCTION",
                                "v1.5.0",
                                "FAILED"
                        )))
                .andExpect(status().isCreated());

        mockMvc.perform(get(
                        "/api/applications/{id}/environments/{environment}/latest",
                        applicationId,
                        "PRODUCTION"
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version")
                        .value("v1.4.2"))
                .andExpect(jsonPath("$.status")
                        .value("SUCCESS"));

        assertEquals(2, deploymentRepository.count());
    }
}
