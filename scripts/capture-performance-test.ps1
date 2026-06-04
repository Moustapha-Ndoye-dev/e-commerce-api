# Script interactif — captures d'écran pour le rapport de performance
# Usage : .\scripts\capture-performance-test.ps1

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

function Wait-Capture {
    param([string]$Message, [string]$Figure)
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host " CAPTURE : $Figure" -ForegroundColor Yellow
    Write-Host " $Message" -ForegroundColor White
    Write-Host "========================================" -ForegroundColor Cyan
    Read-Host "Appuyez sur ENTREE apres la capture d'ecran"
}

Write-Host ""
Write-Host "  TEST DE PERFORMANCE — MODE PRODUCTION" -ForegroundColor Green
Write-Host "  500 users navigation | 200 users commandes | 15 req/s soutenu" -ForegroundColor Green
Write-Host ""

# --- Etape 1 : Java ---
Wait-Capture -Figure "Figure 1 — java-version.png" -Message "Executez : java -version"
java -version

# --- Etape 2 : Compilation Gatling ---
Wait-Capture -Figure "Figure 2 — gatling-compile.png" -Message "Compilation Gatling en cours..."
.\gradlew.bat :performance-tests:compileGatlingJava

# --- Etape 3 : Demarrage API (nouvelle fenetre) ---
Wait-Capture -Figure "Figure 3 — spring-boot-run.png" -Message "L'API va demarrer dans une NOUVELLE fenetre PowerShell. Capturez quand vous voyez 'Started EcommerceApplication'"

$bootRunCmd = "Set-Location '$ProjectRoot'; .\gradlew.bat bootRun"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $bootRunCmd

Write-Host "Attente du demarrage de l'API (30 s)..." -ForegroundColor Gray
Start-Sleep -Seconds 30

# --- Etape 4 : Verification API ---
Wait-Capture -Figure "Figure 4 — api-response.png" -Message "Test de l'endpoint GET /api/products"
$response = Invoke-WebRequest -Uri "http://localhost:8081/api/products" -UseBasicParsing
Write-Host "Status : $($response.StatusCode)" -ForegroundColor Green
Write-Host $response.Content.Substring(0, [Math]::Min(500, $response.Content.Length))

# --- Etape 5 : Test Gatling ---
Wait-Capture -Figure "Figure 5 — gatling-run.png" -Message "Lancement du test de charge PRODUCTION (~2 min). Capturez la fin avec BUILD SUCCESSFUL"
.\gradlew.bat :performance-tests:gatlingRun

# --- Etape 6 : Resume terminal ---
Wait-Capture -Figure "Figure 6 — gatling-summary.png" -Message "Faites defiler le terminal vers le haut pour capturer le tableau 'Global Information'"

# --- Etape 7 : Rapport HTML ---
$reportDir = Get-ChildItem -Path "performance-tests\build\reports\gatling" -Directory |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($reportDir) {
    $reportPath = Join-Path $reportDir.FullName "index.html"
    Write-Host ""
    Write-Host "Rapport HTML : $reportPath" -ForegroundColor Green
    Start-Process $reportPath

    Wait-Capture -Figure "Figure 11 — gatling-report-global.png" -Message "Capturez la vue globale du rapport HTML (onglet ouvert dans le navigateur)"
    Wait-Capture -Figure "Figure 12 — gatling-report-percentiles.png" -Message "Capturez le graphique Response Time Percentiles"
    Wait-Capture -Figure "Figure 13 — gatling-report-details.png" -Message "Capturez le detail par requete HTTP"
}

Write-Host ""
Write-Host "Termine ! Copiez vos PNG dans docs\screenshots\" -ForegroundColor Green
Write-Host ""
