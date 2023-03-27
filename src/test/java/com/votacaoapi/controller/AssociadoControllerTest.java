package com.votacaoapi.controller;

import com.votacaoapi.enums.SessaoStatus;
import com.votacaoapi.enums.Voto;
import com.votacaoapi.model.Associado;
import com.votacaoapi.model.Pauta;
import com.votacaoapi.model.Sessao;
import com.votacaoapi.repository.AssociadoRepository;
import com.votacaoapi.repository.PautaRepository;
import com.votacaoapi.repository.PautasVotadasAssociadosRepository;
import com.votacaoapi.repository.SessaoRepository;
import com.votacaoapi.request.AssociadoRequest;
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

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssociadoControllerTest {

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
    public void deveSalvarAssociado() {
        Flux<String> pautaSavedId = webTestClient.post()
            .uri("/api/v1/associados")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(
                new AssociadoRequest("gabriel")))
            .exchange()
            .expectStatus().isCreated()
            .returnResult(String.class).getResponseBody()
            .log();

        pautaSavedId.next().subscribe(Assertions::assertNotNull);
    }

    @Test
    public void deveVotarEmUmaSessaoAberta() {
        var pauta = pautaRepository.save(new Pauta("fechar portao")).block();
        var sessaodto = Sessao.builder()
            .pautaId(pauta.getId())
            .dataHorainicio(LocalDateTime.now().toString())
            .dataHorafim(LocalDateTime.now().toString())
            .status(SessaoStatus.ABERTA)
            .build();
        var sessao = sessaoRepository.save(sessaodto).block();
        pauta.setSessaoId(sessao.getId());
        pautaRepository.save(pauta).block();
        Associado associado = associadoRepository.save(new Associado("gabriel")).block();


        webTestClient.put()
            .uri(uriBuilder -> uriBuilder
                .path("/api/v1/associados/{id}/votar-pauta/{idPauta}")
                .queryParam("voto", "Sim")
                .build(associado.getId(), pauta.getId()))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent()
            .returnResult(Void.class).getResponseBody()
            .log();

        var response = pautasVotadasAssociadosRepository.findAll().blockFirst();

        assertEquals(response.getAssociadoId(), associado.getId());
        assertEquals(response.getVoto(), Voto.SIM);
    }
}

