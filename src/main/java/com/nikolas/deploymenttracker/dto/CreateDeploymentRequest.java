package com.nikolas.deploymenttracker.dto;

import com.nikolas.deploymenttracker.model.DeploymentEnvironment;
import com.nikolas.deploymenttracker.model.DeploymentStatus;

public record CreateDeploymentRequest(
        Long applicationId,
        DeploymentEnvironment environment,
        String version,
        DeploymentStatus status
) {
}