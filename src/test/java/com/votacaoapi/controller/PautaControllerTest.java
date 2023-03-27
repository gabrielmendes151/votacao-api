package com.votacaoapi.controller;

import com.votacaoapi.enums.SessaoStatus;
import com.votacaoapi.model.Associado;
import com.votacaoapi.model.Pauta;
import com.votacaoapi.model.PautasVotadasAssociados;
import com.votacaoapi.model.Sessao;
import com.votacaoapi.repository.AssociadoRepository;
import com.votacaoapi.repository.PautaRepository;
import com.votacaoapi.repository.PautasVotadasAssociadosRepository;
import com.votacaoapi.repository.SessaoRepository;
import com.votacaoapi.request.AbrirSessaoRequest;
import com.votacaoapi.request.PautaRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PautaControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private SessaoRepository sessaoRepository;

    @Autowired
    private AssociadoRepository associadoRepository;

    @Autowired
    private PautasVotadasAssociadosRepository pautasVotadasAssociadosRepository;

    @AfterAll
    public void limparDb() {
        pautaRepository.deleteAll().block();
        sessaoRepository.deleteAll().block();
        associadoRepository.deleteAll().block();
        pautasVotadasAssociadosRepository.deleteAll().block();
    }

    @Test
    public void deveSalvarPautaComSessaoIdNull() {
        Flux<String> pautaSavedId = webTestClient.post()
            .uri("/api/v1/pautas")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(
                new PautaRequest("fechar portoes")))
            .exchange()
            .expectStatus().isCreated()
            .returnResult(String.class).getResponseBody()
            .log();

        pautaSavedId.next().subscribe(Assertions::assertNotNull);
    }

    @Test
    public void deveAbrirUmaSessaoDefault() {
        var pauta = pautaRepository.save(new Pauta("fechar portao")).block();

        webTestClient.put()
            .uri("/api/v1/pautas/" + pauta.getId() + "/abrir-sessao")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(
                new AbrirSessaoRequest()))
            .exchange()
            .expectStatus().isNoContent()
            .returnResult(Void.class).getResponseBody()
            .log();

        var res = pautaRepository.findById(pauta.getId()).block();
        var sessao = sessaoRepository.findByPautaId(pauta.getId()).block();

        assertNotNull(res.getSessaoId());
        assertEquals(res.getDescricao(), "fechar portao");
        assertEquals(sessao.getPautaId(), res.getId());
        assertNotNull(sessao.getDataHorafim());
        assertNotNull(sessao.getDataHorainicio());
    }

    @Test
    public void deveAbrirUmaSessaoCustomoizada() {
        var pauta = pautaRepository.save(new Pauta("fechar portao")).block();
        var request = AbrirSessaoRequest.builder()
            .dataHorainicio("20/10/2023 10:00")
            .dataHorafim("20/10/2023 11:00")
            .build();


        webTestClient.put()
            .uri("/api/v1/pautas/" + pauta.getId() + "/abrir-sessao")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(
                request))
            .exchange()
            .expectStatus().isNoContent()
            .returnResult(Void.class).getResponseBody()
            .log();

        Pauta res = pautaRepository.findById(pauta.getId()).block();
        var sessao = sessaoRepository.findByPautaId(pauta.getId()).block();

        assertNotNull(res.getSessaoId());
        assertEquals(res.getDescricao(), "fechar portao");
        assertEquals(sessao.getPautaId(), res.getId());
        assertEquals(sessao.getDataHorafim(), "20/10/2023 11:00");
        assertEquals(sessao.getDataHorainicio(), "20/10/2023 10:00");
    }

    @Test
    public void deveTrazerOResultadoDaVotacao() {
        var pauta = pautaRepository.save(new Pauta("fechar portao")).block();
        var sessaodto = Sessao.builder()
            .pautaId(pauta.getId())
            .dataHorainicio(LocalDateTime.now().toString())
            .dataHorafim(LocalDateTime.now().toString())
            .status(SessaoStatus.FINALIZADA)
            .build();
        var sessao = sessaoRepository.save(sessaodto).block();
        pauta.setSessaoId(sessao.getId());
        pautaRepository.save(pauta).block();
        Associado gabriel = associadoRepository.save(new Associado("gabriel")).block();
        Associado gabriel2 = associadoRepository.save(new Associado("gabriel2")).block();
        Associado gabriel3 = associadoRepository.save(new Associado("gabriel3")).block();
        pautasVotadasAssociadosRepository.save(PautasVotadasAssociados.create("Sim", pauta.getId(), gabriel.getId())).block();
        pautasVotadasAssociadosRepository.save(PautasVotadasAssociados.create("Sim", pauta.getId(), gabriel2.getId())).block();
        pautasVotadasAssociadosRepository.save(PautasVotadasAssociados.create("Não", pauta.getId(), gabriel3.getId())).block();


        String res = webTestClient.get()
            .uri("/api/v1/pautas/" + pauta.getId() + "/resultado-votacao")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .returnResult(String.class).getResponseBody()
            .log()
            .blockFirst();

        assertEquals(res, "Resultado da pauta %s foi de APROVADA com um total de 2 votos a favor e um total de 1 votos contra".formatted(pauta.getId()));
    }
}
