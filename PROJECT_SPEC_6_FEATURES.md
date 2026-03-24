# 📋 PROJECT SPEC - GESTION D'ABONNEMENTS INTELLIGENTE

**Restructuration complète selon exigences du professeur**

6 VRAIES Features en 3 Livraisons (2 features chacune)

---

## 🎯 PROJET: "SMART BUDGET MANAGER"

### Concept Principal

Application permettant aux étudiants de **gérer intelligemment leurs abonnements** via:
- Importation automatique de relevés bancaires
- Détection et scoring des abonnements
- Recommandations d'optimisation par algorithmes avancés
- Planification financière à long terme

### Architecture

```
Frontend: HTML5 + JavaScript
Backend: Java 21 + Apache Spark (REST API)
Database: File-based + MySQL option
Infrastructure: Docker + CI/CD
```

### Code Base

```
~8,000 lignes Java
~5,000 lignes JavaScript
16+ services backend
45+ endpoints REST
74 tests unitaires
```

---

# 📦 LIVRAISON 1: Détection & Scoring Initial

*Durée: Semaines 1-4*

---

## 🟢 FEATURE 1: Détection Automatique d'Abonnements (Open Banking)

### Cas d'Usage Détaillé

```
Alice, étudiante à Paris-Nanterre, reçoit tous les mois
des relevés bancaires en CSV depuis son compte courant.

Elle se connecte à Smart Budget Manager et upload son relevé
(3 mois d'historique: janvier, février, mars).

Le système:
  1. Parse le CSV automatiquement
  2. Reconnaît les services (Netflix, Spotify, Microsoft, etc.)
  3. Détecte les patterns récurrents
  4. Enrichit avec données marché (prix moyen)
  5. Lui montre:
    "Netflix 15.99€ (3× récurrent, score 90.7/100)
     Spotify 10.99€ (3× récurrent, score 78.3/100)"

Alice gagne: Visibilité totale en 2 secondes
Système: Tourne autonome avec utilisateurs simulés
```

### Motivation

- **Réel:** Beaucoup d'étudiants oublient leurs abonnements
- **Urgent:** Pas de visibilité centralisée (données dispersées)
- **Créatif:** Combine parsing + reconnaissance + enrichissement

### Challenge Technique Principal

```
7-Phase Pipeline complexe avec interactions multiples:
  Phase 1: Parser CSV robuste
  Phase 2: Pattern recognition (50+ services)
  Phase 3: Clustering temporel (détection fréquence)
  Phase 4: Conversion multi-devises (API distante)
  Phase 5: Benchmarking marché (API distante)
  Phase 6: Scoring 6-critères (algorithme pondéré)
  Phase 7: Recommandations texte (NLP français)
```

### Classes Java Principales & Interactions

```
1. ParserService
   ├─ Input: String (CSV content)
   ├─ Output: List<Transaction>
   └─ Valide dates, montants, labels

2. RecognitionService (50+ patterns)
   ├─ Input: List<Transaction>
   ├─ Output: List<Transaction + recognizedService>
   ├─ Classes: ServicePattern, Matcher
   └─ Confidence scoring per match

3. SubscriptionDetectionEngine
   ├─ Input: Recognized transactions
   ├─ Output: List<DetectedSubscription>
   ├─ Algorithm: Temporal clustering
   └─ Classes: Cluster, FrequencyAnalyzer, ConfidenceCalculator

4. CurrencyConverterService (ExchangeRate-API)
   ├─ Input: DetectedSubscription + sourceCurrency
   ├─ Output: Enriched with amountConverted
   ├─ API fallback: Fixer.io → Hardcoded rates
   └─ Classes: CurrencyRateClient, ExchangeRateCache

5. BenchmarkingService (DummyJSON + fallback DB)
   ├─ Input: Service name
   ├─ Output: MarketAveragePrice, Deviation%
   ├─ Classes: MarketComparator, FallbackDatabase
   └─ Status: INFÉRIEUR/PROCHE/SUPÉRIEUR

6. ScoringEngine (6-criteria weighted)
   ├─ Criteria: Montant (15%), Récurrence (20%), Confiance (12%)
   │            Catégorie (15%), Marché (25%), Devise (13%)
   ├─ Output: Score 0-100
   └─ Classes: ScoringCriteria, WeightedCalculator

7. RecommendationService
   ├─ Input: Score + MarketStatus
   ├─ Output: French text recommendation
   └─ Classes: TextGenerator (templates réponses)

ORCHESTRATOR: OpenBankingSubscriptionDetectionService
  ├─ Coordonne toutes 7 phases
  ├─ Gère erreurs et fallbacks
  └─ Retourne JSON enrichi
```

### Interactions Clés Entre Classes

```
ParserService.parse(csvContent)
  → List<Transaction>

RecognitionService.recognize(transactions)
  → List<Transaction with service + confidence>

SubscriptionDetectionEngine.detect(recognizedTx)
  → List<DetectedSubscription with frequency>

CurrencyConverterService.convert(subscription, "USD", "EUR")
  → DetectedSubscription with amountEUR + rate

BenchmarkingService.benchmark(subscription.service)
  → MarketData (avg, min, max)

ScoringEngine.score(subscription, marketData)
  → Score (0-100) + breakdown {montant: 15, récurrence: 20, ...}

RecommendationService.recommend(score, marketStatus)
  → "Netflix excellent, coût stable..."

FINAL OUTPUT:
{
  service: "Netflix",
  amountEUR: 14.75,
  score: 90.7,
  scoreBreakdown: {...},
  marketAverage: 14.99,
  marketStatus: "PROCHE",
  recommendation: "...",
  externalApisUsed: {exchangeRateApi: true, benchmarkApi: true}
}
```

### Algorithme Détaillé: Scoring 6-Critères

```
Score = (w1 × s_montant) + (w2 × s_récurrence) + (w3 × s_confiance)
       + (w4 × s_catégorie) + (w5 × s_marché) + (w6 × s_devise)

Weights (total = 1.0):
  w1 = 0.15  (Montant: ampleur impact)
  w2 = 0.20  (Récurrence: certitude)
  w3 = 0.12  (Confiance: certitude recognition)
  w4 = 0.15  (Catégorie: valeur type service)
  w5 = 0.25  (Marché: ★ KEY CRITERION ★)
  w6 = 0.13  (Devise: coût conversion)

Calculs:
  s_montant = min(amount/30, 1.0) × 100
             (normalise à 30€ max)
             
  s_récurrence = (occurrences / 3) × 100
                (3+ = 100%, 2 = 67%, 1 = 33%)
                
  s_confiance = patternConfidence × 100
               (0.95 pattern = 95 points)
               
  s_catégorie = categoryScore
               (Streaming=100, Productivity=95, etc.)
               
  s_marché = 100 - ABS(marketDeviationPercent)
            (si -10%: 90pts, si +20%: 80pts)
            
  s_devise = (1 - conversionFee%) × 100
            (EUR direct = 100, USD avec fee = 98)

Exemple Real: Netflix 15.99€
  s_montant = min(15.99/30, 1) × 100 = 53.3
  s_récurrence = (3/3) × 100 = 100
  s_confiance = 0.95 × 100 = 95
  s_catégorie = 100 (Streaming)
  s_marché = 100 - ABS((15.99-14.99)/14.99×100) = 100 - 6.67 = 93.3
  s_devise = 100

  TOTAL = (0.15×53.3) + (0.20×100) + (0.12×95)
        + (0.15×100) + (0.25×93.3) + (0.13×100)
        = 8.0 + 20 + 11.4 + 15 + 23.3 + 13
        = 90.7 / 100 ✅ EXCELLENT
```

### Endpoint API

```
POST /api/bank/import?sourceCurrency=USD&targetCurrency=EUR
  
Input: CSV content (as body)
Output: JSON with all detected subscriptions enriched
```

### Avec Utilisateurs Simulés

```bash
# Générer 50 utilisateurs fictifs
foreach i in 1..50:
  userId = "synthetic_user_" + i
  generateRandomBankStatement(userId) → CSV
  call /api/bank/import
  store results

# Dashboard affiche statistiques globales:
  - 2,847 abonnements détectés
  - Score moyen: 76.5/100
  - Services top: Netflix (1000x), Spotify (850x)
  - Économies potentielles globales: 245,000€/an
```

---

## 🟢 FEATURE 2: Profil Personnel & Scoring de Préférences

### Cas d'Usage Détaillé

```
Marc importe ses abonnements. Le système crée automatiquement
son profil d'énergie financière:

  Dépenses mensuelles: 125€
  Catégories favorites: Streaming (40%), Productivité (35%)
  Budget personnel: 150€
  Rythme d'adoption: Nouveau service tous les 3 mois
  Tolérance au changement: Pas d'annulation pendant 3 ans

Le système utilise ce profil pour:
  - Personnaliser les recommandations futures
  - Détecter anomalies par rapport à ses habitudes
  - Proposer nouvelles options qui correspondent à son style
  - Alerter s'il s'écarte de son évolution typique
```

### Motivation

- **Réel:** Chaque étudiant a un profil d'épargne différent
- **Interactif:** Utilisateurs veulent du "perso"
- **Algorithmique:** Construction progessive du profil

### Challenge Technique Principal

```
Construire un modèle comportemental multi-dimensionnel:
  - Spending patterns (quantitatif)
  - Preferences (catégories, services)
  - Risk profile (stabilité vs innovation)
  - Trend detection (évolution dans le temps)

Interactions entre Profil → Scoring → Recommandations
```

### Classes Java Principales

```
1. UserProfileBuilder
   ├─ Input: List<DetectedSubscription>
   ├─ Output: UserProfile object
   └─ Classes: ProfileCalculator, PreferenceAnalyzer

2. SpendingPatternAnalyzer
   ├─ Calcule: Average, Variance, Trend
   ├─ Output: SpendingPattern object
   └─ Classes: StatisticalAnalyzer, TrendDetector

3. PreferenceCategoryBuilder
   ├─ Input: Services + Scores
   ├─ Output: CategoryPreferences (map%)
   └─ Classes: CategoryMapper, FrequencyCounter

4. RiskProfileCalculator
   ├─ Dimensions: Stability, Innovation, Commitment
   ├─ Output: RiskProfile (enum: CONSERVATIVE/BALANCED/ADVENTUROUS)
   └─ Classes: RiskDimensionCalculator

5. AdoptionPatternAnalyzer
   ├─ Input: History of subscriptions
   ├─ Output: TypicalAdoptionFrequency (months)
   └─ Classes: TimeSeriesAnalyzer

6. PersonalizedScorerService
   ├─ Input: Subscription + UserProfile
   ├─ Output: PersonalizedScore (different per user)
   ├─ Adjust base score by user preferences
   └─ Classes: ScoreAdjuster, PreferenceWeighter

ORCHESTRATOR: UserProfileOrchestrator
  ├─ Builds initial profile
  ├─ Updates on new imports
  ├─ Provides to recommendation engine
```

### Interactions Clés

```
SpendingPatternAnalyzer.analyze(subscriptions)
  → SpendingPattern {avg: 125€, variance: 15, trend: +2%/month}

PreferenceCategoryBuilder.build(subscriptions)
  → CategoryPreferences {Streaming: 40%, Productivity: 35%, ...}

RiskProfileCalculator.calculate(pattern, preferences, history)
  → RiskProfile.BALANCED

UserProfileBuilder.build(pattern, preferences, risk, adoption)
  → UserProfile object (persisted)

PersonalizedScorerService.score(subscription, userProfile)
  → PersonalizedScore (90.7 base → 88 adjusted for this user)
```

### Algorithme: Scoring Personnalisé

```
PersonalizedScore = BaseScore × CategoryPreferenceMultiplier
                  × RiskProfileBonus
                  × AdoptationAttitudeAdjust

Example:
  Netflix
    BaseScore = 90.7
    UserLikesStreaming = +1.1× (category 40% of budget)
    UserIsBalanced = no bonus (1.0×)
    NeverCancelsEarly = +1.05×
    
  PersonalizedScore = 90.7 × 1.1 × 1.0 × 1.05 = 105... CAPPED at 100 = 100/100
  
  vs.
  
  Microsoft365 (not in favorites)
    BaseScore = 72
    UserUninterestedInProductivity = ×0.9
    UserAdventurous = +1.1×
    
  PersonalizedScore = 72 × 0.9 × 1.1 = 71.3/100
```

---

# 📦 LIVRAISON 2: Optimisation & Alertes

*Durée: Semaines 5-8*

---

## 🟢 FEATURE 3: Prédiction de Dépenses Futures (Forecast 3-12 mois)

### Cas d'Usage Détaillé

```
Sophie veut savoir: "Combien je dépenserai les 3 prochains mois?"

Après import, le système analyse son historique et prédit:
  Avril: 125€ ±5€ (confiance 87%)
  Mai: 127€ ±6€ (confiance 85%)
  Juin: 129€ ±8€ (confiance 82%) [Netflix coming, +5€ anticipated]

Sophie peut:
  - Budgéter à l'avance
  - Détecter tendances (coûts augmentent, stagnent?)
  - Se préparer aux pics saisonniers
  - Identifier avant des annulations futures

Cas 12 mois:
  "Vous dépenserez ~1,500€ cette année.
   Tendance: +15€/mois en moyenne.
   Raison: 1 Netflix (15€) à prévoir en Q2"
```

### Motivation

- **Réel:** Budgétisation personnelle
- **Algorithmique:** Séries temporelles + régression + confiance
- **Contrôle:** Anticipation des futures dépenses

### Challenge Technique Principal

```
Gérer données bruitées, imprécises, avec patterns variés:
  - Subscriptions mensuels sont "noiseless" (exact)
  - Mais un utilisateur peut annuler à tout moment
  → Déterminer: Quoi garder? Quoi projeter?
  
Générer intervalles de confiance (pas juste moyenne)
Gérer annualisations (services annuels retournent au même moment)
```

### Classes Java Principales

```
1. TimeSeriesAnalyzer
   ├─ Input: List<Transaction> past 3+ months
   ├─ Output: TimeSeries object (normalized)
   └─ Classes: DataNormalizer, OutlierDetector

2. RegressionModelBuilder
   ├─ Algorithm: Simple linear, polynomial, or ARIMA
   ├─ Input: TimeSeries
   ├─ Output: RegressionModel {coefficients, r2, residuals}
   └─ Classes: LinearRegression, PolynomialRegression

3. SubscriptionRetentionPredictor
   ├─ For each subscription, predict: Keep? Cancel?
   ├─ Input: Subscription history, personal profile
   ├─ Output: RetentionProbability (0-1)
   └─ Classes: RetentionHeuristic, HistoricalComparison

4. ConfidenceCalculator
   ├─ Input: Regression model, residuals
   ├─ Output: Confidence interval (95%)
   └─ Classes: StatisticalCalculator, IntervalEstimator

5. ForecastAggregator
   ├─ Combine: Base forecast + retention + confidence
   ├─ Output: Forecast {value, lowerBound, upperBound, confid%}
   └─ Classes: Combiner, RangeCalculator

6. TrendAnalyzer
   ├─ Input: Forecast for 12 months
   ├─ Output: TrendReport {direction, avgChange, causes}
   └─ Classes: TrendDetector, CauseAnalyzer

ORCHESTRATOR: ForecastService
  ├─ 3-month and 12-month options
  ├─ Handles subscriptions of different frequencies
  ├─ Returns detailed report
```

### Interactions Clés

```
TimeSeriesAnalyzer.analyze(transactions)
  → TimeSeries {normalized values, no outliers}

RegressionModelBuilder.build(timeSeries)
  → RegressionModel {y = 0.5x + 120, r² = 0.92}

SubscriptionRetentionPredictor.predict(profile, history)
  → Map<SubscriptionId, RetentionProb>

ConfidenceCalculator.calculateCI(regressionModel, residuals)
  → ConfidenceInterval {lower: 118, upper: 132, confid: 87%}

ForecastAggregator.aggregate(regression, retention, ci)
  → Forecast {value: 125€, CI: [118, 132], confid: 87%}

TrendAnalyzer.analyze(12-monthForecast)
  → TrendReport {trend: +15€/month, cause: "Netflix incoming", ...}
```

### Algorithme: Timeseries Forecast

```
Input: Last 3-12 months of spending

Step 1: Normalize & detect seasonality
  Data: [80, 82, 81, 85, 90, 92]
  Trend: Rising (+2%)
  Seasonality: None (example)

Step 2: Simple Linear Regression (if <6 months of data)
  y = mx + b
  m = 0.5 (slope: +0.5€ per week)
  b = 120 (intercept)
  
Step 3: Predict next 3 months
  Month 1: 120 + 0.5×4 = 122€
  Month 2: 120 + 0.5×8 = 124€
  Month 3: 120 + 0.5×12 = 126€

Step 4: Confidence intervals (95% CI)
  σ² (variance of residuals)
  SE = sqrt(σ²/n)
  CI = ±1.96 × SE
  
  Example:
  Month 1 forecast: 122€, ±4€ → [118, 126] at 95%

Step 5: Handle subscriptions separately
  For each subscription:
    - If monthly: Include directly
    - If annual: Project next occurrence
    - If quarterly: Inter/extrapolate
    
  Combine all into total forecast

Output: {
  forecast: [{month: 1, value: 122, lower: 118, upper: 126, confid: 87%}, ...],
  trend: "Stable with slight increase",
  annualTotal: 1500€
}
```

---

## 🟢 FEATURE 4: Détection Anomalies & Alertes

### Cas d'Usage Détaillé

```
Jean utilise l'app depuis 2 mois. Le système détecte:

ANOMALIE 1 - Doublons suspectés:
  "Netflix charge 31.98€ (2× Netflix 15.99€)?
   Avez-vous 2 comptes? Action recommandée: Vérifier/Consolidation"

ANOMALIE 2 - Sous-utilisation:
  "Vous payez 15€ Adobe Creative depuis 3 mois.
   Vous n'avez jamais lancé l'app Photoshop.
   À 99% vous l'avez oublié → Annulation gagnée?"

ANOMALIE 3 - Mutation de prix:
  "Spotify: 9.99€ → 11.99€ (prix augmenté +20%)
   Au-delà de budget. Alert!"

Système = AUTONOME: Pas besoin Jean, scan annuels,
génère rapports synthétiques pour utilateurs simulés.
```

### Motivation

- **Réel:** Beaucoup d'abonnements oubliés/doublons
- **Complexité:** Heuristiques + règles + détection stats
- **Valeur:** Économies directes pour utilisateurs

### Challenge Technique Principal

```
Définir "règles d'anomalie" qui capturent:
  - Duplicates (même service 2× dans même mois)
  - Under-engagement (service payé, jamais utilisé)
  - Price spikes (mutations >15%)
  - Frequency changes (monthly? Soudain yearly?)
  - Budget exceedance (nouvelle souscription dépasse budget)

Éviter faux positifs tout en détectant vrais problèmes.
```

### Classes Java Principales

```
1. DuplicateDetector
   ├─ Input: List<DetectedSubscription> for month
   ├─ Algorithm: Group by service, compare amounts
   ├─ Output: List<Anomaly {type: DUPLICATE, confidence: 95%, action: MERGE}>
   └─ Classes: ServiceGrouper, ComparativeAnalyzer

2. UnderutilizationDetector
   ├─ Input: Subscription + usage logs (optional)
   ├─ Algorithm: If no usage in last 3 months → flag
   ├─ Output: Anomaly {type: UNUSED, prob: 75%, action: CANCEL_RECOMMENDATION}
   └─ Classes: UsageTracker, TimeBasedAnalyzer

3. PriceMutationDetector
   ├─ Input: Subscription history (same service over months)
   ├─ Algorithm: Compare month-to-month, flag if Δ > 15%
   ├─ Output: Anomaly {type: PRICE_SPIKE, delta: +20%, action: REVIEW}
   └─ Classes: HistoricalComparator, ThresholdChecker

4. FrequencyMutationDetector
   ├─ Input: Subscription (was monthly, now? Quarterly? Yearly?)
   ├─ Algorithm: Analyze intervals between occurrences
   ├─ Output: Anomaly {type: FREQUENCY_CHANGE, newFreq: QUARTERLY, ...}
   └─ Classes: IntervalAnalyzer, PatternRecognizer

5. BudgetExceedanceDetector
   ├─ Input: New subscription + UserProfile
   ├─ Algorithm: If totalMonthly > userBudget → flag
   ├─ Output: Anomaly {type: BUDGET_EXCEEDED, amount: +15€}
   └─ Classes: AggregationEngine, ThresholdChecker

6. AnomalyScorer & Prioritizer
   ├─ Input: List<Anomaly>
   ├─ Output: Scored, sorted by severity
   ├─ Action recommendations: IGNORE / REVIEW / URGENT_ACTION
   └─ Classes: SeverityCalculator, ActionRecommender

ORCHESTRATOR: AnomalyDetectionService
  ├─ Runs all detectors
  ├─ Combines findings
  ├─ Generates report
```

### Interactions Clés

```
// Check for duplicates within month
duplicateDetector.detectDuplicates(subscriptions)
  → List<Anomaly>

// Check for unused services (requires usage logs/optional)
underutilDetector.detectUnderutilization(subscription, usage)
  → Anomaly?

// Check price changes
priceMutationDetector.detectMutation(subscriptionHistory)
  → List<Anomaly>

// Aggregate & prioritize
anomalyScorer.score(allAnomalies)
  → SortedList<Anomaly> by severity

// Final report
anomalyReporter.generate(scoredAnomalies)
  → Report {critical: 2, warnings: 5, total_savings_potential: 85€}
```

### Algorithme: Anomaly Rules

```
Rule 1: DUPLICATE
  IF count(service with name X in month Y) > 1
  THEN Anomaly(DUPLICATE, confidence: 95%, action: CONSOLIDATE)
  
Rule 2: UNDERUTILIZATION
  IF (subscription age in months > 3) AND (usage_count == 0)
  THEN Anomaly(UNUSED, confidence: 75%, action: REVIEW)
  
Rule 3: PRICE SPIKE
  IF (current_price / previous_price - 1) > 0.15
  THEN Anomaly(PRICE_SPIKE, delta_percent: ..., action: REVIEW)
  
Rule 4: FREQUENCY CHANGE
  IF (interval_between_charges has increased by >20%)
  THEN Anomaly(FREQ_CHANGE, new_freq: ..., action: VERIFY)
  
Rule 5: BUDGET EXCEEDANCE
  IF (monthly_total > user_budget × 1.05)
  THEN Anomaly(OVER_BUDGET, excess: ..., action: URGENT)

Scoring:
  severity = (confidence % × rule_weight) / 100
  
  CRITICAL (score > 80): URGENT_ACTION required
  WARNING (score 50-80): REVIEW recommended
  INFO (score < 50): Monitor

Example Report:
  Total: 47 subscriptions reviewed
  Anomalies: 4
    └─ 2 Critical: Duplicates (Netflix 2x, +15.98€ monthly)
    └─ 1 Warning: Price spike (Spotify +2€)
    └─ 1 Info: Possible underutilization (Adobe unused)
  Potential Savings: 85€/month
```

---

# 📦 LIVRAISON 3: Long-Terme & Optimisation Globale

*Durée: Semaines 9-12*

---

## 🟢 FEATURE 5: Planification Long-Terme (Lifecycle 12+ mois)

### Cas d'Usage Détaillé

```
Célia est une étudiante de Master (année 1/2). Elle demande:
"J'ai Netflix, Notion, GitHub. Comment évoluera mon budget?"

Le système génère plan 24 mois:

PHASE 1 (Mois 1-6): Bachelor level
  Budget: 35€/mois
  Services: Netflix, Spotify, GitHub Free
  
PHASE 2 (Mois 7-12): Master starts
  Budget: 65€/mois
  New: GitHub Pro (+8€), Notion Personal (+4€)
  Reason: "Estudios avanzados requieren herramientas"
  
PHASE 3 (Mois 13-18): Master advanced
  Budget: 75€/mois
  New: Adobe Portfolio ($15€), JetBrains IDE (+20€)
  
PHASE 4 (Mois 19-24): Graduation
  Budget: 85€/mois (freelance starting)
  New: Adobe CC full (+35€), Grammarly ($12€)

Célia peut:
  - Préparer financièrement
  - Anticiper besoin nouveau

Système = AUTONOME: Génère lifecycles pour users simulés
basées sur profil (étudiant → junior dev → senior)
```

### Motivation

- **Longue vue:** Planification année complète
- **Contexte:** Lié au cycle de vie (étudiant → professionnel)
- **Prédictif:** Anticipe nouvelles souscriptions

### Challenge Technique Principal

```
Modéliser "phases de vie" (~6 mois chacune) avec:
  - Contexte (étudiant starter? Master? Freelance?)
  - Besoin typique par phase
  - Budget probable
  - Services associés
  
Générer projection crédible sans vraie donnée utilisateur.
```

### Classes Java Principales

```
1. UserLifecycleClassifier
   ├─ Input: UserProfile
   ├─ Output: LifecyclePhase {phase: "STARTER/INTERMEDIATE/ADVANCED", ...}
   └─ Classes: IntervalClassifier, StudentStatusDetector

2. PhaseCharacterizer
   ├─ For each lifecycle phase: Typical budget, services
   ├─ Input: Phase enum + industry (student/dev/designer)
   ├─ Output: PhaseCharacteristics {budget: 65€, typical_services: [...]}
   └─ Classes: ServicePredictor, BudgetEstimator

3. ServiceEvolutionPredictor
   ├─ Input: Current services + target phase
   ├─ Algorithm: Estimate which new services likely (GitHub Free → Pro)
   ├─ Output: List<UpgradeCandidate> {service, probability, benefit}
   └─ Classes: UpgradeAnalyzer, BenefitCalculator

4. BudgetProjector
   ├─ Input: Current + predicted services
   ├─ Output: Projected monthly spend per phase
   └─ Classes: AggregationEngine

5. LifecycleReporter
   ├─ Input: Phases + transitions
   ├─ Output: Visual report, explanations, warnings
   └─ Classes: ReportFormatter, TextGenerator

ORCHESTRATOR: LifecyclePlanningService
  ├─ Orchestrates 24-month projection
  ├─ Handles transitions between phases
  ├─ Generates advice
```

### Interactions Clés

```
userClassifier.classify(userProfile)
  → LifecyclePhase.INTERMEDIATE_STUDENT

phaseCharacterizer.characterizeForPhases(profile.field, 24months)
  → Map<Phase, PhaseCharacteristics>

serviceEvolutionPredictor.predictUpgrades(current, nextPhase)
  → List<UpgradeCandidate> {GitHub Free→Pro, ...}

budgetProjector.project(phases)
  → MonthlyProjection [35, 35, 35, ..., 65, 65, 65, ...]

lifecycleReporter.generate(phases, transitions)
  → Report with visual projection
```

### Algoritmo: Lifecycle Projection

```
Input: User profile, 24-month horizon

Step 1: Classify current phase
  Age + spending + services → Phase (STARTER/INTERMEDIATE/ADVANCED)

Step 2: Define phase transitions
  Each phase = 6 months
  Phase 1: Starter (current)
    → Phase 2: Intermediate
    → Phase 3: Advanced
    → Phase 4: Senior (if applicable)

Step 3: For each phase, predict:
  - Typical budget (starter: 30-50€, advanced: 70-100€)
  - New services likely (upgrades, new tools)
  - Retirement of unused services
  
Step 4: Build month-by-month projection
  [35, 35, 35, 35, 35, 35, | 65, 65, 65, 65, 65, 65, | ...]
  
Step 5: Generate transitions
  "Month 6→7: Entering Master. GitHub Free → Pro (+8€)"
  "Month 12→13: Need design tools. Adobe Starting (+15€)"

Output: {
  phases: [
    {phase: 1, name: "Starter", duration: 6mo, budget: 35€/mo, services: [Netflix, Spotify]},
    {phase: 2, name: "Intermediate", duration: 6mo, budget: 65€/mo, 
     new_services: [GitHub Pro], reason: "Master coursework design"},
    ...
  ],
  total_24mo: 1500€,
  savings_potential: "Cancellation plan for unused 3+ months"
}
```

---

## 🟢 FEATURE 6: Rééquilibrage Portfolio & Optimisation Globale

### Cas d'Usage Détaillé

```
Thomas a 8 abonnements (135€/mois). Il demande:
"Comment je peux optimiser ça?"

Le système analyse:
  Currents:
    Netflix 15.99€ (40% of Streaming)
    Spotify 11.99€ (35% of Streaming)
    Disney+ 9.99€ (25% of Streaming)
    [3x] = 37.97€ total streaming (28% budget)
    
    Microsoft 15€ (60% Productivity)
    Adobe 60€ (40% Productivity)
    [2x] = 75€ total productivity (55% budget)
    
    Misc: 22€ (16%)

  Recommendation:
    "Your portfolio is unbalanced:
     55% Productivity (vs 35% recommended) too high
     
     Action: Delay Adobe until Q2 (consolidate with college license)
     Savings: 60€ (44% reduction)
     New portfolio: 75€/mo, well-balanced"

Thomas = Gains confidence in decisions + validates approach
Autonomous = System scores all possible rebalancing for 50 fake users
```

### Motivation

- **Optimisation globale:** Pas juste detector une feature, refactor tout
- **Scoring multi-critères:** Balancer entre catégories, coûts, valeur
- **Recommandations concrètes:** "Annule ceci, upgrade ça"

### Challenge Technique Principal

```
Multi-objective optimization:
  - Minimize cost (total < user budget)
  - Maximize value (retain high-score services)
  - Balance portfolio (healthy mix of categories)
  - Respect constraints (min 2 services per category)
  
No single "best" answer → trade-offs → show alternatives
```

### Classes Java Principales

```
1. PortfolioAnalyzer
   ├─ Input: All user subscriptions
   ├─ Output: PortfolioComposition {categories %, avg_score, total_cost}
   └─ Classes: CategoryAggregator, StatisticsCalculator

2. PortfolioOptimizer (NP-hard-ish problem!)
   ├─ Input: Portfolio + budget constraint + preferences
   ├─ Algorithm: Greedy selection (keep high-score, remove low-score)
   ├─ Output: OptimizedPortfolio
   ├─ also generate alternatives (scenario 1, 2, 3)
   └─ Classes: SelectionAlgorithm, ScenarioGenerator

3. CategoryBalancer
   ├─ Input: OptimizedPortfolio
   ├─ Output: CategoryRebalancing {Streaming 40%, Productivity 30%, ...}
   ├─ Algorithm: Suggest category-level adjustments
   └─ Classes: BalanceCalculator, CategoryTransferer

4. RecommendationGenerator
   ├─ Input: Current → Optimized portfolio
   ├─ Output: Actions {CANCEL: [service1], UPGRADE: [service2], ...}
   ├─ Text: French recommendations with rationale
   └─ Classes: ActionPlanner, TextFormatter

5. SavingsCalculator
   ├─ Input: Current vs. Optimized
   ├─ Output: MonthlyS savings€ + % reduction + ROI time
   └─ Classes: MathCalculator

ORCHESTRATOR: PortfolioOptimizationService
  ├─ Runs full optimization
  ├─ Generates 2-3 alternatives
  ├─ Recommends best fit
```

### Interactions Clés

```
portfolioAnalyzer.analyze(subscriptions)
  → PortfolioComposition {categorieBreakdown: {...}}

portfolioOptimizer.optimize(portfolio, userBudget, preferences)
  → Optimized portfolio (scores removed/kept, total cost lower)

CategoryBalancer.balance(optimized)
  → Rebalancing suggestions (move from Streaming → Production)

recommendationGenerator.generate(current, optimized, balancing)
  → Actions {cancel_this: 60€ savings, upgrade_that: ...}

savingsCalculator.calculate(current, optimized)
  → Savings {monthly: 60€, annual: 720€, ROI_months: immediate}
```

### Algoritmo: Portfolio Optimization (Greedy)

```
Input:
  subscriptions: [Netflix 90.7, Spotify 78, Disney 65, ..., Adobe 45, ...]
  userBudget: 100€
  current total: 135€

Step 1: Sort by score (descending)
  Netflix 90.7 ✓
  Spotify 78 ✓
  Microsoft 75 ✓
  Disney 65 ✓
  GitHub 60 ✓
  Notion 55 (next if budget permits)
  Adobe 45
  ...

Step 2: Greedy select (keep high-score until budget exceeded)
  Netflix 15.99€ [15.99 total]
  Spotify 11.99€ [27.98 total]
  Microsoft 15€ [42.98 total]
  Disney 9.99€ [52.97 total]
  GitHub 8€ [60.97 total]
  Notion 4.99€ [65.96 total]
  Adobe 60€? NO - would hit 125.96€ > 100€ budget → SKIP
  ...

Step 3: Check categories balance
  Current: Streaming 40%, Productivity 55%, Misc 5% → UNBALANCED
  After: Streaming 25%, Productivity 22%, Misc 5% → BETTER but still light on prod
  
  Action: "Consider upgrading Microsoft → Professional version
          for better value in your high-priority category"

Step 4: Generate alternatives
  Scenario 1 (Conservative): Keep all current, gradually transition
  Scenario 2 (Aggressive): Cut adobe, gain 60€ (recommended)
  Scenario 3 (Balanced): Reduce to 6 services, 85€/mo

Step 5: Calculate savings
  Current: 135€
  Optimized (Scenario 2): 75€
  Savings: 60€/month = 720€/year
  
Output: {
  current_portfolio: 135€,
  optimized: 75€,
  savings: 60€ (44%),
  actions: [
    {type: CANCEL, service: Adobe, reasoning: "Low score 45/100, high cost"},
    {type: KEEP, service: Netflix, reasoning: "Top score 90.7, essential"}
  ],
  alternatives: [{...}, {...}]
}
```

---

## 📊 RÉSUMÉ: 6 VRAIES FEATURES

| # | Feature | Livraison | Cas d'Usage | Algo Principal | Classes |
|---|---------|-----------|------------|-----------------|---------|
| 1 | **Open Banking** | 1 | Import+Detect abos | 7-phase pipeline | Parser, Recognizer, Scorer |
| 2 | **User Profile** | 1 | Build personal model | Pattern analysis | ProfileBuilder, PreferenceAnalyzer |
| 3 | **Forecast** | 2 | Predict 3-12mo spend | Timeseries regression | TimeSeriesAnalyzer, RegressionModel |
| 4 | **Anomaly Detect** | 2 | Find duplicates/unused | Heuristic rules | DuplicateDetector, BudgetChecker |
| 5 | **Lifecycle Plan** | 3 | 24-month projection | Phase modeling | LifecycleClassifier, PhaseCharacterizer |
| 6 | **Portfolio Optim** | 3 | Rebalance & save | Greedy algorithm | PortfolioOptimizer, CategoryBalancer |

---

## 🎯 Infrastructure (NOT Features)

- ✅ CRUD layer (abonnements CRUD)
- ✅ Auth (login/register)
- ✅ CSV I/O (import/export)
- ✅ REST API (45+ endpoints)
- ✅ Database (File/H2/MySQL)
- ✅ Docker deployment
- ✅ CI/CD (GitHub Actions)

---

**END OF RESTRUCTURED PROJECT SPEC**

*Conforme aux exigences du professeur*  
*6 vraies features avec cas d'usage, algos, et classes détaillées*  
*Prêt pour présentation groupe + académique* ✅
