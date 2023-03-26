package com.votacaoapi.service;

import com.votacaoapi.enums.SessaoStatus;
import com.votacaoapi.exceptions.NotFoundException;
import com.votacaoapi.exceptions.ValidacaoException;
import com.votacaoapi.model.Associado;
import com.votacaoapi.model.PautasVotadasAssociados;
import com.votacaoapi.repository.AssociadoRepository;
import com.votacaoapi.repository.PautasVotadasAssociadosRepository;
import com.votacaoapi.request.AssociadoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class AssociadoService {

    private final AssociadoRepository repository;

    private final SessaoService sessaoService;

    private final PautasVotadasAssociadosRepository pautasVotadasAssociadosRepository;

    private Mono<Associado> findById(final String associadoId) {
        return repository.findById(associadoId)
            .switchIfEmpty(Mono.error(new NotFoundException("Associado não encontrado")));
    }

    public Mono<String> salvar(final AssociadoRequest associadoRequest) {
        return repository.save(new Associado(associadoRequest.getNome()))
            .map(Associado::getId);
    }

    public Mono<Void> votar(final String idAssociado, final String idPauta, final String voto) {
        return findById(idAssociado)
            .flatMap(associado -> sessaoService.findByPautaId(idPauta)
                .flatMap(sessao -> {
                    if (sessao.getStatus() == SessaoStatus.FINALIZADA || sessao.getStatus() == SessaoStatus.CRIADA) {
                        return Mono.error(new ValidacaoException("Sessão não se encontra aberta."));
                    }
                    return pautasVotadasAssociadosRepository.countByAssociadoIdAndPautaId(associado.getId(),
                            sessao.getPautaId())
                        .flatMap(value -> value > 0
                            ? Mono.error(new ValidacaoException("Este associado já votou nesta pauta."))
                            : Mono.empty())
                        .then(pautasVotadasAssociadosRepository.save(PautasVotadasAssociados.create(voto, idPauta, idAssociado))
                            .then());
                }));
    }
}
