
package com.nikolas.deploymenttracker.repository;

import java.time.Instant;

import com.nikolas.deploymenttracker.model.Application;
import com.nikolas.deploymenttracker.model.Deployment;
import com.nikolas.deploymenttracker.model.DeploymentEnvironment;
import com.nikolas.deploymenttracker.model.DeploymentStatus;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class DeploymentRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    @Test
    void findsLatestSuccessfulDeploymentIgnoringNewerFailure() {

        Application application = applicationRepository.saveAndFlush(
                new Application("payment-service")
        );

        Deployment successful = new Deployment();
        successful.setApplication(application);
        successful.setEnvironment(DeploymentEnvironment.PRODUCTION);
        successful.setVersion("v1.4.2");
        successful.setStatus(DeploymentStatus.SUCCESS);
        successful.setDeployedAt(
                Instant.parse("2026-10-01T10:00:00Z")
        );

        Deployment failed = new Deployment();
        failed.setApplication(application);
        failed.setEnvironment(DeploymentEnvironment.PRODUCTION);
        failed.setVersion("v1.5.0");
        failed.setStatus(DeploymentStatus.FAILED);
        failed.setDeployedAt(
                Instant.parse("2026-10-01T11:00:00Z")
        );

        deploymentRepository.saveAndFlush(successful);
        deploymentRepository.saveAndFlush(failed);

        Deployment latest = deploymentRepository
                .findFirstByApplication_IdAndEnvironmentAndStatusOrderByDeployedAtDescIdDesc(
                        application.getId(),
                        DeploymentEnvironment.PRODUCTION,
                        DeploymentStatus.SUCCESS
                )
                .orElseThrow();

        assertEquals("v1.4.2", latest.getVersion());
        assertEquals(DeploymentStatus.SUCCESS, latest.getStatus());
    }
}
