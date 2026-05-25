# Captures d'écran — Rapport académique

Ces fichiers PNG sont référencés dans **[rapport-test-performance.md](../rapport-test-performance.md)**.

## Instructions

1. Suivre les étapes du [guide technique](../performance-test-guide.md) ou du rapport académique.
2. Capturer avec **Win + Shift + S**.
3. Enregistrer ici avec le nom exact ci-dessous.

## Liste des figures (rapport prof)

| Fichier | Figure | Contenu à capturer |
|---------|--------|-------------------|
| `01-java-version.png` | Figure 1 | `java -version` → OpenJDK 17 |
| `03-gatling-compile.png` | Figure 2 | `compileGatlingJava` → BUILD SUCCESSFUL |
| `09-spring-boot-run.png` | Figure 3 | `bootRun` → Started EcommerceApplication |
| `10-api-response.png` | Figure 4 | Navigateur ou terminal : GET /api/products (5 produits) |
| `11-gatling-run.png` | Figure 5 | `gatlingRun` → BUILD SUCCESSFUL |
| `12-gatling-summary.png` | Figure 6 | Tableau Global Information dans le terminal |
| `05-spring-boot-build-gradle.png` | Figure 7 | `build.gradle.kts` racine dans l'IDE |
| `06-gatling-build-gradle.png` | Figure 8 | `performance-tests/build.gradle.kts` |
| `07-application-properties.png` | Figure 9 | `application.properties` (port 8080, H2) |
| `08-gatling-simulation.png` | Figure 10 | `EcommerceSimulation.java` |
| `13-gatling-report-global.png` | Figure 11 | Rapport HTML — stats globales |
| `14-gatling-report-percentiles.png` | Figure 12 | Rapport HTML — graphique percentiles |
| `15-gatling-report-details.png` | Figure 13 | Rapport HTML — détail 5 requêtes |

## Rapport HTML Gatling (source des figures 11–13)

Ouvrir :
```
performance-tests/build/reports/gatling/ecommercesimulation-20260524210717587/index.html
```

Ou le dossier le plus récent :
```powershell
$report = Get-ChildItem ..\..\performance-tests\build\reports\gatling\ -Directory | Sort-Object Name -Descending | Select-Object -First 1
start "$($report.FullName)\index.html"
```

## Fichier texte (optionnel)

`gatling-run-output.txt` — sortie terminal d'un run Gatling (peut servir de référence pour la Figure 6).
