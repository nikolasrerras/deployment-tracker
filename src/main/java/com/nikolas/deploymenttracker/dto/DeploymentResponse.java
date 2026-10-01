package com.nikolas.deploymenttracker.dto;

import java.time.Instant;

import com.nikolas.deploymenttracker.model.DeploymentEnvironment;
import com.nikolas.deploymenttracker.model.DeploymentStatus;

public record DeploymentResponse(
        Long id,
        Long applicationId,
        DeploymentEnvironment environment,
        String version,
        DeploymentStatus status,
        Instant deployedAt
) {
}