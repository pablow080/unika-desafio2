package com.example.service;

import com.example.model.Endereco;
import com.example.repository.EnderecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;

    @Autowired
    public EnderecoService(EnderecoRepository enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }

    @Transactional(readOnly = true)
    public List<Endereco> listarEnderecosPorCliente(Long clienteId) {
        return enderecoRepository.findAllByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public Endereco buscarPorId(Long id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com ID: " + id));
    }

    @Transactional
    public Endereco salvar(Endereco endereco) {
        validarEndereco(endereco);
        return enderecoRepository.save(endereco);
    }

    @Transactional
    public void excluir(Long id) {
        Endereco endereco = buscarPorId(id);
        if (endereco.isEnderecoPrincipal()) {
            throw new IllegalStateException("Não é possível excluir o endereço principal");
        }
        enderecoRepository.delete(endereco);
    }

    private void validarEndereco(Endereco endereco) {
        if (endereco.getLogradouro() == null || endereco.getLogradouro().trim().isEmpty()) {
            throw new IllegalArgumentException("Logradouro é obrigatório");
        }

        if (endereco.getCep() == null || endereco.getCep().trim().isEmpty()) {
            throw new IllegalArgumentException("CEP é obrigatório");
        }

        if (endereco.getCidade() == null || endereco.getCidade().trim().isEmpty()) {
            throw new IllegalArgumentException("Cidade é obrigatória");
        }

        if (endereco.getEstado() == null || endereco.getEstado().trim().isEmpty()) {
            throw new IllegalArgumentException("Estado é obrigatório");
        }
    }
}