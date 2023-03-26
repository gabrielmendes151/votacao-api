package com.votacaoapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.PRECONDITION_FAILED)
public class SessaoFinalizadaException extends RuntimeException {

    public SessaoFinalizadaException() {
        super("Sessão para votação de pauta já finalizada");
    }
}
