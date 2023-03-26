package com.votacaoapi.model;

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
@Document(collection = "PAUTAS")
public class Pauta {


    public Pauta(String descricao) {
        this.descricao = descricao;
    }

    @Id
    private String id;

    private String descricao;

    private String  sessaoId;
}

