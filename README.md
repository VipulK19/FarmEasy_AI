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

---

## 3. Build and Test Instructions

The project is ready to open and run in Android Studio:
- **Build APK:** `./gradlew assembleDebug` (Output: `app/build/outputs/apk/debug/app-debug.apk`)
- **Run Unit Tests:** `./gradlew testDebugUnitTest` (All automated domain and calculation tests pass)
- **Supabase SQL Schema:** `supabase/migrations/20260101_init_farmeasy_schema.sql`
