
package com.nikolas.deploymenttracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nikolas.deploymenttracker.model.Application;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    Optional<Application> findByName(String name);

    boolean existsByName(String name);
}
