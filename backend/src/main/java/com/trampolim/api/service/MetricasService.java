package com.trampolim.api.service;

import com.trampolim.api.dto.MetricasDTO;
import com.trampolim.api.repository.AvaliacaoRepository;
import com.trampolim.api.repository.CandidaturaRepository;
import com.trampolim.api.repository.ChallengeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Números agregados do painel: não precisam de tabela própria, saem das que já existem
@Service
public class MetricasService {

    private static final int MESES_NO_GRAFICO = 6;

    private final ChallengeRepository challengeRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public MetricasService(ChallengeRepository challengeRepository, CandidaturaRepository candidaturaRepository,
                           AvaliacaoRepository avaliacaoRepository) {
        this.challengeRepository = challengeRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    public MetricasDTO calcular() {
        return new MetricasDTO(
                challengeRepository.count(),
                candidaturaRepository.count(),
                candidaturaRepository.countByLinkEnvioIsNotNull(),
                avaliacaoRepository.count(),
                inscricoesPorMes());
    }

    // Sempre os últimos 6 meses, inclusive os que não tiveram inscrição
    private List<MetricasDTO.InscricoesNoMes> inscricoesPorMes() {
        YearMonth esteMes = YearMonth.now();
        YearMonth primeiroMes = esteMes.minusMonths(MESES_NO_GRAFICO - 1L);
        LocalDateTime desde = primeiroMes.atDay(1).atStartOfDay();

        Map<YearMonth, Long> contagem = candidaturaRepository.datasDeInscricaoDesde(desde).stream()
                .collect(Collectors.groupingBy(YearMonth::from, Collectors.counting()));

        List<MetricasDTO.InscricoesNoMes> meses = new ArrayList<>();
        for (YearMonth mes = primeiroMes; !mes.isAfter(esteMes); mes = mes.plusMonths(1)) {
            meses.add(new MetricasDTO.InscricoesNoMes(mes.toString(), contagem.getOrDefault(mes, 0L)));
        }
        return meses;
    }
}
