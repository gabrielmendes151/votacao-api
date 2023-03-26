package com.votacaoapi.model;

import com.votacaoapi.enums.Voto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "PAUTAS_VOTADAS_ASSOCIADOS")
public class PautasVotadasAssociados {
    @Id
    private String id;

    private String pautaId;

    private String associadoId;

    private Voto voto;

    public static PautasVotadasAssociados create(final String voto, final String pautaId, final String associadoId) {
        return PautasVotadasAssociados.builder()
            .voto(Voto.findByDescricao(voto))
            .pautaId(pautaId)
            .associadoId(associadoId)
            .build();
    }
}
