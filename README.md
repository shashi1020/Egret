<img width="3168" height="1344" alt="banner_v1" src="https://github.com/user-attachments/assets/ba5063a9-9e77-41b2-a5fd-0605cb149f71" />

# Egret: AI-Powered Precision Agriculture Platform 🌾🛰️

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![Flask](https://img.shields.io/badge/Flask-000000?style=for-the-badge&logo=flask&logoColor=white)
![Gemini AI](https://img.shields.io/badge/Google%20Gemini-8E75B2?style=for-the-badge&logo=googlebard&logoColor=white)

**Egret** is a next-generation agritech application designed to empower farmers with actionable, data-driven insights. By fusing data from **Satellite Imagery (Sentinel-2)**, **Computer Vision (Crop Disease Detection)**, and **Soil Analysis Reports**, Egret provides a holistic "Crop Doctor" experience that goes beyond simple monitoring to offer explainable, localized advisories.

---
---

## 🚀 Key Features

### 1. 🌍 Multi-Source Data Fusion Engine
Egret's core innovation is its ability to synthesize conflicting data points into a single, coherent risk assessment.
* **Satellite Intelligence:** Automatically fetches and processes Sentinel-2 L2A imagery to calculate **NDVI (Normalized Difference Vegetation Index)**.
* **Visual Diagnosis:** Uses on-device or cloud-based computer vision to identify crop diseases from leaf photos (e.g., *Stem Rust*, *Anthracnose*).
* **Soil Context:** Integrates soil health reports (N-P-K levels, pH) to determine if crop stress is biotic (disease) or abiotic (nutrient deficiency).
* **Smart Conflict Resolution:** Mathematically weighs conflicting signals (e.g., "Disease detected" vs. "High Soil Health") to prevent false alarms and provide a balanced Risk Score (0-100).

### 2. 🧠 Explainable AI Advisory (GenAI Integration)
Instead of showing raw charts, Egret "talks" to the farmer.
* **Natural Language Generation:** Uses **Google Gemini 1.5 Flash** to translate complex agronomic data into simple, human-readable advice in English or regional languages (e.g., Telugu).
* **Context-Aware:** The AI understands crop stages (Vegetative vs. Reproductive) and adjusts recommendations accordingly (e.g., "Avoid Nitrogen now to prevent disease spread").
* **Actionable Steps:** Provides specific checklists for immediate treatment, fertilizer management, and preventive care.

### 3. 🌦️ Hyper-Local Weather Intelligence
Precise weather data tailored to the farm's exact geocoordinates to optimize field operations.
* **Spray Windows:** Recommends ideal times for pesticide application based on wind speed, temperature, and rain probability to prevent washout.
* **Irrigation Planning:** Integrates upcoming rainfall forecasts with current soil moisture estimates to save water and prevent root rot.
* **Extreme Weather Alerts:** Provides early warnings for frost, hail, or unseasonal rains, allowing farmers to take protective measures for sensitive crop stages.

### 4. 📈 AI-Driven Market Insights
Empowering farmers with economic intelligence to maximize profit realization.
* **Live Mandi Prices:** Aggregates real-time price data from local markets (*Mandis*) for the specific crop being cultivated.
* **Trend Analysis:** Uses historical data and AI to identify price trends and volatility.
* **Smart Sales Advisory:** Suggests whether to "Hold" (store) or "Sell" produce based on current market dynamics and predicted price movements.

### 5. 📱 Modern Android Interface (Jetpack Compose)
A beautiful, highly responsive mobile experience built for clarity and speed.
* **Dynamic Dashboards:** Features color-coded "Status Strips" for instant health assessment (Healthy/Warning/Critical).
* **Visual Maps:** Renders colored NDVI maps directly in the app with an intuitive legend (Red=Barren, Green=Healthy).
* **Interactive Metrics:** Uses animated circular gauges and linear progress bars to visualize Stress Scores and Confidence levels.
* **History Tracking:** Allows farmers to scroll through a timeline of past scans to monitor recovery trends.

### 6. 🛠️ Robust Backend Infrastructure
A scalable Python backend designed for reliability.
* **Geo-Spatial Processing:** Handles GeoJSON farm boundaries and performs coordinate transformations (SRID 4326 to 3857) for accurate area calculations.
* **Resilient API Handling:** Implements retry logic and fallback windows (±1 day, ±3 days) for satellite data fetching to handle cloud cover or API downtime.
* **Colorization Pipeline:** Server-side processing converts raw scientific TIFF data (Float32) into visual PNG heatmaps before sending to the client.
* **Secure Authentication:** Integrated with Firebase Admin SDK for secure user management and token verification.

## 🏗️ Tech Stack

| Component | Technologies |
| :--- | :--- |
| **Mobile** | Android (Kotlin), Jetpack Compose, Retrofit, Hilt (Dependency Injection), Coroutines |
| **Backend** | Python (Flask), SQLAlchemy (PostgreSQL/PostGIS), Google Generative AI SDK, Sentinel Hub API |
| **Data Science** | NumPy, Pillow (PIL), Rasterio (Geospatial data), GeoAlchemy2 |
| **Database** | PostgreSQL with PostGIS extension for spatial queries |
