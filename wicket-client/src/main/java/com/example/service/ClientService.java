package com.example.service;

import com.example.model.Client;
import com.example.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    @Transactional
    public Client save(Client client) {
        validateClient(client);
        return clientRepository.save(client);
    }

    @Transactional
    public void delete(Long id) {
        clientRepository.deleteById(id);
    }

    private void validateClient(Client client) {
        if (client.getPersonType() == Client.PersonType.INDIVIDUAL) {
            if (client.getCpf() == null || client.getCpf().trim().isEmpty()) {
                throw new IllegalArgumentException("CPF is required for individual clients");
            }
            if (clientRepository.existsByCpf(client.getCpf()) && client.getId() == null) {
                throw new IllegalArgumentException("CPF already exists");
            }
        } else {
            if (client.getCnpj() == null || client.getCnpj().trim().isEmpty()) {
                throw new IllegalArgumentException("CNPJ is required for company clients");
            }
            if (clientRepository.existsByCnpj(client.getCnpj()) && client.getId() == null) {
                throw new IllegalArgumentException("CNPJ already exists");
            }
        }
    }
}