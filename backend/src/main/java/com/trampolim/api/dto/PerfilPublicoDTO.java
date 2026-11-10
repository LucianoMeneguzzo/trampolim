package com.trampolim.api.dto;

import com.trampolim.api.entity.Avaliacao;
import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.entity.User;
import com.trampolim.api.entity.UserType;
import java.time.LocalDateTime;
import java.util.List;

// Resposta de GET /api/usuarios/{id}: é pública, por isso não inclui o e-mail
public class PerfilPublicoDTO {

    private Long id;
    private String name;
    private UserType type;
    private String bio;
    private String github;
    private String linkedin;
    private String professionalTitle;
    private List<TrabalhoValidado> trabalhos;

    public PerfilPublicoDTO(User usuario, List<Avaliacao> avaliacoes) {
        this.id = usuario.getId();
        this.name = usuario.getName();
        this.type = usuario.getType();
        this.bio = usuario.getBio();
        this.github = usuario.getGithub();
        this.linkedin = usuario.getLinkedin();
        this.professionalTitle = usuario.getProfessionalTitle();
        this.trabalhos = avaliacoes.stream().map(TrabalhoValidado::new).toList();
    }

    // Uma entrega avaliada por um mentor: é o que transforma o perfil em portfólio
    public static class TrabalhoValidado {

        private Long candidaturaId;
        private String tituloDesafio;
        private String tecnologias;
        private String nomeMentor;
        private Integer nota;
        private String feedback;
        private String linkEnvio;
        private LocalDateTime dataEnvio;
        private LocalDateTime dataAvaliacao;

        TrabalhoValidado(Avaliacao avaliacao) {
            Candidatura candidatura = avaliacao.getCandidatura();
            this.candidaturaId = candidatura.getId();
            this.tituloDesafio = candidatura.getDesafio().getTitulo();
            this.tecnologias = candidatura.getDesafio().getTecnologias();
            this.nomeMentor = avaliacao.getMentor().getName();
            this.nota = avaliacao.getNota();
            this.feedback = avaliacao.getFeedback();
            this.linkEnvio = candidatura.getLinkEnvio();
            this.dataEnvio = candidatura.getDataEnvio();
            this.dataAvaliacao = avaliacao.getDataAvaliacao();
        }

        // Getters
        public Long getCandidaturaId() { return candidaturaId; }
        public String getTituloDesafio() { return tituloDesafio; }
        public String getTecnologias() { return tecnologias; }
        public String getNomeMentor() { return nomeMentor; }
        public Integer getNota() { return nota; }
        public String getFeedback() { return feedback; }
        public String getLinkEnvio() { return linkEnvio; }
        public LocalDateTime getDataEnvio() { return dataEnvio; }
        public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public UserType getType() { return type; }
    public String getBio() { return bio; }
    public String getGithub() { return github; }
    public String getLinkedin() { return linkedin; }
    public String getProfessionalTitle() { return professionalTitle; }
    public List<TrabalhoValidado> getTrabalhos() { return trabalhos; }
}
