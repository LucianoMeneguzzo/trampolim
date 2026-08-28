-- ==========================================
-- USUÁRIOS (Alunos e Mentores)
-- ==========================================
INSERT INTO usuarios (nome, email, senha, tipo, bio, github, linkedin, titulo_profissional) VALUES
('Ana Martins', 'ana.martins@email.com', '$2b$10$hash123', 'aluno', 'Transformo requisitos confusos em produtos simples de usar.', 'ana-martins', 'ana-martins', 'Desenvolvedora Full Stack em formação'),
('Bruna Lopes', 'bruna.lopes@email.com', '$2b$10$hash456', 'aluno', 'Aprendendo com desafios reais.', 'bruna-lopes', 'bruna-lopes', 'Desenvolvedora Júnior'),
('Rafael Freitas', 'rafael.freitas@email.com', '$2b$10$hash789', 'mentor', 'Backend com 8 anos de mercado, focado em APIs e arquitetura.', 'rafael-freitas', 'rafael-freitas', 'Backend · 8 anos de mercado'),
('Marina Salles', 'marina.salles@email.com', '$2b$10$hash101', 'mentor', 'Frontend e UX, apaixonada por interfaces que fazem sentido.', 'marina-salles', 'marina-salles', 'Frontend · 6 anos de mercado'),
('Caio Mendes', 'caio.mendes@email.com', '$2b$10$hash102', 'mentor', 'Full Stack, gosto de projetos que resolvem problemas reais.', 'caio-mendes', 'caio-mendes', 'Full Stack · 5 anos de mercado');

-- ==========================================
-- TECNOLOGIAS
-- ==========================================
INSERT INTO tecnologias (nome) VALUES
('React'),
('Java'),
('Spring Boot'),
('Node.js'),
('SQL'),
('MySQL'),
('PostgreSQL'),
('API REST'),
('JUnit'),
('API externa');

-- ==========================================
-- DESAFIOS
-- ==========================================
INSERT INTO desafios (mentor_id, titulo, contexto, entregaveis, nivel, prazo_dias, status, data_publicacao) VALUES
(3, 'API de pedidos', 
'Uma pequena loja precisa sair da planilha e passar a registrar pedidos em um sistema confiável. Seu desafio é modelar os recursos principais, criar os endpoints e documentar as decisões que tomou.',
'✓ Endpoints para criar, listar e atualizar pedidos\n✓ Validação de dados e respostas de erro consistentes\n✓ Persistência com MySQL ou PostgreSQL\n✓ README com instruções para rodar o projeto',
'intermediario', 7, 'aberto', '2026-08-20 10:00:00'),

(4, 'Dashboard de pedidos',
'Construa uma visão operacional com filtros, estados e dados que fazem sentido para uma loja.',
'✓ Interface com filtros por status e data\n✓ Cards com métricas principais\n✓ Tabela de pedidos com paginação\n✓ Design responsivo',
'intermediario', 10, 'aberto', '2026-08-22 14:00:00'),

(3, 'Busca inteligente',
'Crie uma experiência de busca com filtros combináveis e uma interface objetiva.',
'✓ Campo de busca com autocomplete\n✓ Filtros por categoria e preço\n✓ Resultados paginados\n✓ Estados vazios bem tratados',
'avancado', 14, 'ultimas_vagas', '2026-08-15 09:00:00'),

(3, 'Agenda de consultas',
'Planeje um fluxo simples para marcar, cancelar e visualizar horários disponíveis.',
'✓ Calendário com horários disponíveis\n✓ Formulário de agendamento\n✓ Confirmação e cancelamento\n✓ Listagem de consultas do usuário',
'intermediario', 8, 'aberto', '2026-08-25 11:00:00'),

(5, 'Catálogo de filmes',
'Uma aplicação com consumo de API, favoritos e uma interface que prioriza conteúdo.',
'✓ Listagem de filmes da API\n✓ Sistema de favoritos\n✓ Página de detalhes do filme\n✓ Design clean e focado no conteúdo',
'iniciante', 5, 'aberto', '2026-08-26 16:00:00'),

(3, 'Relatório financeiro',
'Transforme dados mensais em um relatório legível e pronto para apoiar decisões.',
'✓ Gráficos de receitas e despesas\n✓ Filtros por período\n✓ Exportação para PDF\n✓ Comparativo mensal',
'intermediario', 12, 'aberto', '2026-08-18 13:00:00');

-- ==========================================
-- DESAFIO_TECNOLOGIAS (Relacionamento N:N)
-- ==========================================
-- API de pedidos (id=1)
INSERT INTO desafio_tecnologias (desafio_id, tecnologia_id) VALUES
(1, 2), (1, 3), (1, 6), (1, 9); -- Java, Spring Boot, MySQL, JUnit

-- Dashboard de pedidos (id=2)
INSERT INTO desafio_tecnologias (desafio_id, tecnologia_id) VALUES
(2, 1), (2, 8), (2, 5); -- React, API REST, UX

-- Busca inteligente (id=3)
INSERT INTO desafio_tecnologias (desafio_id, tecnologia_id) VALUES
(3, 1), (3, 4), (3, 5); -- React, Node.js, SQL

-- Agenda de consultas (id=4)
INSERT INTO desafio_tecnologias (desafio_id, tecnologia_id) VALUES
(4, 2), (4, 3); -- Java, Spring

-- Catálogo de filmes (id=5)
INSERT INTO desafio_tecnologias (desafio_id, tecnologia_id) VALUES
(5, 1), (5, 10); -- React, API externa

-- Relatório financeiro (id=6)
INSERT INTO desafio_tecnologias (desafio_id, tecnologia_id) VALUES
(6, 5), (6, 2); -- SQL, Java

-- ==========================================
-- ENTREGAS
-- ==========================================
INSERT INTO entregas (aluno_id, desafio_id, status, repo_url, linguagem_principal, data_inscricao, data_envio) VALUES
(1, 1, 'avaliado', 'https://github.com/ana-martins/api-pedidos', 'Java', '2026-08-21 10:00:00', '2026-08-27 14:32:00'),
(1, 2, 'avaliado', 'https://github.com/ana-martins/dashboard-pedidos', 'JavaScript', '2026-08-23 09:00:00', '2026-08-28 16:45:00'),
(1, 5, 'avaliado', 'https://github.com/ana-martins/catalogo-filmes', 'JavaScript', '2026-08-26 17:00:00', '2026-08-29 11:20:00'),
(2, 1, 'enviado', 'https://github.com/bruna-lopes/api-pedidos', 'Java', '2026-08-22 11:00:00', '2026-08-28 10:15:00'),
(1, 4, 'inscrito', NULL, NULL, '2026-08-26 14:00:00', NULL);

-- ==========================================
-- AVALIAÇÕES
-- ==========================================
INSERT INTO avaliacoes (entrega_id, mentor_id, nota, feedback, data_avaliacao) VALUES
(1, 3, 8.5, 'A estrutura da API está clara e as respostas de erro estão bem pensadas. Para evoluir, eu incluiria testes de integração nos fluxos de criação e atualização de pedidos.', '2026-08-28 09:00:00'),
(2, 4, 9.0, 'Interface bem organizada e fluxo de filtros consistente. O design está limpo e a experiência do usuário é intuitiva. Parabéns!', '2026-08-29 10:30:00'),
(3, 5, 8.0, 'Boa integração com API e atenção aos estados vazios. O código está bem estruturado. Como próximo passo, trabalhe na performance do carregamento das imagens.', '2026-08-30 14:00:00');