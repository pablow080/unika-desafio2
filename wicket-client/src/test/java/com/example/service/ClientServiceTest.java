package com.example.service;

import com.example.model.Client;
import com.example.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client individualClient;
    private Client companyClient;

    @BeforeEach
    void setUp() {
        individualClient = new Client();
        individualClient.setPersonType(Client.PersonType.INDIVIDUAL);
        individualClient.setName("John Doe");
        individualClient.setCpf("12345678901");
        individualClient.setEmail("john@example.com");

        companyClient = new Client();
        companyClient.setPersonType(Client.PersonType.COMPANY);
        companyClient.setCompanyName("Acme Inc");
        companyClient.setCnpj("12345678901234");
        companyClient.setEmail("contact@acme.com");
    }

    @Test
    void shouldSaveValidIndividualClient() {
        when(clientRepository.save(any(Client.class))).thenReturn(individualClient);
        when(clientRepository.existsByCpf(anyString())).thenReturn(false);

        Client saved = clientService.save(individualClient);

        assertNotNull(saved);
        assertEquals("John Doe", saved.getName());
        assertEquals("12345678901", saved.getCpf());
        verify(clientRepository).save(individualClient);
    }

    @Test
    void shouldSaveValidCompanyClient() {
        when(clientRepository.save(any(Client.class))).thenReturn(companyClient);
        when(clientRepository.existsByCnpj(anyString())).thenReturn(false);

        Client saved = clientService.save(companyClient);

        assertNotNull(saved);
        assertEquals("Acme Inc", saved.getCompanyName());
        assertEquals("12345678901234", saved.getCnpj());
        verify(clientRepository).save(companyClient);
    }

    @Test
    void shouldThrowExceptionWhenSavingIndividualClientWithoutCpf() {
        individualClient.setCpf(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.save(individualClient);
        });

        assertEquals("CPF is required for individual clients", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingCompanyClientWithoutCnpj() {
        companyClient.setCnpj(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.save(companyClient);
        });

        assertEquals("CNPJ is required for company clients", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingClientWithDuplicateCpf() {
        when(clientRepository.existsByCpf(anyString())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.save(individualClient);
        });

        assertEquals("CPF already exists", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingClientWithDuplicateCnpj() {
        when(clientRepository.existsByCnpj(anyString())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.save(companyClient);
        });

        assertEquals("CNPJ already exists", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }
}