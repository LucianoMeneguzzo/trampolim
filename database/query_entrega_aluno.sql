-- Listar entregas de um aluno específico
SELECT 
    e.id AS entrega_id,
    d.titulo AS desafio_titulo,
    d.id AS desafio_id,
    e.status,
    e.repo_url,
    e.linguagem_principal,
    e.data_inscricao,
    e.data_envio,
    a.nota,
    a.feedback,
    u.nome AS mentor_nome
FROM entregas e
JOIN desafios d ON e.desafio_id = d.id
LEFT JOIN avaliacoes a ON e.id = a.entrega_id
LEFT JOIN usuarios u ON a.mentor_id = u.id
WHERE e.aluno_id = 1
ORDER BY e.data_inscricao DESC;