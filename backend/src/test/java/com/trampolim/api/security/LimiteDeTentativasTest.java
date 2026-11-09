package com.trampolim.api.security;

import com.trampolim.api.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Teste de unidade: o relógio é uma variável, então dá para "esperar" 15 minutos numa linha
class LimiteDeTentativasTest {

    private static final Duration QUINZE_MINUTOS = Duration.ofMinutes(15);

    private final AtomicLong agora = new AtomicLong(1_000_000);
    private final LimiteDeTentativas limite = new LimiteDeTentativas(agora::get);

    private void avancar(Duration tempo) {
        agora.addAndGet(tempo.toMillis());
    }

    @Test
    void bloqueiaQuandoAtingeOLimite() {
        for (int i = 0; i < 3; i++) {
            limite.exigirLivre("login|ana", 3);
            limite.registrar("login|ana", QUINZE_MINUTOS);
        }

        assertThatThrownBy(() -> limite.exigirLivre("login|ana", 3))
                .isInstanceOf(ApiException.class)
                .satisfies(erro -> org.assertj.core.api.Assertions.assertThat(((ApiException) erro).getStatus())
                        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    @Test
    void cadaChaveTemAPropriaContagem() {
        for (int i = 0; i < 3; i++) {
            limite.registrar("login|ana", QUINZE_MINUTOS);
        }

        assertThatCode(() -> limite.exigirLivre("login|bruno", 3)).doesNotThrowAnyException();
    }

    @Test
    void liberaDepoisQueAJanelaTermina() {
        for (int i = 0; i < 3; i++) {
            limite.registrar("login|ana", QUINZE_MINUTOS);
        }

        avancar(Duration.ofMinutes(14));
        assertThatThrownBy(() -> limite.exigirLivre("login|ana", 3)).isInstanceOf(ApiException.class);

        avancar(Duration.ofMinutes(1));
        assertThatCode(() -> limite.exigirLivre("login|ana", 3)).doesNotThrowAnyException();

        // A contagem recomeça do zero: uma tentativa nova não bloqueia de novo
        limite.registrar("login|ana", QUINZE_MINUTOS);
        assertThatCode(() -> limite.exigirLivre("login|ana", 3)).doesNotThrowAnyException();
    }

    @Test
    void novasTentativasNaoEsticamAJanela() {
        limite.registrar("login|ana", QUINZE_MINUTOS);
        avancar(Duration.ofMinutes(10));
        limite.registrar("login|ana", QUINZE_MINUTOS);
        limite.registrar("login|ana", QUINZE_MINUTOS);

        // A janela começou na primeira tentativa, então termina 15 minutos depois dela
        avancar(Duration.ofMinutes(5));
        assertThatCode(() -> limite.exigirLivre("login|ana", 3)).doesNotThrowAnyException();
    }

    @Test
    void zerarLiberaNaHora() {
        for (int i = 0; i < 3; i++) {
            limite.registrar("login|ana", QUINZE_MINUTOS);
        }

        limite.zerar("login|ana");

        assertThatCode(() -> limite.exigirLivre("login|ana", 3)).doesNotThrowAnyException();
    }

    @Test
    void descartaJanelasVencidasQuandoAcumulaChavesDemais() {
        for (int i = 0; i < 10_000; i++) {
            limite.registrar("antiga|" + i, Duration.ofMinutes(1));
        }
        limite.registrar("recente", QUINZE_MINUTOS);
        limite.registrar("recente", QUINZE_MINUTOS);

        // Passa do teto de chaves com as antigas já vencidas: a limpeza roda e mantém só o que vale
        avancar(Duration.ofMinutes(2));
        limite.registrar("gatilho", QUINZE_MINUTOS);

        assertThatThrownBy(() -> limite.exigirLivre("recente", 2)).isInstanceOf(ApiException.class);
        assertThatCode(() -> limite.exigirLivre("antiga|1", 1)).doesNotThrowAnyException();
    }
}
