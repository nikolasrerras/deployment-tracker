
package com.nikolas.deploymenttracker.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nikolas.deploymenttracker.dto.ApplicationResponse;
import com.nikolas.deploymenttracker.dto.CreateApplicationRequest;
import com.nikolas.deploymenttracker.dto.DeploymentResponse;
import com.nikolas.deploymenttracker.model.Application;
import com.nikolas.deploymenttracker.service.ApplicationService;
import com.nikolas.deploymenttracker.service.DeploymentService;
import com.nikolas.deploymenttracker.model.DeploymentEnvironment;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final DeploymentService deploymentService;

    public ApplicationController(
            ApplicationService applicationService,
            DeploymentService deploymentService) {

        this.applicationService = applicationService;
        this.deploymentService = deploymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse createApplication(
            @RequestBody CreateApplicationRequest request) {

        Application application =
                applicationService.createApplication(request.name());

        return new ApplicationResponse(
                application.getId(),
                application.getName()
        );
    }

    @GetMapping
    public List<ApplicationResponse> getAllApplications() {

        return applicationService.getAllApplications()
                .stream()
                .map(application -> new ApplicationResponse(
                        application.getId(),
                        application.getName()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplicationById(
            @PathVariable Long id) {

        Application application =
                applicationService.getApplicationById(id);

        return new ApplicationResponse(
                application.getId(),
                application.getName()
        );
    }

    @GetMapping("/{id}/deployments")
    public List<DeploymentResponse> getApplicationDeployments(
            @PathVariable Long id) {

        return deploymentService.getDeploymentsByApplicationId(id);
    }


@GetMapping("/{id}/environments/{environment}/latest")
public DeploymentResponse getLatestDeployment(
        @PathVariable Long id,
        @PathVariable DeploymentEnvironment environment) {

    return deploymentService.getLatestSuccessfulDeployment(
            id,
            environment
    );
}

}
