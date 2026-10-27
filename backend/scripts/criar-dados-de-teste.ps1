# Cria dados de teste para ver a tela /business/assessment/:id
#
# ATENCAO: use so com o backend no banco em memoria (H2). Se o backend estiver
# ligado ao Supabase, as contas e a inscricao de teste vao parar no banco do time.
#
# Uso (com o backend rodando em localhost:8080), na pasta backend/scripts:
#   powershell -ExecutionPolicy Bypass -File .\criar-dados-de-teste.ps1
#
# 1. Se ainda nao existe nenhum desafio, cria um mentor de teste e um desafio.
# 2. Cria um aluno novo, inscreve no desafio mais recente e envia um repositorio.
# 3. Mostra o link da tela de avaliacao.
# A avaliacao so abre para o mentor que publicou o desafio.

$api = 'http://localhost:8080'

function Post($rota, $corpo, $token) {
    $headers = @{}
    if ($token) { $headers.Authorization = "Bearer $token" }
    Invoke-RestMethod -Method Post -Uri "$api$rota" -Headers $headers -ContentType 'application/json' -Body ($corpo | ConvertTo-Json)
}

function Put($rota, $corpo, $token) {
    Invoke-RestMethod -Method Put -Uri "$api$rota" -Headers @{ Authorization = "Bearer $token" } -ContentType 'application/json' -Body ($corpo | ConvertTo-Json)
}

function Conta($nome, $email, $senha, $tipo) {
    try { Post '/api/auth/cadastro' @{ name = $nome; email = $email; password = $senha; type = $tipo } | Out-Null } catch { }
    (Post '/api/auth/login' @{ email = $email; password = $senha }).token
}

$desafios = @(Invoke-RestMethod -Uri "$api/api/desafios")

if ($desafios.Count -eq 0) {
    $mentorEmail = 'mentor.teste@trampolim.local'
    $mentorSenha = 'senha-teste-123'
    $tokenMentor = Conta 'Mentor Teste' $mentorEmail $mentorSenha 'MENTOR'
    $desafio = Post '/api/desafios' @{
        title        = 'API de pedidos para uma pequena loja'
        description  = 'A loja precisa sair da planilha e registrar pedidos por uma API REST.'
        level        = 'INTERMEDIARIO'
        technologies = @('Java', 'Spring Boot')
        deadlineDays = 7
        slots        = 20
    } $tokenMentor
    Write-Host "Mentor de teste criado: $mentorEmail / $mentorSenha"
} else {
    $desafio = $desafios[0]
}

$n = Get-Date -Format 'HHmmss'
$tokenAluno = Conta "Ana Martins $n" "aluna.$n@trampolim.local" 'senha-teste-123' 'STUDENT'
$inscricao = Post '/api/candidaturas' @{ desafioId = $desafio.id } $tokenAluno
Put "/api/candidaturas/$($inscricao.id)/envio" @{ linkRepositorio = 'github.com/ana-martins/api-pedidos' } $tokenAluno | Out-Null

Write-Host ""
Write-Host "Desafio: $($desafio.titulo) (publicado por $($desafio.nomeCriador))"
Write-Host "Entrega pronta. Entre como esse mentor e abra:"
Write-Host "  http://localhost:5173/business/assessment/$($inscricao.id)"
