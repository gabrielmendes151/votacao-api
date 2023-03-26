package com.votacaoapi.repository;

import com.votacaoapi.model.Pauta;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PautaRepository extends ReactiveCrudRepository<Pauta, String> {
}
