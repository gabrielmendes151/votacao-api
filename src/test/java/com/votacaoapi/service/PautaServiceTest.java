package com.votacaoapi.service;

import com.votacaoapi.enums.SessaoStatus;
import com.votacaoapi.model.Pauta;
import com.votacaoapi.model.Sessao;
import com.votacaoapi.repository.PautaRepository;
import com.votacaoapi.repository.PautasVotadasAssociadosRepository;
import com.votacaoapi.request.AbrirSessaoRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PautaServiceTest {

    @InjectMocks
    private PautaService pautaService;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoService sessaoService;

    @Mock
    private PautasVotadasAssociadosRepository pautasVotadasAssociadosRepository;

    @Test
    public void deveAbrirUmaSessaoDefault() {
        Pauta pauta = getPauta();

        Sessao sessao = getSessao(pauta);
        when(pautaRepository.findById(anyString())).thenReturn(Mono.just(pauta));
        when(sessaoService.validar(any(), any())).thenReturn(Mono.empty());
        when(sessaoService.salvar(any())).thenReturn(Mono.just(sessao));
        when(pautaRepository.save(any())).thenReturn(Mono.just(pauta));

        StepVerifier
            .create(pautaService.abrirSessao("1uuidhash", new AbrirSessaoRequest()))
            .expectComplete()
            .verify();

        verify(pautaRepository, times(1)).save(any());
    }

    @Test
    public void deveAbrirUmaSessaoCustomizada() {
        Pauta pauta = getPauta();

        var sessao = Sessao.builder()
            .pautaId(pauta.getId())
            .status(SessaoStatus.ABERTA)
            .id("1uuidhash")
            .dataHorafim(LocalDateTime.now().toString())
            .dataHorainicio(LocalDateTime.now().toString())
            .build();
        when(pautaRepository.findById(anyString())).thenReturn(Mono.just(pauta));
        when(sessaoService.validar(any(), any())).thenReturn(Mono.empty());
        when(sessaoService.salvar(any())).thenReturn(Mono.just(sessao));
        when(pautaRepository.save(any())).thenReturn(Mono.just(pauta));

        StepVerifier
            .create(pautaService.abrirSessao("1uuidhash", new AbrirSessaoRequest()))
            .expectComplete()
            .verify();

        verify(pautaRepository, times(1)).save(any());
    }

    @Test
    public void deveTrazerOResultadoDeUmaPautaFinalizada() {
        var pauta = getPauta();
        Sessao sessao = getSessao(pauta);
        sessao.setStatus(SessaoStatus.FINALIZADA);

        when(sessaoService.findByPautaId(any())).thenReturn(Mono.just(sessao));
        when(pautasVotadasAssociadosRepository.countByVotoAndPautaId(any(), any()))
            .thenReturn(Mono.just(3L))
            .thenReturn(Mono.just(5L));

        StepVerifier
            .create(pautaService.fetchResultadoVotacao("1uuidhash"))
            .expectNext("Resultado da pauta 1uuidhash foi de NEGADA com um total de 3 votos a favor e um total de 5 votos contra")
            .expectComplete()
            .verify();
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
