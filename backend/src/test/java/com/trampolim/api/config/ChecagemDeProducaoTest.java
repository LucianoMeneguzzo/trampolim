package com.trampolim.api.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChecagemDeProducaoTest {

    private static final String BANCO = "jdbc:postgresql://db:5432/trampolim";
    private static final String CHAVE = "uma-chave-de-teste-com-mais-de-32-caracteres";
    private static final String FRONT = "https://trampolim.onrender.com";

    @Test
    void aceitaConfiguracaoCompleta() {
        assertThatCode(() -> new ChecagemDeProducao(BANCO, CHAVE, FRONT)).doesNotThrowAnyException();
    }

    @Test
    void recusaBancoEmMemoria() {
        assertThatThrownBy(() -> new ChecagemDeProducao("jdbc:h2:mem:trampolimdb", CHAVE, FRONT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB_URL");
    }

    @Test
    void recusaChaveJwtAusenteCurtaOuDeExemplo() {
        assertThatThrownBy(() -> new ChecagemDeProducao(BANCO, " ", FRONT))
                .hasMessageContaining("JWT_SECRET não foi definida");
        assertThatThrownBy(() -> new ChecagemDeProducao(BANCO, "curta", FRONT))
                .hasMessageContaining("pelo menos 32 caracteres");
        assertThatThrownBy(() -> new ChecagemDeProducao(BANCO, ChecagemDeProducao.CHAVE_DE_EXEMPLO, FRONT))
                .hasMessageContaining("valor de exemplo");
    }

    @Test
    void recusaSemEnderecoDoFrontEListaTudoOQueFalta() {
        assertThatThrownBy(() -> new ChecagemDeProducao("jdbc:h2:mem:trampolimdb", "", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB_URL")
                .hasMessageContaining("JWT_SECRET")
                .hasMessageContaining("CORS_ALLOWED_ORIGINS");
    }
}
