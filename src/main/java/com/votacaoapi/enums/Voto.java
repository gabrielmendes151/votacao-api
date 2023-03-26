package com.votacaoapi.enums;

import com.votacaoapi.exceptions.ValidacaoException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Voto {
    SIM("Sim"),
    NAO("Não");

    private final String descricao;

    Voto(String descricao) {
        this.descricao = descricao;
    }

    public static Voto findByDescricao(String descricao) {
        return Arrays.stream(Voto.values())
            .filter(voto -> voto.getDescricao().equals(descricao))
            .findFirst()
            .orElseThrow(() -> new ValidacaoException("Opção de voto inválida"));
    }
}
