package com.trampolim.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// Liga o @Async: os métodos marcados com ele rodam em segundo plano.
// Hoje quem usa é o NotificacaoService, para o envio de e-mail não atrasar a resposta da API.
@Configuration
@EnableAsync
public class TarefasEmSegundoPlano {
}
