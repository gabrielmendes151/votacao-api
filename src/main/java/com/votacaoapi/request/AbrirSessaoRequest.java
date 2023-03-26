package com.votacaoapi.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbrirSessaoRequest {
    private String dataHorainicio;

    private String dataHorafim;
}
