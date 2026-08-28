-- ==========================================
-- 1. USUÁRIOS (Alunos e Mentores)
-- ==========================================
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL, -- Lembrar de salvar o hash da senha, nunca a senha pura!
    tipo ENUM('aluno', 'mentor') NOT NULL,
    
    -- Campos de Perfil (usados principalmente por alunos, mas mentores podem ter)
    bio TEXT,
    github VARCHAR(150),
    linkedin VARCHAR(150),
    titulo_profissional VARCHAR(100) -- Ex: "Backend · 8 anos de mercado"
);

-- ==========================================
-- 2. TECNOLOGIAS (Catálogo de tags)
-- ==========================================
CREATE TABLE tecnologias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE -- Ex: 'React', 'Java', 'Spring Boot'
);

-- ==========================================
-- 3. DESAFIOS
-- ==========================================
CREATE TABLE desafios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    mentor_id INT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    contexto TEXT NOT NULL,
    entregaveis TEXT NOT NULL,
    nivel ENUM('iniciante', 'intermediario', 'avancado') NOT NULL,
    prazo_dias INT NOT NULL,
    status ENUM('rascunho', 'aberto', 'ultimas_vagas', 'encerrado') DEFAULT 'rascunho',
    data_publicacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (mentor_id) REFERENCES usuarios(id)
);

-- Tabela auxiliar para relacionar Desafios com Múltiplas Tecnologias (N:N)
CREATE TABLE desafio_tecnologias (
    desafio_id INT NOT NULL,
    tecnologia_id INT NOT NULL,
    PRIMARY KEY (desafio_id, tecnologia_id),
    FOREIGN KEY (desafio_id) REFERENCES desafios(id) ON DELETE CASCADE,
    FOREIGN KEY (tecnologia_id) REFERENCES tecnologias(id) ON DELETE CASCADE
);

-- ==========================================
-- 4. ENTREGAS (A jornada do aluno no desafio)
-- ==========================================
CREATE TABLE entregas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    aluno_id INT NOT NULL,
    desafio_id INT NOT NULL,
    status ENUM('inscrito', 'enviado', 'avaliado') DEFAULT 'inscrito',
    
    -- Dados do Repositório (preenchidos na hora do envio)
    repo_url VARCHAR(255),
    linguagem_principal VARCHAR(50),
    
    -- Datas
    data_inscricao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_envio TIMESTAMP NULL,
    
    FOREIGN KEY (aluno_id) REFERENCES usuarios(id),
    FOREIGN KEY (desafio_id) REFERENCES desafios(id)
);

-- ==========================================
-- 5. AVALIAÇÕES (O feedback do mentor)
-- ==========================================
CREATE TABLE avaliacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    entrega_id INT NOT NULL UNIQUE, -- 1 entrega tem apenas 1 avaliação
    mentor_id INT NOT NULL,
    nota DECIMAL(3,1) NOT NULL, -- Permite notas como 8.5, 9.0
    feedback TEXT NOT NULL,
    data_avaliacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (entrega_id) REFERENCES entregas(id),
    FOREIGN KEY (mentor_id) REFERENCES usuarios(id)
);