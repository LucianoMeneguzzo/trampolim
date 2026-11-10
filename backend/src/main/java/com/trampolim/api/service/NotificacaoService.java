package com.trampolim.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// Avisos por e-mail: o mentor fica sabendo de inscrições e entregas, e o aluno, da decisão e da avaliação.
//
// Funciona com qualquer servidor SMTP (Gmail, Brevo, Mailtrap...), configurado pelas variáveis MAIL_*.
// Sem MAIL_HOST o aviso só aparece no log: o projeto continua rodando sem configurar nada.
//
// Os métodos são @Async: o e-mail sai em segundo plano, então a pessoa não espera o servidor de
// e-mail responder. E se o envio falhar, a inscrição ou a avaliação já foi gravada do mesmo jeito.
@Service
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);

    private final ObjectProvider<JavaMailSender> carteiro;
    private final String servidor;
    private final String remetente;
    private final String enderecoDoFront;

    public NotificacaoService(ObjectProvider<JavaMailSender> carteiro,
                              @Value("${spring.mail.host:}") String servidor,
                              @Value("${app.email.remetente:}") String remetente,
                              @Value("${app.cors.allowed-origins:http://localhost:5173}") String origensDoFront) {
        this.carteiro = carteiro;
        this.servidor = servidor;
        this.remetente = remetente;
        // O primeiro endereço liberado no CORS é o do front: é ele que vai nos links dos e-mails
        this.enderecoDoFront = origensDoFront.split(",")[0].trim().replaceAll("/$", "");
    }

    @Async
    public void novaInscricao(String emailDoMentor, String nomeDoMentor, String nomeDoAluno, String tituloDoDesafio) {
        enviar(emailDoMentor, "Nova inscrição em \"" + tituloDoDesafio + "\"",
                "Olá, " + nomeDoMentor + "!\n\n"
                        + nomeDoAluno + " se inscreveu no seu desafio \"" + tituloDoDesafio + "\".\n\n"
                        + "Veja as inscrições no seu perfil: " + enderecoDoFront + "/business/profile");
    }

    @Async
    public void projetoEnviado(String emailDoMentor, String nomeDoMentor, String nomeDoAluno, String tituloDoDesafio, Long candidaturaId) {
        enviar(emailDoMentor, "Entrega nova em \"" + tituloDoDesafio + "\"",
                "Olá, " + nomeDoMentor + "!\n\n"
                        + nomeDoAluno + " enviou o projeto do desafio \"" + tituloDoDesafio + "\" e aguarda a sua avaliação.\n\n"
                        + "Avalie aqui: " + enderecoDoFront + "/business/assessment/" + candidaturaId);
    }

    @Async
    public void inscricaoDecidida(String emailDoAluno, String nomeDoAluno, String tituloDoDesafio, boolean aprovada) {
        String texto = aprovada
                ? "Sua inscrição no desafio \"" + tituloDoDesafio + "\" foi aprovada. Agora é só enviar o seu projeto."
                : "Sua inscrição no desafio \"" + tituloDoDesafio + "\" não foi aprovada desta vez. Há outros desafios abertos esperando por você.";
        enviar(emailDoAluno, (aprovada ? "Inscrição aprovada: " : "Sobre a sua inscrição: ") + tituloDoDesafio,
                "Olá, " + nomeDoAluno + "!\n\n" + texto + "\n\n"
                        + "Acompanhe pelo seu perfil: " + enderecoDoFront + "/student/profile");
    }

    @Async
    public void entregaAvaliada(String emailDoAluno, String nomeDoAluno, String tituloDoDesafio, int nota, Long alunoId) {
        enviar(emailDoAluno, "Sua entrega foi avaliada: " + tituloDoDesafio,
                "Olá, " + nomeDoAluno + "!\n\n"
                        + "Sua entrega do desafio \"" + tituloDoDesafio + "\" recebeu nota " + nota + " de 10.\n"
                        + "O feedback completo está no seu perfil, e o trabalho já aparece no seu portfólio público:\n"
                        + enderecoDoFront + "/student/profile/" + alunoId);
    }

    private void enviar(String para, String assunto, String texto) {
        // Um título de desafio não pode quebrar a linha e "inventar" outros cabeçalhos no e-mail
        String assuntoLimpo = assunto.replaceAll("[\\r\\n]+", " ");

        JavaMailSender envio = servidor.isBlank() ? null : carteiro.getIfAvailable();
        if (envio == null) {
            log.info("E-mail não configurado (defina MAIL_HOST). Aviso que seria enviado: {}", assuntoLimpo);
            return;
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(para);
        mensagem.setSubject("[Trampolim] " + assuntoLimpo);
        mensagem.setText(texto + "\n\nEquipe Trampolim");

        try {
            envio.send(mensagem);
        } catch (MailException e) {
            // Falha de e-mail nunca derruba a ação principal. O endereço de quem recebe não vai para o log.
            log.warn("Não foi possível enviar o e-mail \"{}\": {}", assuntoLimpo, e.getMessage());
        }
    }
}
