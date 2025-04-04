package com.example.repository;

import com.example.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByCpf(String cpf);
    boolean existsByCnpj(String cnpj);
}