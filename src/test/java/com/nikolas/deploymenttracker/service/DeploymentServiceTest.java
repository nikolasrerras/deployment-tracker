
package com.nikolas.deploymenttracker.service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.nikolas.deploymenttracker.dto.DeploymentResponse;
import com.nikolas.deploymenttracker.model.Application;
import com.nikolas.deploymenttracker.model.Deployment;
import com.nikolas.deploymenttracker.model.DeploymentEnvironment;
import com.nikolas.deploymenttracker.model.DeploymentStatus;
import com.nikolas.deploymenttracker.repository.ApplicationRepository;
import com.nikolas.deploymenttracker.repository.DeploymentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentServiceTest {

    @Mock
    private DeploymentRepository deploymentRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private DeploymentService deploymentService;

    @Test
    void createDeploymentSavesValidDeployment() {

        Application application = new Application("payment-service");
        application.setId(1L);

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        when(deploymentRepository.save(any(Deployment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        deploymentService.createDeployment(
                1L,
                DeploymentEnvironment.PRODUCTION,
                "v1.0.0",
                DeploymentStatus.SUCCESS
        );

        ArgumentCaptor<Deployment> captor =
                ArgumentCaptor.forClass(Deployment.class);

        verify(deploymentRepository).save(captor.capture());

        Deployment saved = captor.getValue();

        assertEquals(1L, saved.getApplication().getId());
        assertEquals(DeploymentEnvironment.PRODUCTION,
                saved.getEnvironment());
        assertEquals("v1.0.0", saved.getVersion());
        assertEquals(DeploymentStatus.SUCCESS,
                saved.getStatus());
        assertNotNull(saved.getDeployedAt());
    }

    @Test
    void createDeploymentRejectsBlankVersion() {

        assertThrows(
                IllegalArgumentException.class,
                () -> deploymentService.createDeployment(
                        1L,
                        DeploymentEnvironment.PRODUCTION,
                        "   ",
                        DeploymentStatus.SUCCESS
                )
        );

        verifyNoInteractions(
                applicationRepository,
                deploymentRepository
        );
    }

    @Test
    void createDeploymentThrowsWhenApplicationNotFound() {

        when(applicationRepository.findById(9999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> deploymentService.createDeployment(
                        9999L,
                        DeploymentEnvironment.PRODUCTION,
                        "v1.0.0",
                        DeploymentStatus.SUCCESS
                )
        );

        verify(deploymentRepository, never())
                .save(any(Deployment.class));
    }

    @Test
    void getLatestSuccessfulDeploymentReturnsSuccess() {

        Application application = new Application("payment-service");
        application.setId(1L);

        Deployment successful = new Deployment();
        successful.setId(10L);
        successful.setApplication(application);
        successful.setEnvironment(DeploymentEnvironment.PRODUCTION);
        successful.setVersion("v1.4.2");
        successful.setStatus(DeploymentStatus.SUCCESS);
        successful.setDeployedAt(
                Instant.parse("2026-10-01T10:00:00Z")
        );

        when(applicationRepository.existsById(1L))
                .thenReturn(true);

        when(deploymentRepository
                .findFirstByApplication_IdAndEnvironmentAndStatusOrderByDeployedAtDescIdDesc(
                        1L,
                        DeploymentEnvironment.PRODUCTION,
                        DeploymentStatus.SUCCESS
                ))
                .thenReturn(Optional.of(successful));

        DeploymentResponse result =
                deploymentService.getLatestSuccessfulDeployment(
                        1L,
                        DeploymentEnvironment.PRODUCTION
                );

        assertEquals(10L, result.id());
        assertEquals(1L, result.applicationId());
        assertEquals("v1.4.2", result.version());
        assertEquals(DeploymentStatus.SUCCESS, result.status());

        verify(deploymentRepository)
                .findFirstByApplication_IdAndEnvironmentAndStatusOrderByDeployedAtDescIdDesc(
                        1L,
                        DeploymentEnvironment.PRODUCTION,
                        DeploymentStatus.SUCCESS
                );
    }

    @Test
    void getLatestSuccessfulDeploymentThrowsWhenNoneExists() {

        when(applicationRepository.existsById(1L))
                .thenReturn(true);

        when(deploymentRepository
                .findFirstByApplication_IdAndEnvironmentAndStatusOrderByDeployedAtDescIdDesc(
                        1L,
                        DeploymentEnvironment.PRODUCTION,
                        DeploymentStatus.SUCCESS
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> deploymentService.getLatestSuccessfulDeployment(
                        1L,
                        DeploymentEnvironment.PRODUCTION
                )
        );
    }

    @Test
    void getAllDeploymentsReturnsDeploymentResponses() {

        Application application = new Application("payment-service");
        application.setId(1L);

        Deployment deployment = new Deployment();
        deployment.setId(10L);
        deployment.setApplication(application);
        deployment.setEnvironment(DeploymentEnvironment.STAGING);
        deployment.setVersion("v1.0.0");
        deployment.setStatus(DeploymentStatus.SUCCESS);
        deployment.setDeployedAt(
                Instant.parse("2026-10-01T10:00:00Z")
        );

        when(deploymentRepository.findAllByOrderByDeployedAtDescIdDesc())
                .thenReturn(List.of(deployment));

        List<DeploymentResponse> result =
                deploymentService.getAllDeployments();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals("v1.0.0", result.get(0).version());
        assertEquals(
                DeploymentEnvironment.STAGING,
                result.get(0).environment()
        );
    }

    @Test
    void getDeploymentsByApplicationIdThrowsWhenApplicationNotFound() {

        when(applicationRepository.existsById(9999L))
                .thenReturn(false);

        assertThrows(
                NoSuchElementException.class,
                () -> deploymentService.getDeploymentsByApplicationId(9999L)
        );

        verifyNoInteractions(deploymentRepository);
    }
}
