
package com.nikolas.deploymenttracker.service;

import java.time.Instant;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nikolas.deploymenttracker.model.Application;
import com.nikolas.deploymenttracker.model.Deployment;
import com.nikolas.deploymenttracker.model.DeploymentEnvironment;
import com.nikolas.deploymenttracker.model.DeploymentStatus;
import com.nikolas.deploymenttracker.repository.ApplicationRepository;
import com.nikolas.deploymenttracker.repository.DeploymentRepository;
import java.util.List;
import com.nikolas.deploymenttracker.dto.DeploymentResponse;

@Service
public class DeploymentService {

    private final DeploymentRepository deploymentRepository;
    private final ApplicationRepository applicationRepository;

    public DeploymentService(
            DeploymentRepository deploymentRepository,
            ApplicationRepository applicationRepository) {

        this.deploymentRepository = deploymentRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public Deployment createDeployment(
            Long applicationId,
            DeploymentEnvironment environment,
            String version,
            DeploymentStatus status) {

        if (applicationId == null || applicationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid application ID"
            );
        }

        if (environment == null || status == null) {
            throw new IllegalArgumentException(
                    "Environment and status are required"
            );
        }

        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException(
                    "Version cannot be empty"
            );
        }

        String normalizedVersion = version.trim();

        if (normalizedVersion.length() > 50) {
            throw new IllegalArgumentException(
                    "Version cannot exceed 50 characters"
            );
        }

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Application not found: " + applicationId
                ));

        Deployment deployment = new Deployment(
                application,
                environment,
                normalizedVersion,
                status,
                Instant.now()
        );

        return deploymentRepository.save(deployment);
    }

    @Transactional(readOnly = true)
        public List<DeploymentResponse> getAllDeployments() {

        return deploymentRepository
            .findAllByOrderByDeployedAtDescIdDesc()
            .stream()
            .map(deployment -> new DeploymentResponse(
                    deployment.getId(),
                    deployment.getApplication().getId(),
                    deployment.getEnvironment(),
                    deployment.getVersion(),
                    deployment.getStatus(),
                    deployment.getDeployedAt()
            ))
            .toList();
        }


        @Transactional(readOnly = true)
                public List<DeploymentResponse> getDeploymentsByApplicationId(
                Long applicationId) {

                if (!applicationRepository.existsById(applicationId)) {
                        throw new NoSuchElementException(
                        "Application not found: " + applicationId
                );
        }

                return deploymentRepository
                .findByApplication_IdOrderByDeployedAtDescIdDesc(applicationId)
                .stream()
                .map(deployment -> new DeploymentResponse(
                    deployment.getId(),
                    deployment.getApplication().getId(),
                    deployment.getEnvironment(),
                    deployment.getVersion(),
                    deployment.getStatus(),
                    deployment.getDeployedAt()
                ))
                .toList();
        }


@Transactional(readOnly = true)
public DeploymentResponse getLatestSuccessfulDeployment(
        Long applicationId,
        DeploymentEnvironment environment) {

    if (!applicationRepository.existsById(applicationId)) {
        throw new NoSuchElementException(
                "Application not found: " + applicationId
        );
    }

    Deployment deployment = deploymentRepository
            .findFirstByApplication_IdAndEnvironmentAndStatusOrderByDeployedAtDescIdDesc(
                    applicationId,
                    environment,
                    DeploymentStatus.SUCCESS
            )
            .orElseThrow(() -> new NoSuchElementException(
                    "No successful deployment found"
            ));

    return new DeploymentResponse(
            deployment.getId(),
            deployment.getApplication().getId(),
            deployment.getEnvironment(),
            deployment.getVersion(),
            deployment.getStatus(),
            deployment.getDeployedAt()
    );
}


}
