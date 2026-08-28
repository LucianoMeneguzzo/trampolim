-- Buscar detalhes de uma entrega para avaliação
SELECT 
    e.id AS entrega_id,
    e.repo_url,
    e.linguagem_principal,
    e.data_inscricao,
    e.data_envio,
    DATEDIFF(e.data_envio, e.data_inscricao) AS dias_entrega,
    u.nome AS aluno_nome,
    u.bio AS aluno_bio,
    u.github AS aluno_github,
    d.titulo AS desafio_titulo
FROM entregas e
JOIN usuarios u ON e.aluno_id = u.id
JOIN desafios d ON e.desafio_id = d.id
WHERE e.id = 4
  AND e.status = 'enviado';