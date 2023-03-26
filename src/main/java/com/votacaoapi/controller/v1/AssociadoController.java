package com.votacaoapi.controller.v1;

import com.votacaoapi.request.AssociadoRequest;
import com.votacaoapi.service.AssociadoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/associados")
public class AssociadoController {

    private final AssociadoService service;

    @PostMapping
    public Mono<ResponseEntity<String>> salvar(@RequestBody AssociadoRequest request) {
        return service.salvar(request)
            .flatMap(associadoId -> Mono.just(ResponseEntity.status(201).body(associadoId)))
            .doOnSuccess(res -> log.info("Associado salvo com sucesso"));
    }

    @PutMapping("{id}/votar-pauta/{idPauta}")
    public Mono<ResponseEntity<?>> votarPauta(@PathVariable("id") String id, @PathVariable("idPauta") String idPauta,
                                              @RequestParam("voto") String voto) {
        return service.votar(id, idPauta, voto)
            .then(Mono.just(ResponseEntity.noContent().build()));

    }
}
