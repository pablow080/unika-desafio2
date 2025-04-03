package com.example.repository;

import com.example.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
    
    @Query("SELECT e FROM Endereco e WHERE e.cliente.id = :clienteId")
    List<Endereco> findAllByClienteId(@Param("clienteId") Long clienteId);
    
    @Query("SELECT COUNT(e) > 0 FROM Endereco e WHERE e.cliente.id = :clienteId AND e.enderecoPrincipal = true")
    boolean existsEnderecoPrincipalByClienteId(@Param("clienteId") Long clienteId);
}