package com.trampolim.api.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Teste de unidade: o servidor de e-mail é um dublê, então nada sai de verdade
class NotificacaoServiceTest {

    private final JavaMailSender carteiro = mock(JavaMailSender.class);

    @SuppressWarnings("unchecked")
    private NotificacaoService servico(String servidor) {
        ObjectProvider<JavaMailSender> fornecedor = mock(ObjectProvider.class);
        when(fornecedor.getIfAvailable()).thenReturn(carteiro);
        return new NotificacaoService(fornecedor, servidor, "nao-responda@trampolim.dev", "https://trampolim.dev/, http://localhost:5173");
    }

    private SimpleMailMessage enviado() {
        ArgumentCaptor<SimpleMailMessage> captura = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(carteiro).send(captura.capture());
        return captura.getValue();
    }

    @Test
    void avisaOMentorDaNovaInscricao() {
        servico("smtp.exemplo.com").novaInscricao("rafael@trampolim.dev", "Rafael", "Ana", "API de pedidos");

        SimpleMailMessage email = enviado();
        assertThat(email.getTo()).containsExactly("rafael@trampolim.dev");
        assertThat(email.getFrom()).isEqualTo("nao-responda@trampolim.dev");
        assertThat(email.getSubject()).isEqualTo("[Trampolim] Nova inscrição em \"API de pedidos\"");
        assertThat(email.getText()).contains("Olá, Rafael!", "Ana se inscreveu", "https://trampolim.dev/business/profile");
    }

    @Test
    void avisaOMentorDaEntregaComOLinkDaAvaliacao() {
        servico("smtp.exemplo.com").projetoEnviado("rafael@trampolim.dev", "Rafael", "Ana", "API de pedidos", 12L);

        assertThat(enviado().getText()).contains("Ana enviou o projeto", "https://trampolim.dev/business/assessment/12");
    }

    @Test
    void avisaOAlunoDaDecisao() {
        servico("smtp.exemplo.com").inscricaoDecidida("ana@trampolim.dev", "Ana", "API de pedidos", true);

        SimpleMailMessage email = enviado();
        assertThat(email.getTo()).containsExactly("ana@trampolim.dev");
        assertThat(email.getSubject()).isEqualTo("[Trampolim] Inscrição aprovada: API de pedidos");
        assertThat(email.getText()).contains("foi aprovada");
    }

    @Test
    void avisaOAlunoDaRecusaSemFalarEmAprovacao() {
        servico("smtp.exemplo.com").inscricaoDecidida("ana@trampolim.dev", "Ana", "API de pedidos", false);

        SimpleMailMessage email = enviado();
        assertThat(email.getSubject()).isEqualTo("[Trampolim] Sobre a sua inscrição: API de pedidos");
        assertThat(email.getText()).contains("não foi aprovada");
    }

    @Test
    void avisaOAlunoDaAvaliacaoComANotaEOPortfolio() {
        servico("smtp.exemplo.com").entregaAvaliada("ana@trampolim.dev", "Ana", "API de pedidos", 9, 5L);

        assertThat(enviado().getText()).contains("nota 9 de 10", "https://trampolim.dev/student/profile/5");
    }

    @Test
    void semServidorConfiguradoNaoTentaEnviar() {
        servico("").novaInscricao("rafael@trampolim.dev", "Rafael", "Ana", "API de pedidos");

        verify(carteiro, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void falhaNoEnvioNaoViraErro() {
        doThrow(new MailSendException("servidor fora do ar")).when(carteiro).send(any(SimpleMailMessage.class));

        assertThatCode(() -> servico("smtp.exemplo.com").novaInscricao("rafael@trampolim.dev", "Rafael", "Ana", "API de pedidos"))
                .doesNotThrowAnyException();
    }

    @Test
    void tituloComQuebraDeLinhaNaoQuebraOAssunto() {
        servico("smtp.exemplo.com").novaInscricao("rafael@trampolim.dev", "Rafael", "Ana", "API\r\nBcc: outra@pessoa.com");

        assertThat(enviado().getSubject()).doesNotContain("\r", "\n");
    }
}
