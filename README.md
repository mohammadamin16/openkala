# OpenKala

![OpenKala Banner](./banner.png)

A native Android Digikala client focused on speed, smoothness, and cache-first UX.

This project was fully vibecoded using Codex GPT-5.3.

## Project Summary

- Native Android app (Jetpack Compose + Kotlin), not React Native.
- Cache-first data flow for faster startup and reduced loading waits.
- Pixel-focused RTL UI implementation based on Digikala screens.
- Shared-element transitions for key flows (home/search and item/detail experiences).
- API-driven home, search, category, and product detail sections using Digikala public endpoints.
- Performance-first approach: stable list keys, incremental refresh, and minimized recomposition hotspots.

## Current Scope

- Home screen with multiple dynamic sections (including incredible offers and banners).
- Product detail screen with variant/color handling and live countdowns.
- Search entry and search results flow.
- Category browsing flow.

## Tech Stack

- Kotlin
- Jetpack Compose
- Hilt (DI)
- Retrofit + Kotlinx Serialization
- DataStore (local cache)
- Coil (image loading)

## Run Locally

```bash
./gradlew :app:assembleDebug
```

## Notes

- UI direction is fixed to RTL by design.
- This is an independent client project and is not an official Digikala app.
- AI-generated code still requires human review, security checks, and real-device QA before production use.
