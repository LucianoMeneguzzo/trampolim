package com.trampolim.api.security;

import com.trampolim.api.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

// Freio contra força bruta: conta quantas vezes uma chave (ex.: "login|IP|e-mail") apareceu
// dentro de uma janela de tempo e recusa com 429 quando passa do limite.
// A contagem fica na memória do processo: simples e suficiente para uma instância do back.
// Com várias instâncias, cada uma teria a própria contagem (aí o caminho seria um Redis).
@Component
public class LimiteDeTentativas {

    // Acima disso, a limpeza roda para a memória não crescer sem fim
    private static final int MAXIMO_DE_CHAVES = 10_000;

    private record Janela(long expiraEm, int total) {}

    private final Map<String, Janela> janelas = new ConcurrentHashMap<>();
    private final LongSupplier relogio;

    public LimiteDeTentativas() {
        this(System::currentTimeMillis);
    }

    // Os testes passam um relógio próprio para "avançar o tempo" sem esperar de verdade
    LimiteDeTentativas(LongSupplier relogio) {
        this.relogio = relogio;
    }

    // Lança 429 se a chave já atingiu o limite e a janela ainda não terminou
    public void exigirLivre(String chave, int limite) {
        Janela janela = janelas.get(chave);
        if (janela != null && janela.expiraEm() > relogio.getAsLong() && janela.total() >= limite) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Aguarde alguns minutos e tente de novo.");
        }
    }

    // Soma uma tentativa. A janela começa na primeira e não se renova com as seguintes.
    public void registrar(String chave, Duration duracao) {
        long agora = relogio.getAsLong();
        janelas.merge(chave, new Janela(agora + duracao.toMillis(), 1), (atual, nova) ->
                atual.expiraEm() <= agora ? nova : new Janela(atual.expiraEm(), atual.total() + 1));

        if (janelas.size() > MAXIMO_DE_CHAVES) {
            janelas.values().removeIf(janela -> janela.expiraEm() <= agora);
        }
    }

    // Chamado quando dá certo (ex.: login com a senha correta)
    public void zerar(String chave) {
        janelas.remove(chave);
    }
}
