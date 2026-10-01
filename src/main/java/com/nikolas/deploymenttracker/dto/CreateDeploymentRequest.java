
package com.nikolas.deploymenttracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.nikolas.deploymenttracker.model.DeploymentEnvironment;
import com.nikolas.deploymenttracker.model.DeploymentStatus;

public record CreateDeploymentRequest(

        @NotNull(message = "Application ID is required")
        @Positive(message = "Application ID must be positive")
        Long applicationId,

        @NotNull(message = "Environment is required")
        DeploymentEnvironment environment,

        @NotBlank(message = "Version is required")
        @Size(
                max = 50,
                message = "Version must not exceed 50 characters"
        )
        String version,

        @NotNull(message = "Status is required")
        DeploymentStatus status

) {
}
