package com.votacaoapi.model;

import com.votacaoapi.Utils.DataUtils;
import com.votacaoapi.enums.SessaoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import static com.votacaoapi.Utils.DataUtils.convertLocalDateTimeToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "SESSOES")
public class Sessao {

    @Id
    private String id;

    private String dataHorainicio;

    private String dataHorafim;

    private SessaoStatus status;

    private String pautaId;

    public static Sessao criarSessao(final String inicio, final String fim, final String pautaId) {
        return Sessao.builder()
            .status(sessaoDeveSerAberta(inicio) ? SessaoStatus.ABERTA : SessaoStatus.CRIADA)
            .dataHorainicio(inicio)
            .dataHorafim(fim)
            .pautaId(pautaId)
            .build();
    }

    public static Sessao criarSessaoDefault(final String pautaId) {
        var now = convertLocalDateTimeToString(LocalDateTime.now().plusMinutes(1));
        return Sessao.builder()
            .status(SessaoStatus.ABERTA)
            .dataHorainicio(convertLocalDateTimeToString(LocalDateTime.now()))
            .dataHorafim(now)
            .pautaId(pautaId)
            .build();
    }

    private static Boolean sessaoDeveSerAberta(final String inicio) {
        var dataInicio = DataUtils.convertStringToDateTime(inicio);
        return dataInicio.isBefore(LocalDateTime.now());
    }
}

