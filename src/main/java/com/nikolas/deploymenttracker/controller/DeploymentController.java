
package com.nikolas.deploymenttracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nikolas.deploymenttracker.dto.CreateDeploymentRequest;
import com.nikolas.deploymenttracker.dto.DeploymentResponse;
import com.nikolas.deploymenttracker.model.Deployment;
import com.nikolas.deploymenttracker.service.DeploymentService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/deployments")
public class DeploymentController {

    private final DeploymentService deploymentService;

    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeploymentResponse createDeployment(
            @RequestBody CreateDeploymentRequest request) {

        Deployment deployment = deploymentService.createDeployment(
                request.applicationId(),
                request.environment(),
                request.version(),
                request.status()
        );

        return new DeploymentResponse(
                deployment.getId(),
                request.applicationId(),
                deployment.getEnvironment(),
                deployment.getVersion(),
                deployment.getStatus(),
                deployment.getDeployedAt()
        );
    }

    @GetMapping
    public List<DeploymentResponse> getAllDeployments() {
        return deploymentService.getAllDeployments();
    }
}
