package com.votacaoapi.repository;

import com.votacaoapi.enums.SessaoStatus;
import com.votacaoapi.model.Sessao;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SessaoRepository extends ReactiveCrudRepository<Sessao, String> {

    Mono<Sessao> findByPautaId(String pautaId);

    Flux<Sessao> findAllByStatusIsNot(SessaoStatus sessaoStatus);
}
