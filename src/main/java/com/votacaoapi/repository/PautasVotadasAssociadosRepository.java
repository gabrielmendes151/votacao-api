package com.votacaoapi.repository;

import com.votacaoapi.enums.Voto;
import com.votacaoapi.model.PautasVotadasAssociados;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PautasVotadasAssociadosRepository extends ReactiveCrudRepository<PautasVotadasAssociados, String> {

    Mono<Long> countByAssociadoIdAndPautaId(String associadoId, String pautaId);

    Mono<Long> countByVotoAndPautaId(Voto voto, String pautaId);

}
