package com.votacaoapi.repository;

import com.votacaoapi.model.Associado;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssociadoRepository extends ReactiveCrudRepository<Associado, String> {
}
