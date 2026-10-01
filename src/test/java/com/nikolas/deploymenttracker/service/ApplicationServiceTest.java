
package com.nikolas.deploymenttracker.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.nikolas.deploymenttracker.exception.DuplicateApplicationException;
import com.nikolas.deploymenttracker.model.Application;
import com.nikolas.deploymenttracker.repository.ApplicationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void createApplicationTrimsNameAndSavesIt() {

        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Application result = applicationService.createApplication(
                "  payment-service  "
        );

        assertEquals("payment-service", result.getName());

        verify(applicationRepository)
                .existsByName("payment-service");

        verify(applicationRepository)
                .save(any(Application.class));
    }

    @Test
    void createApplicationThrowsExceptionWhenNameAlreadyExists() {

        when(applicationRepository.existsByName("payment-service"))
                .thenReturn(true);

        DuplicateApplicationException exception = assertThrows(
                DuplicateApplicationException.class,
                () -> applicationService.createApplication("payment-service")
        );

        assertEquals(
                "Application already exists: payment-service",
                exception.getMessage()
        );

        verify(applicationRepository, never())
                .save(any(Application.class));
    }

    @Test
    void getApplicationByIdThrowsExceptionWhenNotFound() {

        when(applicationRepository.findById(9999L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> applicationService.getApplicationById(9999L)
        );

        assertEquals(
                "Application not found: 9999",
                exception.getMessage()
        );

        verify(applicationRepository).findById(9999L);
    }

    @Test
    void getAllApplicationsReturnsSavedApplications() {

        Application first = new Application("payment-service");
        Application second = new Application("user-service");

        when(applicationRepository.findAll())
                .thenReturn(List.of(first, second));

        List<Application> result = applicationService.getAllApplications();

        assertEquals(2, result.size());
        assertEquals("payment-service", result.get(0).getName());
        assertEquals("user-service", result.get(1).getName());

        verify(applicationRepository).findAll();
    }

    @Test
    void getApplicationByIdReturnsApplicationWhenFound() {

        Application application = new Application("payment-service");

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        Application result = applicationService.getApplicationById(1L);

        assertEquals("payment-service", result.getName());

        verify(applicationRepository).findById(1L);
    }

    @Test
    void createApplicationRejectsBlankName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.createApplication("   ")
        );

        verifyNoInteractions(applicationRepository);
    }
}
