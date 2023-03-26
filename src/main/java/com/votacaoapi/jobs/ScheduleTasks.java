package com.votacaoapi.jobs;

import com.votacaoapi.Utils.DataUtils;
import com.votacaoapi.enums.SessaoStatus;
import com.votacaoapi.model.Sessao;
import com.votacaoapi.repository.SessaoRepository;
import com.votacaoapi.service.SessaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
public class ScheduleTasks {

    @Autowired
    private SessaoService sessaoService;

    @Autowired
    private SessaoRepository sessaoRepository;

    @Scheduled(cron = "*/1 * * * * *")
    public void verificarSessao() {
        sessaoService.findAllSessoesDiferenteDeFinalizada()
            .flatMap(sessao -> {
                if (deveAbrirSessao(sessao)) {
                    sessao.setStatus(SessaoStatus.ABERTA);
                }
                if (deveFinalizarSessao(sessao)) {
                    sessao.setStatus(SessaoStatus.FINALIZADA);
                    //todo mandar mensagem para o rabbitMq
                }
                sessaoService.salvar(sessao).subscribe();
                return Mono.empty();
            }).subscribe();
    }

    private Boolean deveAbrirSessao(final Sessao sessao) {
        var inicio = DataUtils.convertStringToDateTime(sessao.getDataHorainicio());
        var fim = DataUtils.convertStringToDateTime(sessao.getDataHorafim());
        return inicio.isBefore(LocalDateTime.now())
            && LocalDateTime.now().isBefore(fim);

    }

    private Boolean deveFinalizarSessao(final Sessao sessao) {
        var fim = DataUtils.convertStringToDateTime(sessao.getDataHorafim());
        return fim.isBefore(LocalDateTime.now());

    }
}
