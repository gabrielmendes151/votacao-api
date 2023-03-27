package com.votacaoapi.service;

import com.votacaoapi.enums.SessaoStatus;
import com.votacaoapi.exceptions.ValidacaoException;
import com.votacaoapi.model.Associado;
import com.votacaoapi.model.Pauta;
import com.votacaoapi.model.PautasVotadasAssociados;
import com.votacaoapi.model.Sessao;
import com.votacaoapi.repository.AssociadoRepository;
import com.votacaoapi.repository.PautasVotadasAssociadosRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssociadoServiceTest {

    @InjectMocks
    private AssociadoService associadoService;

    @Mock
    private AssociadoRepository associadoRepository;

    @Mock
    private SessaoService sessaoService;

    @Mock
    private PautasVotadasAssociadosRepository pautasVotadasAssociadosRepository;

    @Test
    public void deveRealizarUmVoto() {
        var associado = Associado.builder().id("1ahha").nome("gabriel").build();
        var sessao = getSessao(getPauta());

        when(associadoRepository.findById(anyString())).thenReturn(Mono.just(associado));
        when(sessaoService.findByPautaId(anyString())).thenReturn(Mono.just(sessao));
        when(pautasVotadasAssociadosRepository.countByAssociadoIdAndPautaId(any(), anyString())).thenReturn(Mono.just(0L));
        when(pautasVotadasAssociadosRepository.save(any())).thenReturn(Mono.just(new PautasVotadasAssociados()));

        StepVerifier
            .create(associadoService.votar("1uuidhash", "1uuidhash", "Sim"))
            .expectComplete()
            .verify();

        verify(pautasVotadasAssociadosRepository, times(1)).save(any());
    }

    @Test
    public void deveRetornarErroAoTentarVotarEmUmaSessaoDiferenteDeAberta() {
        var associado = Associado.builder().id("1ahha").nome("gabriel").build();
        var sessao = getSessao(getPauta());
        sessao.setStatus(SessaoStatus.FINALIZADA);

        when(associadoRepository.findById(anyString())).thenReturn(Mono.just(associado));
        when(sessaoService.findByPautaId(anyString())).thenReturn(Mono.just(sessao));

        StepVerifier
            .create(associadoService.votar("1ahha", getPauta().getId(), "Sim"))
            .expectErrorMatches(throwable -> throwable instanceof ValidacaoException &&
                throwable.getMessage().equals("Sessão não se encontra aberta."))
            .verify();

        verify(pautasVotadasAssociadosRepository, never()).save(any());
    }

    @Test
    public void deveRetornarErroAoAssociadoTentarVotarMaisDeUmaVezNaMesmaSessao() {
        var associado = Associado.builder().id("1ahha").nome("gabriel").build();
        var sessao = getSessao(getPauta());

        when(associadoRepository.findById(anyString())).thenReturn(Mono.just(associado));
        when(sessaoService.findByPautaId(anyString())).thenReturn(Mono.just(sessao));
        when(pautasVotadasAssociadosRepository.countByAssociadoIdAndPautaId(any(), any())).thenReturn(Mono.just(1L));

        StepVerifier
            .create(associadoService.votar("1ahha", getPauta().getId(), "Sim"))
            .expectErrorMatches(throwable -> throwable instanceof ValidacaoException &&
                throwable.getMessage().equals("Este associado já votou nesta pauta."))
            .verify();

        verify(pautasVotadasAssociadosRepository, never()).save(any());
    }

    private static Pauta getPauta() {
        var pauta = Pauta.builder()
            .descricao("fechar portao")
            .id("1uuidhash")
            .build();
        return pauta;
    }

    private static Sessao getSessao(Pauta pauta) {
        var sessao = Sessao.builder()
            .pautaId(pauta.getId())
            .status(SessaoStatus.ABERTA)
            .id("1uuidhash")
            .build();
        return sessao;
    }
}
