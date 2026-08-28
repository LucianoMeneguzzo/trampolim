-- Listar desafios abertos com tecnologias e mentor
SELECT 
    d.id,
    d.titulo,
    d.nivel,
    d.prazo_dias,
    d.status,
    u.nome AS mentor_nome,
    u.titulo_profissional AS mentor_titulo,
    GROUP_CONCAT(t.nome SEPARATOR ', ') AS tecnologias
FROM desafios d
JOIN usuarios u ON d.mentor_id = u.id
LEFT JOIN desafio_tecnologias dt ON d.id = dt.desafio_id
LEFT JOIN tecnologias t ON dt.tecnologia_id = t.id
WHERE d.status IN ('aberto', 'ultimas_vagas')
GROUP BY d.id
ORDER BY d.data_publicacao DESC;

-- Com filtro por tecnologia (ex: React)
SELECT 
    d.id,
    d.titulo,
    d.nivel,
    d.prazo_dias,
    d.status,
    GROUP_CONCAT(t.nome SEPARATOR ', ') AS tecnologias
FROM desafios d
JOIN desafio_tecnologias dt ON d.id = dt.desafio_id
JOIN tecnologias t ON dt.tecnologia_id = t.id
WHERE d.status IN ('aberto', 'ultimas_vagas')
  AND t.nome = 'React'
GROUP BY d.id
HAVING tecnologias LIKE '%React%';

-- Com filtro por nível (ex: iniciante)
SELECT 
    d.id,
    d.titulo,
    d.nivel,
    d.prazo_dias,
    d.status,
    GROUP_CONCAT(t.nome SEPARATOR ', ') AS tecnologias
FROM desafios d
LEFT JOIN desafio_tecnologias dt ON d.id = dt.desafio_id
LEFT JOIN tecnologias t ON dt.tecnologia_id = t.id
WHERE d.status IN ('aberto', 'ultimas_vagas')
  AND d.nivel = 'iniciante'
GROUP BY d.id;