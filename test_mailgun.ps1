# Script de test Mailgun avec la nouvelle clé API
# Charge les variables du .env
$env:PATH = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot\bin;$env:PATH"

# Charger .env
$lines = Get-Content .env
foreach($ln in $lines) {
    $t = $ln.Trim()
    if($t -and -not $t.StartsWith('#') -and $t.Contains('=')) {
        $p = $t.Split('=', 2)
        [Environment]::SetEnvironmentVariable($p[0].Trim(), $p[1].Trim(), 'Process')
    }
}

$apiKey = [Environment]::GetEnvironmentVariable('MAILGUN_API_KEY', 'Process')
$domain = [Environment]::GetEnvironmentVariable('MAILGUN_DOMAIN', 'Process')

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Mailgun avec nouvelle clé API"
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Clé API: $apiKey"
Write-Host "Domaine: $domain"
Write-Host ""

# Test 1: Authentification basique
Write-Host "[1] Test authentification Mailgun..." -ForegroundColor Yellow
$auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("api:$apiKey"))

try {
    $response = Invoke-WebRequest -Uri "https://api.mailgun.net/v3/domains" -Headers @{Authorization="Basic $auth"} -UseBasicParsing -TimeoutSec 10
    Write-Host "[OK] Authentification reussie (Code: $($response.StatusCode))" -ForegroundColor Green
    Write-Host "Reponse: $($response.Content.Substring(0, 200))..."
} catch {
    Write-Host "[ERROR] Authentification echouee" -ForegroundColor Red
    Write-Host "Code HTTP: $($_.Exception.Response.StatusCode.Value__)" 
    Write-Host "Erreur: $_"
    exit 1
}

Write-Host ""
Write-Host "[2] Test d'envoi d'email..." -ForegroundColor Yellow

$emailData = @{
    from = "noreply@$domain"
    to = "qa.currency@test.com"
    subject = "Test Mailgun - Nouvelle clé API"
    text = "Ceci est un email de test avec la nouvelle clé API Mailgun configurée le $(Get-Date)"
}

try {
    $response = Invoke-WebRequest -Uri "https://api.mailgun.net/v3/$domain/messages" `
        -Method POST `
        -Headers @{Authorization="Basic $auth"} `
        -Body $emailData `
        -UseBasicParsing `
        -TimeoutSec 10
    
    Write-Host "[OK] Email envoye avec succes (Code: $($response.StatusCode))" -ForegroundColor Green
    $result = $response.Content | ConvertFrom-Json
    Write-Host "ID du message: $($result.id)"
    Write-Host "Message: $($result.message)"
} catch {
    Write-Host "[ERROR] Envoi d'email echoue" -ForegroundColor Red
    Write-Host "Code HTTP: $($_.Exception.Response.StatusCode.Value__)"
    Write-Host "Erreur: $_"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "[OK] Tous les tests Mailgun sont reussis !"
Write-Host "========================================" -ForegroundColor Green
