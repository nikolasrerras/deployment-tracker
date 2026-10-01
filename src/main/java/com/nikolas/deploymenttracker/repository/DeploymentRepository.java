
package com.nikolas.deploymenttracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nikolas.deploymenttracker.model.Deployment;
import com.nikolas.deploymenttracker.model.DeploymentEnvironment;
import com.nikolas.deploymenttracker.model.DeploymentStatus;

public interface DeploymentRepository
        extends JpaRepository<Deployment, Long> {

    List<Deployment> findAllByOrderByDeployedAtDescIdDesc();

    List<Deployment> findByApplication_IdOrderByDeployedAtDescIdDesc(
            Long applicationId
    );

    Optional<Deployment> findFirstByApplication_IdAndEnvironmentAndStatusOrderByDeployedAtDescIdDesc(
            Long applicationId,
            DeploymentEnvironment environment,
            DeploymentStatus status
    );
}
