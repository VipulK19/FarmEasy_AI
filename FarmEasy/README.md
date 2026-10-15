# FarmEasy — AI-Based Smart Farmer Advisory Platform

**Academic Context:** Final-Year B.Tech Computer Science & Business Systems Mega Project  
**Platform:** Android (API 24 - 37)  
**Primary Language:** Kotlin (v2.2.10)  
**UI Framework:** Jetpack Compose + Material 3 (Design System)  
**Architecture:** Clean Architecture + MVVM + Offline-First Local Persistence  
**Backend:** Supabase PostgreSQL + Edge Functions (Optional Cloud Sync)  
**AI Advisory:** Groq-Hosted LLM (`llama-3.3-70b-versatile` / Edge Proxy)  
**Weather:** Open-Meteo REST API  

---

## 1. Project Overview

**FarmEasy** is a unified mobile agricultural decision-support platform designed to empower farmers with actionable agronomic intelligence. The platform bridges the gap between field challenges and scientific expertise by offering:

- **AI Agricultural Advisory:** Multilingual dialogue (English, Hindi, Marathi) powered by Groq-hosted LLMs with agronomic safety constraints.
- **Crop Disease Detection:** Visual leaf pathology diagnostics with pathogen identification, chemical dosage guidelines, biological treatments, and cultural practices.
- **Soil Health & NPK Assessment:** Algorithmic fertility scoring based on ICAR standards, generating tailored fertilizer split applications (Urea, DAP, MOP, Lime/Gypsum).
- **Custom Canvas Visualizations:** Native Jetpack Compose Canvas implementations for 7-day temperature/rain trend curves and circular NPK fertility gauges.
- **Government Schemes & Subsidies:** Database of verified government initiatives (PM-KISAN, PMFBY, SMAM, PMKSY) with eligibility rules, required document checklists, and official portal links.
- **Agricultural Loans & Credit:** Kisan Credit Card (KCC) interest subvention schemes and machinery financing.
- **Agri Dealer Locator:** Certified fertilizer, seed, and equipment dealer directory with Haversine distance calculations and OpenStreetMap geo-navigation.
- **Kisan Marketplace:** Agricultural inputs catalog with category filtering, cart management, subtotal calculation, and simulated demo order placement.
- **Weather & Spray Window Advisory:** Live Open-Meteo integration providing 7-day forecasts and agrochemical spray window evaluations (wind speed and precipitation checks).
- **Hands-Free Voice Assistant:** Interactive speech-to-text (STT) and text-to-speech (TTS) voice interaction in regional languages.

> **Decision-Support Notice:** FarmEasy serves as an agricultural decision-support tool. It does not replace certified agronomist evaluations or laboratory soil health tests. Chemical treatment recommendations emphasize protective gear and approved CIB&RC dosages.

---

## 2. Architecture & Design Principles

The application strictly follows **Clean Architecture** principles across Presentation, Domain, and Data layers:

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│  Jetpack Compose Material 3 Screens + MVVM ViewModels  │
│      Immutable StateFlow UI States + Sealed Events     │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                      Domain Layer                      │
│     Pure Kotlin Immutable Models (No Android/DB DTOs)  │
│   Repository Interfaces + Business Logic Use Cases     │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                       Data Layer                       │
│    Local SQLite Helper (DAOs) + PreferencesDataStore   │
│   Ktor HTTP REST Client (Open-Meteo, Supabase, Groq)   │
│    Repository Implementations + Sample Data Seeder     │
└────────────────────────────────────────────────────────┘
```

### Key Architectural Highlights:
1. **Decoupled Dependency Injection:** Built with an application-level `AppContainer` (`DefaultAppContainer`) providing singletons for all repositories, DAOs, services, and domain use cases.
2. **Offline-First Persistence:** Robust local SQLite persistence (`FarmEasyDatabaseHelper`) with DAOs (`FarmDao`, `CropDao`, `DiseaseDao`, `SoilDao`, `CartDao`, `ChatDao`, `NotificationDao`). The application functions offline and transparently caches data.
3. **Graceful Cloud Fallback & Demo Mode:** If Supabase credentials or Groq API keys are absent, the application launches in an explicitly labeled **Demo Mode** with realistic agricultural datasets.

---

## 3. Project Directory Structure

```
FarmEasy/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/farmeasy/
│   │   │   │   ├── FarmEasyApplication.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── di/
│   │   │   │   │   └── AppContainer.kt
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── Screen.kt
│   │   │   │   │   └── NavGraph.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Spacing.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   └── components/
│   │   │   │   │       ├── BarsAndNav.kt
│   │   │   │   │       ├── ButtonsAndCards.kt
│   │   │   │   │       ├── CanvasCharts.kt
│   │   │   │   │       └── FeedbackAndInputs.kt
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   └── DomainModels.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── RepositoryInterfaces.kt
│   │   │   │   │   └── usecase/
│   │   │   │   │       └── UseCases.kt
│   │   │   │   ├── core/
│   │   │   │   │   ├── common/
│   │   │   │   │   │   ├── AudioSpeechManager.kt
│   │   │   │   │   │   └── NetworkMonitor.kt
│   │   │   │   │   └── ml/
│   │   │   │   │       ├── CropDiseaseClassifier.kt
│   │   │   │   │       └── SoilRecommendationEngine.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── FarmEasyDatabaseHelper.kt
│   │   │   │   │   │   ├── PreferencesDataStore.kt
│   │   │   │   │   │   └── dao/LocalDaos.kt
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── KtorClientProvider.kt
│   │   │   │   │   │   ├── groq/GroqService.kt
│   │   │   │   │   │   ├── supabase/SupabaseServices.kt
│   │   │   │   │   │   └── weather/OpenMeteoService.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── RepositoryImplementations.kt
│   │   │   │   │   └── sample/
│   │   │   │   │       └── SampleData.kt
│   │   │   │   └── feature/
│   │   │   │       ├── auth/
│   │   │   │       ├── dashboard/
│   │   │   │       ├── chatbot/
│   │   │   │       ├── disease/
│   │   │   │       ├── soil/
│   │   │   │       ├── recommendations/
│   │   │   │       ├── schemes/
│   │   │   │       ├── farm/
│   │   │   │       ├── dealer/
│   │   │   │       ├── marketplace/
│   │   │   │       ├── weather/
│   │   │   │       ├── voice/
│   │   │   │       ├── notifications/
│   │   │   │       └── profile/
│   │   │   ├── res/
│   │   │   │   └── values/
│   │   │   │       ├── strings.xml
│   │   │   │       ├── colors.xml
│   │   │   │       └── themes.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   │       └── java/com/example/farmeasy/
│   │           └── FarmEasyDomainTests.kt
│   └── build.gradle.kts
├── supabase/
│   └── migrations/
│       └── 20260101_init_farmeasy_schema.sql
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 4. Supabase & Cloud Backend Configuration

The application works out of the box in offline/demo mode. To connect a live Supabase backend:

1. Create a Supabase project at [supabase.com](https://supabase.com).
2. Open the **SQL Editor** in your Supabase dashboard and run the migration script:
   ```
   FarmEasy/supabase/migrations/20260101_init_farmeasy_schema.sql
   ```
   This creates tables for `farmers`, `farms`, `crops`, `disease_analyses`, `soil_assessments`, `orders`, and applies Row-Level Security (RLS) policies.
3. In the FarmEasy Android App, navigate to **Profile -> Configure Cloud API Services** and enter:
   - **Supabase URL:** `https://<YOUR-PROJECT-REF>.supabase.co`
   - **Supabase Anon Key:** `eyJhbGciOi...`
   - **Groq API Key (Optional):** `gsk_...`
4. Tap **Save Credentials**. The app will now sync farmer profiles and chat advisory requests with your cloud instance.

---

## 5. Build and Test Instructions

### Prerequisites
- JDK 17, 21, or 26
- Android SDK 37 (Build Tools 36.0.0+)
- Android Studio Ladybug / Meerkat or later

### Running Tests
Execute the unit test suite:
```bash
./gradlew testDebugUnitTest
```
All unit tests in `FarmEasyDomainTests.kt` validate domain rules, soil calculations, form validation, and cart pricing.

### Building Debug APK
Assemble the APK binary:
```bash
./gradlew assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 6. Verification and Acceptance Checklist

| Item | Requirement | Status |
|---|---|---|
| 1 | Gradle sync & clean compilation | Verified (BUILD SUCCESSFUL) |
| 2 | All 14 feature screens & Navigation Compose | Implemented & Linked |
| 3 | Material 3 Agricultural Theme (Light & Dark) | Implemented (`Theme.kt`, `Color.kt`) |
| 4 | Clean Architecture (UI, Domain, Data) | Strict separation maintained |
| 5 | Offline-First SQLite Local Persistence | Implemented with SQLite OpenHelper & DAOs |
| 6 | Ktor REST Client & Open-Meteo Integration | Implemented & verified |
| 7 | Custom Canvas Visualizations (NPK & Weather) | Implemented (`CanvasCharts.kt`) |
| 8 | STT Speech Recognition & TTS Readout | Implemented (`AudioSpeechManager.kt`) |
| 9 | Explicit Demo Mode Indicators | Transparently surfaced on unconfigured services |
| 10 | Automated Unit Tests | 100% Passing (`testDebugUnitTest`) |
| 11 | Supabase SQL Schema & RLS Policies | Included in `supabase/migrations/` |
