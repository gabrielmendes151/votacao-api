package com.votacaoapi.controller.v1;

import com.votacaoapi.request.AbrirSessaoRequest;
import com.votacaoapi.request.PautaRequest;
import com.votacaoapi.service.PautaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pautas")
public class PautaController {

    private final PautaService service;

    @PostMapping
    public Mono<ResponseEntity<String>> salvar(@RequestBody PautaRequest request) {
        return service.salvar(request)
            .flatMap(pautaId -> Mono.just(ResponseEntity.status(201).body(pautaId)))
            .doOnSuccess(res -> log.info("Pauta salva com sucesso"));
    }

    @PutMapping("{id}/abrir-sessao")
    public Mono<ResponseEntity<Void>> abrirSessao(@PathVariable("id") String id, @RequestBody AbrirSessaoRequest abrirSessaoRequest) {
         return service.abrirSessao(id, abrirSessaoRequest)
             .then(Mono.just(ResponseEntity.noContent().build()));

    }

    @GetMapping("{id}/resultado-votacao")
    public Mono<ResponseEntity<String>> fetchResultadoVotacao(@PathVariable("id") String id) {
        return service.fetchResultadoVotacao(id)
            .flatMap(resultado -> Mono.just(ResponseEntity.ok(resultado)));
    }
}
