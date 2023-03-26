package com.votacaoapi.service;

import com.votacaoapi.enums.SessaoStatus;
import com.votacaoapi.enums.Voto;
import com.votacaoapi.exceptions.NotFoundException;
import com.votacaoapi.exceptions.ValidacaoException;
import com.votacaoapi.model.Pauta;
import com.votacaoapi.model.Sessao;
import com.votacaoapi.repository.PautaRepository;
import com.votacaoapi.repository.PautasVotadasAssociadosRepository;
import com.votacaoapi.request.AbrirSessaoRequest;
import com.votacaoapi.request.PautaRequest;
import com.votacaoapi.response.VotacaoPautaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PautaService {

    private final PautaRepository repository;

    private final SessaoService sessaoService;

    private final PautasVotadasAssociadosRepository pautasVotadasAssociadosRepository;

    public Mono<Pauta> findById(final String pautaId) {
        return repository.findById(pautaId)
            .switchIfEmpty(Mono.error(new NotFoundException("Pauta não encontrada")));
    }

    public Mono<String> salvar(final PautaRequest pautaRequest) {
        return repository.save(new Pauta(pautaRequest.getDescricao()))
            .map(Pauta::getId);
    }

    public Mono<Void> abrirSessao(final String pautaId, final AbrirSessaoRequest abrirSessaoRequest) {
        if (ObjectUtils.isEmpty(abrirSessaoRequest) || ObjectUtils.isEmpty(abrirSessaoRequest.getDataHorafim())
            || ObjectUtils.isEmpty(abrirSessaoRequest.getDataHorainicio())) {
            return abrirSessao(pautaId);
        } else {
            return abrirSessao(pautaId, abrirSessaoRequest.getDataHorainicio(), abrirSessaoRequest.getDataHorafim());
        }
    }

    public Mono<Void> abrirSessao(final String pautaId, final String inicio, final String fim) {
        return findById(pautaId)
            .flatMap(pauta ->
                sessaoService.validar(inicio, fim)
                    .then(sessaoService.salvar(Sessao.criarSessao(inicio, fim, pauta.getId())))
                    .flatMap(sessao -> {
                        pauta.setSessaoId(sessao.getId());
                        return repository.save(pauta);
                    }).then()
                    .onErrorResume(Mono::error));
    }

    public Mono<Void> abrirSessao(final String pautaId) {
        return findById(pautaId)
            .flatMap(pauta -> {
                var sessao = Sessao.criarSessaoDefault(pauta.getId());
                return sessaoService.salvar(sessao)
                    .flatMap(se -> {
                        pauta.setSessaoId(sessao.getId());
                        return repository.save(pauta);
                    });
            }).then();
    }


    public Mono<String> fetchResultadoVotacao(final String idPauta) {
        var votacaoPautaResponse = new VotacaoPautaResponse();
        return fetchSessao(idPauta)
            .onErrorResume(Mono::error)
            .flatMap(sessao -> Mono.zip(pautasVotadasAssociadosRepository.countByVotoAndPautaId(Voto.SIM, sessao.getPautaId()),
                    pautasVotadasAssociadosRepository.countByVotoAndPautaId(Voto.NAO, sessao.getPautaId()))
                .flatMap(tuple -> {
                    votacaoPautaResponse.setVotosAfavor(tuple.getT1());
                    votacaoPautaResponse.setVotosContra(tuple.getT2());
                    return Mono.just(votacaoPautaResponse);
                }).map(votosPauta ->
                    "Resultado da pauta " + idPauta + " foi de "
                        + fetchResultadoVotacao(votacaoPautaResponse) + " com um total de "
                        + votacaoPautaResponse.getVotosAfavor() + " votos a favor e um total de "
                        + votacaoPautaResponse.getVotosContra() + " votos contra"
                ));
    }


    private Mono<Sessao> fetchSessao(String idPauta) {
        return sessaoService.findByPautaId(idPauta)
            .flatMap(sessao -> {
                if (sessao.getStatus() != SessaoStatus.FINALIZADA) {
                    return Mono.error(new ValidacaoException("Sessão não se encontra finalizada"));
                }
                return Mono.just(sessao);
            })
            .switchIfEmpty(Mono.error(new NotFoundException("Não foi encontrada nenhuma sessão para essa pauta")));
    }

    public String fetchResultadoVotacao(final VotacaoPautaResponse votacaoPautaResponse) {
        return votacaoPautaResponse.getVotosAfavor() > votacaoPautaResponse.getVotosContra()
            ? "APROVADA"
            : "NEGADA";
    }

}
