-- Listar desafios publicados por um mentor
SELECT 
    d.id,
    d.titulo,
    d.nivel,
    d.status,
    d.data_publicacao,
    COUNT(e.id) AS total_inscritos,
    SUM(CASE WHEN e.status = 'enviado' THEN 1 ELSE 0 END) AS pendentes_avaliacao,
    SUM(CASE WHEN e.status = 'avaliado' THEN 1 ELSE 0 END) AS avaliados
FROM desafios d
LEFT JOIN entregas e ON d.id = e.desafio_id
WHERE d.mentor_id = 3
GROUP BY d.id
ORDER BY d.data_publicacao DESC;