
package com.nikolas.deploymenttracker.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nikolas.deploymenttracker.model.Application;
import com.nikolas.deploymenttracker.repository.ApplicationRepository;
import com.nikolas.deploymenttracker.exception.DuplicateApplicationException;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public Application createApplication(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Application name cannot be empty"
            );
        }

        String normalizedName = name.trim();

        if (applicationRepository.existsByName(normalizedName)) {
            throw new DuplicateApplicationException(normalizedName);
        }

        Application application = new Application(normalizedName);

        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Application not found: " + id
                ));
    }
}
