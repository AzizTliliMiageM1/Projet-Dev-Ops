#!/usr/bin/env node
/**
 * 🧪 TEST E2E - FLUX COMPLET OPEN BANKING + OPTIMISATION
 * 
 * Ce script teste l'ensemble de la chaîne :
 * 1. Authentification utilisateur
 * 2. Import relevé bancaire (CSV)
 * 3. Appels aux APIs distantes (devises + benchmark)
 * 4. Vérification des résultats enrichis (score, recommandations, conversions)
 * 5. Ajout des abonnements détectés
 * 
 * Utilisation:
 *   node test-full-flow-e2e.mjs
 */

class OpenBankingE2ETest {
  constructor() {
    this.baseUrl = 'http://localhost:4567';
    this.userEmail = `test_${Date.now()}@test.com`;
    this.userPassword = 'Test1234!';
    this.session = null;
    this.testResults = [];
  }

  // ==================== HELPERS ====================

  log(level, message, data = null) {
    const timestamp = new Date().toISOString();
    const prefix = `[${timestamp}] [${level}]`;
    console.log(prefix, message);
    if (data) console.log('       ', JSON.stringify(data, null, 2));
  }

  async delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  createTestCSV() {
    return `Date;Label;Amount
2026-01-01;NETFLIX.COM;15.99
2026-02-01;NETFLIX.COM;15.99
2026-03-01;NETFLIX.COM;15.99
2026-01-10;SPOTIFY.COM;9.99
2026-02-10;SPOTIFY.COM;9.99
2026-03-10;SPOTIFY.COM;9.99
2026-01-15;MICROSOFT 365;19.99
2026-02-15;MICROSOFT 365;19.99
2026-03-15;MICROSOFT 365;19.99
2026-01-05;AMAZON PRIME;14.99
2026-02-05;AMAZON PRIME;14.99
2026-03-05;AMAZON PRIME;14.99`;
  }

  recordResult(testName, passed, details = {}) {
    this.testResults.push({ testName, passed, details, timestamp: new Date() });
    const icon = passed ? '✅' : '❌';
    this.log(passed ? 'PASS' : 'FAIL', `${icon} ${testName}`, details);
  }

  // ==================== TESTS ====================

  async testRegister() {
    this.log('INFO', '📋 TEST 1: Enregistrement utilisateur');
    try {
      const url = new URL(`${this.baseUrl}/api/register`);
      url.searchParams.set('email', this.userEmail);
      url.searchParams.set('password', this.userPassword);
      url.searchParams.set('pseudo', 'test_user');
      
      const response = await fetch(url.toString(), {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      });

      const passed = response.ok || response.status === 400; // 400 si déjà enregistré
      this.recordResult('Registration', passed, { status: response.status, email: this.userEmail });
      return passed;
    } catch (error) {
      this.recordResult('Registration', false, { error: error.message });
      return false;
    }
  }

  async testConfirmEmail() {
    this.log('INFO', '📧 TEST 1.5: Confirmation email (mode dev)');
    try {
      const url = new URL(`${this.baseUrl}/api/confirm-dev`);
      url.searchParams.set('email', this.userEmail);
      
      const response = await fetch(url.toString(), {
        method: 'POST'
      });

      const passed = response.ok;
      this.recordResult('ConfirmEmail', passed, { status: response.status, email: this.userEmail });
      return passed;
    } catch (error) {
      this.recordResult('ConfirmEmail', false, { error: error.message });
      return false;
    }
  }

  async testLogin() {
    this.log('INFO', '🔐 TEST 2: Authentification utilisateur');
    try {
      const response = await fetch(`${this.baseUrl}/api/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: this.userEmail,
          password: this.userPassword
        })
      });

      if (!response.ok) {
        this.recordResult('Login', false, { status: response.status, body: await response.text() });
        return false;
      }

      this.session = response.headers.get('set-cookie');
      const success = this.session && this.session.length > 0;
      this.recordResult('Login', success, { email: this.userEmail, sessionLength: this.session?.length });
      return success;
    } catch (error) {
      this.recordResult('Login', false, { error: error.message });
      return false;
    }
  }

  async testBankImportEUR() {
    this.log('INFO', '📊 TEST 3: Import bancaire (EUR → EUR)');
    try {
      const csvContent = this.createTestCSV();
      const response = await fetch(
        `${this.baseUrl}/api/bank/import?sourceCurrency=EUR&targetCurrency=EUR`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'text/plain',
            'Cookie': this.session
          },
          body: csvContent
        }
      );

      if (!response.ok) {
        this.recordResult('BankImport_EUR', false, { status: response.status });
        return null;
      }

      const body = await response.json();
      const passed = body.success && body.subscriptionsDetected > 0;
      this.recordResult('BankImport_EUR', passed, {
        subscriptions: body.subscriptionsDetected,
        transactions: body.transactionsProcessed,
        detectedServices: body.detections?.map(d => d.service) || []
      });

      return body;
    } catch (error) {
      this.recordResult('BankImport_EUR', false, { error: error.message });
      return null;
    }
  }

  async testBankImportUSD() {
    this.log('INFO', '💱 TEST 4: Import bancaire avec conversion (USD → EUR)');
    try {
      const csvContent = this.createTestCSV();
      const response = await fetch(
        `${this.baseUrl}/api/bank/import?sourceCurrency=USD&targetCurrency=EUR`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'text/plain',
            'Cookie': this.session
          },
          body: csvContent
        }
      );

      if (!response.ok) {
        this.recordResult('BankImport_USD_to_EUR', false, { status: response.status });
        return null;
      }

      const body = await response.json();
      const passed = body.success && body.subscriptionsDetected > 0;

      // Vérifier que les conversions ont été effectuées
      const firstDetection = body.detections?.[0];
      const hasConversion = firstDetection?.amountEUR && firstDetection?.amountTargetCurrency;
      const hasExternalApis = !!firstDetection?.externalApis;

      this.recordResult('BankImport_USD_to_EUR', passed && hasConversion && hasExternalApis, {
        subscriptions: body.subscriptionsDetected,
        hasConversion,
        hasExternalApis,
        sourceAmount: firstDetection?.amount,
        amountEUR: firstDetection?.amountEUR,
        amountTarget: firstDetection?.amountTargetCurrency,
        externalApis: firstDetection?.externalApis
      });

      return body;
    } catch (error) {
      this.recordResult('BankImport_USD_to_EUR', false, { error: error.message });
      return null;
    }
  }

  async testEnrichedScoring(detections) {
    this.log('INFO', '🎯 TEST 5: Score d\'optimisation enrichi');
    try {
      if (!detections || detections.length === 0) {
        this.recordResult('EnrichedScoring', false, { reason: 'No detections' });
        return;
      }

      const firstDetection = detections[0];
      const hasScore = typeof firstDetection.optimizationScore === 'number';
      const hasBreakdown = !!firstDetection.scoreBreakdown;
      const hasMarketData = firstDetection.marketStatus && firstDetection.marketDeviationPercent !== undefined;
      const hasRecommendation = !!firstDetection.recommendation;

      const passed = hasScore && hasBreakdown && hasMarketData && hasRecommendation;
      this.recordResult('EnrichedScoring', passed, {
        score: firstDetection.optimizationScore,
        scoreBreakdown: firstDetection.scoreBreakdown,
        marketStatus: firstDetection.marketStatus,
        marketDeviation: firstDetection.marketDeviationPercent,
        recommendation: firstDetection.recommendation?.substring(0, 80)
      });
    } catch (error) {
      this.recordResult('EnrichedScoring', false, { error: error.message });
    }
  }

  async testExternalApisUsed(detections) {
    this.log('INFO', '🔗 TEST 6: Vérification appels API distantes');
    try {
      if (!detections || detections.length === 0) {
        this.recordResult('ExternalAPIs', false, { reason: 'No detections' });
        return;
      }

      const firstDetection = detections[0];
      const hasExternalApis = !!firstDetection.externalApis;
      const exchangeApiUsed = firstDetection.externalApis?.exchangeRateApi === true;
      const benchmarkApiUsed = firstDetection.externalApis?.benchmarkApi === true;

      const passed = hasExternalApis && exchangeApiUsed && benchmarkApiUsed;
      this.recordResult('ExternalAPIs', passed, {
        exchangeRateApi: exchangeApiUsed,
        benchmarkApi: benchmarkApiUsed,
        marketAvg: firstDetection.marketAveragePrice,
        marketMin: firstDetection.marketMinPrice,
        marketMax: firstDetection.marketMaxPrice
      });
    } catch (error) {
      this.recordResult('ExternalAPIs', false, { error: error.message });
    }
  }

  async testAddDetectedSubscription(detection) {
    this.log('INFO', '➕ TEST 7: Ajout d\'un abonnement détecté');
    try {
      const payload = {
        service: detection.service,
        category: detection.category,
        amount: detection.amount
      };

      const response = await fetch(`${this.baseUrl}/api/open-banking/add-detected`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Cookie': this.session
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        this.recordResult('AddDetection', false, { status: response.status });
        return false;
      }

      const body = await response.json();
      const passed = body.success;
      this.recordResult('AddDetection', passed, {
        service: detection.service,
        abonnementId: body.abonnementId,
        message: body.message
      });

      return passed;
    } catch (error) {
      this.recordResult('AddDetection', false, { error: error.message });
      return false;
    }
  }

  // ==================== EXÉCUTION ====================

  async run() {
    this.log('INFO', '🚀 === DÉMARRAGE TESTS E2E OPEN BANKING ===');
    console.log('');

    // Test 1-2: Auth
    const registerOk = await this.testRegister();
    await this.delay(500);
    const confirmOk = await this.testConfirmEmail();
    await this.delay(500);
    const loginOk = await this.testLogin();

    if (!loginOk) {
      this.log('ERROR', '❌ Login échoué, arrêt des tests');
      this.printSummary();
      process.exit(1);
    }

    // Test 3-4: Import
    await this.delay(500);
    const importEur = await this.testBankImportEUR();

    await this.delay(500);
    const importUsd = await this.testBankImportUSD();

    // Test 5-6: Enrichissement
    if (importUsd?.detections) {
      await this.testEnrichedScoring(importUsd.detections);
      await this.delay(200);
      await this.testExternalApisUsed(importUsd.detections);

      // Test 7: Ajout
      await this.delay(200);
      if (importUsd.detections.length > 0) {
        await this.testAddDetectedSubscription(importUsd.detections[0]);
      }
    }

    console.log('');
    this.printSummary();
  }

  printSummary() {
    this.log('INFO', '📈 === RÉSUMÉ DES TESTS ===');
    const totalTests = this.testResults.length;
    const passedTests = this.testResults.filter(r => r.passed).length;
    const failedTests = totalTests - passedTests;

    console.log(`\n  Total: ${totalTests}`);
    console.log(`  ✅ Réussis: ${passedTests}`);
    console.log(`  ❌ Échoués: ${failedTests}`);
    console.log(`  📊 Taux de succès: ${Math.round((passedTests / totalTests) * 100)}%\n`);

    if (failedTests > 0) {
      console.log('  Détails des échecs:');
      this.testResults
        .filter(r => !r.passed)
        .forEach(r => {
          console.log(`    - ${r.testName}: ${JSON.stringify(r.details)}`);
        });
    }

    this.log('INFO', failedTests === 0 ? '✅ TOUS LES TESTS RÉUSSIS!' : '⚠️ Certains tests ont échoué');
  }
}

// ==================== EXÉCUTION ====================

async function main() {
  const tester = new OpenBankingE2ETest();
  await tester.run();
  process.exit(0);
}

main().catch(error => {
  console.error('Fatal error:', error);
  process.exit(1);
});
