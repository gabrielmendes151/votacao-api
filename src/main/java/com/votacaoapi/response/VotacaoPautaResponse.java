package com.votacaoapi.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VotacaoPautaResponse {
    private Long votosAfavor = 0L;

    private Long votosContra = 0L;
}
