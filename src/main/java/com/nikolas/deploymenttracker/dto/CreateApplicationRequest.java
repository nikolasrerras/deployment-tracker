
package com.nikolas.deploymenttracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(

        @NotBlank(message = "Application name is required")
        @Size(
                max = 100,
                message = "Application name must not exceed 100 characters"
        )
        String name

) {
}
