-- Métricas gerais da plataforma
SELECT 
    (SELECT COUNT(*) FROM desafios WHERE status IN ('aberto', 'ultimas_vagas', 'encerrado')) AS desafios_publicados,
    (SELECT COUNT(*) FROM entregas) AS inscricoes_realizadas,
    (SELECT COUNT(*) FROM entregas WHERE status = 'enviado') AS projetos_enviados,
    (SELECT COUNT(*) FROM entregas WHERE status = 'avaliado') AS avaliacoes_concluidas,
    ROUND(
        (SELECT COUNT(*) FROM entregas WHERE status = 'avaliado') * 100.0 / 
        NULLIF((SELECT COUNT(*) FROM entregas), 0), 
        1
    ) AS taxa_conclusao;

-- Inscrições por mês (últimos 6 meses)
SELECT 
    DATE_FORMAT(data_inscricao, '%Y-%m') AS mes,
    COUNT(*) AS total_inscricoes
FROM entregas
WHERE data_inscricao >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
GROUP BY DATE_FORMAT(data_inscricao, '%Y-%m')
ORDER BY mes;

-- Desafios mais populares (mais inscritos)
SELECT 
    d.titulo,
    d.nivel,
    COUNT(e.id) AS total_inscritos,
    u.nome AS mentor_nome
FROM desafios d
JOIN usuarios u ON d.mentor_id = u.id
LEFT JOIN entregas e ON d.id = e.desafio_id
GROUP BY d.id
ORDER BY total_inscritos DESC
LIMIT 5;