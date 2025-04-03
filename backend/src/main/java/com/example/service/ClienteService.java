package com.example.service;

import com.example.model.Cliente;
import com.example.model.Endereco;
import com.example.repository.ClienteRepository;
import com.example.repository.EnderecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository, EnderecoRepository enderecoRepository) {
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Cliente> listarClientes(Pageable pageable) {
        return clienteRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findByIdWithEnderecos(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + id));
    }

    @Transactional
    public Cliente salvar(Cliente cliente) {
        validarCliente(cliente);
        
        for (Endereco endereco : cliente.getEnderecos()) {
            endereco.setCliente(cliente);
        }
        
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Long id, Cliente cliente) {
        Cliente clienteExistente = buscarPorId(id);
        
        if (!clienteExistente.getCpfCnpj().equals(cliente.getCpfCnpj())) {
            throw new IllegalArgumentException("Não é permitido alterar o CPF/CNPJ do cliente");
        }
        
        validarCliente(cliente);
        cliente.setId(id);
        
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }

    private void validarCliente(Cliente cliente) {
        if (cliente.getCpfCnpj() == null || cliente.getCpfCnpj().trim().isEmpty()) {
            throw new IllegalArgumentException("CPF/CNPJ é obrigatório");
        }

        if (cliente.getTipoPessoa() == null) {
            throw new IllegalArgumentException("Tipo de pessoa é obrigatório");
        }

        if (cliente.getId() == null && clienteRepository.existsByCpfCnpj(cliente.getCpfCnpj())) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com este CPF/CNPJ");
        }

        validarEnderecos(cliente.getEnderecos());
    }

    private void validarEnderecos(List<Endereco> enderecos) {
        if (enderecos == null || enderecos.isEmpty()) {
            throw new IllegalArgumentException("Cliente deve ter pelo menos um endereço");
        }

        boolean temEnderecoPrincipal = false;
        for (Endereco endereco : enderecos) {
            if (endereco.isEnderecoPrincipal()) {
                if (temEnderecoPrincipal) {
                    throw new IllegalArgumentException("Cliente não pode ter mais de um endereço principal");
                }
                temEnderecoPrincipal = true;
            }
        }

        if (!temEnderecoPrincipal) {
            throw new IllegalArgumentException("Cliente deve ter um endereço principal");
        }
    }
}