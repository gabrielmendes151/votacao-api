package com.votacaoapi.service;

import com.votacaoapi.Utils.DataUtils;
import com.votacaoapi.enums.SessaoStatus;
import com.votacaoapi.exceptions.ValidacaoException;
import com.votacaoapi.model.Sessao;
import com.votacaoapi.repository.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessaoService {

    private final SessaoRepository repository;

    public Mono<Void> validar(final String inicio, final String fim) {
        var dataInicio = DataUtils.convertStringToDateTime(inicio);
        var dataFim = DataUtils.convertStringToDateTime(fim);
        if (dataFim.isBefore(LocalDateTime.now())) {
            return Mono.error(new ValidacaoException("A data hora final da sessão não pode ser menor que a data hora atual"));
        }
        if (dataInicio.isAfter(dataFim)) {
            return Mono.error(new ValidacaoException("A data hora final da sessão não pode ser menor que a data hora inicio"));
        }
        return Mono.empty();
    }

    @CacheEvict(value = "sessoes", allEntries = true)
    public Mono<Sessao> salvar(final Sessao sessao) {
        return repository.save(sessao);
    }

    public Mono<Sessao> findByPautaId(final String pautaId) {
        return repository.findByPautaId(pautaId);
    }

    @Cacheable("sessoes")
    public Flux<Sessao> findAllSessoesDiferenteDeFinalizada() {
        return repository.findAllByStatusIsNot(SessaoStatus.FINALIZADA);
    }

}
