-- Buscar perfil completo de um aluno
SELECT 
    u.nome,
    u.bio,
    u.github,
    u.linkedin,
    u.titulo_profissional,
    COUNT(DISTINCT e.desafio_id) AS total_desafios,
    ROUND(AVG(a.nota), 1) AS media_notas,
    COUNT(DISTINCT t.id) AS total_tecnologias
FROM usuarios u
LEFT JOIN entregas e ON u.id = e.aluno_id AND e.status = 'avaliado'
LEFT JOIN avaliacoes a ON e.id = a.entrega_id
LEFT JOIN desafios d ON e.desafio_id = d.id
LEFT JOIN desafio_tecnologias dt ON d.id = dt.desafio_id
LEFT JOIN tecnologias t ON dt.tecnologia_id = t.id
WHERE u.id = 1
GROUP BY u.id;

-- Listar desafios avaliados do aluno (para o portfólio)
SELECT 
    d.titulo,
    d.id AS desafio_id,
    GROUP_CONCAT(t.nome SEPARATOR ', ') AS tecnologias,
    a.nota,
    a.feedback,
    u.nome AS mentor_nome
FROM entregas e
JOIN desafios d ON e.desafio_id = d.id
JOIN avaliacoes a ON e.id = a.entrega_id
JOIN usuarios u ON a.mentor_id = u.id
LEFT JOIN desafio_tecnologias dt ON d.id = dt.desafio_id
LEFT JOIN tecnologias t ON dt.tecnologia_id = t.id
WHERE e.aluno_id = 1
  AND e.status = 'avaliado'
GROUP BY d.id, a.id
ORDER BY a.data_avaliacao DESC;