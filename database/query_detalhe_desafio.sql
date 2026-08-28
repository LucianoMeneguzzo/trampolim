-- Buscar detalhes completos de um desafio específico
SELECT 
    d.id,
    d.titulo,
    d.contexto,
    d.entregaveis,
    d.nivel,
    d.prazo_dias,
    d.status,
    d.data_publicacao,
    u.nome AS mentor_nome,
    u.titulo_profissional AS mentor_titulo,
    u.github AS mentor_github,
    COUNT(e.id) AS total_inscritos,
    GROUP_CONCAT(t.nome SEPARATOR ', ') AS tecnologias
FROM desafios d
JOIN usuarios u ON d.mentor_id = u.id
LEFT JOIN desafio_tecnologias dt ON d.id = dt.desafio_id
LEFT JOIN tecnologias t ON dt.tecnologia_id = t.id
LEFT JOIN entregas e ON d.id = e.desafio_id
WHERE d.id = 1
GROUP BY d.id;