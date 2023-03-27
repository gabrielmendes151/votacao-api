package com.votacaoapi.service;

import com.votacaoapi.exceptions.ValidacaoException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class SessaoServiceTest {

    @InjectMocks
    private SessaoService sessaoService;

    @Test
    public void devePassarValidacoes() {
        StepVerifier
            .create(sessaoService.validar("22/10/2023 11:00", "22/10/2023 11:05"))
            .expectComplete()
            .verify();
    }

    @Test
    public void deveLancarUmExceptionQuandoDataFinalMenorQueInicial() {
        StepVerifier
            .create(sessaoService.validar("22/10/2023 11:10", "22/10/2023 11:05"))
            .expectErrorMatches(throwable -> throwable instanceof ValidacaoException &&
                throwable.getMessage().equals("A data hora final da sessão não pode ser menor que a data hora inicio")
            ).verify();
    }

    @Test
    public void deveLancarUmExceptionQuandoDataFinalForMenorQueCurrentDate() {
        StepVerifier
            .create(sessaoService.validar("22/10/2023 11:10", "22/10/2020 11:05"))
            .expectErrorMatches(throwable -> throwable instanceof ValidacaoException &&
                throwable.getMessage().equals("A data hora final da sessão não pode ser menor que a data hora atual")
            ).verify();
    }
}
