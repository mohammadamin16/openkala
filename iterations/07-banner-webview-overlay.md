# Home Banner Click WebView Overlay (Top Bar with Close + Share)

## Summary
Implement a full-screen WebView overlay for home banner clicks, matching your screenshot behavior:
1. Overlay opens above all home content and bottom tabs.
2. Only overlay top bar is visible (close on right, share on left, centered title).
3. Applies to all home banner groups.
4. Back behavior: WebView history first, then close overlay.
5. Share action: share current WebView URL.

## Scope (Locked)
Banner click opens overlay for all home banner groups:
- Hero/header carousel banners
- Top banners section
- Middle promo banners section

Only banners with a non-blank deeplink open overlay.

## Grounded Current State
- Banner models already carry `deeplink`:
  - `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/domain/model/HomeModels.kt`
- Home mapper already maps banner URLs from API:
  - `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/domain/mapper/HomeMappers.kt`
- Top/middle banner composables already expose `onBannerClick`, but currently default no-op:
  - `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`
- Hero carousel banners are not currently clickable.
- Existing WebView exists for top tabs; overlay implementation can reuse URL normalization ideas:
  - `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

## Important API/Interface Changes
1. Update `HomeScreenRoute` callback surface:
- Add `onBannerOpenStateChanged: (Boolean) -> Unit` to report overlay visibility upward.
- Keep existing `onWebModeChanged` behavior unchanged (top tabs web mode).

2. Internal home state changes:
- Add overlay state:
  - `isBannerOverlayVisible: Boolean`
  - `bannerOverlayInitialUrl: String`
  - `bannerOverlayCurrentUrl: String`
  - `bannerOverlayWebViewRef: WebView?`

No backend/network contract changes required.

## URL Handling Rules
Normalize banner deeplink to absolute URL before loading:
1. If starts with `http://` or `https://` -> use as-is.
2. If starts with `/` -> prefix with `https://www.digikala.com`.
3. Else -> prefix with `https://`.
4. If blank/invalid after normalization -> do not open overlay.

## UI/UX Implementation

### 1) Add Banner WebView overlay composable
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

Create `BannerWebViewOverlay(...)`:
- Fullscreen container over home content.
- Top bar:
  - Right: close icon button
  - Center: fixed title `فروشگاه اینترنتی دیجی‌کالا`
  - Left: share icon button
- Body: `AndroidView` WebView filling remaining space.

WebView behavior:
- `javaScriptEnabled = true`
- `domStorageEnabled = true`
- stay in-app for `http/https` links
- track current URL on page finished for share action

### 2) Wire banner clicks to overlay open
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

Changes:
- Hero carousel `AsyncImage` becomes clickable with `onBannerClick`.
- Pass non-noop `onBannerClick` into:
  - `TopBannersSection(...)`
  - `MiddlePromoBannersSection(...)`
- Click handler:
  - normalize deeplink
  - if valid: set overlay state visible + initial/current URL

### 3) Overlay layering and visibility
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

Layout strategy:
- Keep existing home content tree.
- Wrap content in a root `Box`.
- Render `BannerWebViewOverlay` as last child when visible so it sits above everything.

Bottom tabs behavior:
- While overlay visible, report `onBannerOpenStateChanged(true)` so bottom bar is hidden.
- On close, report `false`.

### 4) Navigation shell coordination
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/navigation/OpenKalaNavHost.kt`

Add home-level visibility state:
- `isHomeBannerOverlayVisible: Boolean`

Bottom bar show rule becomes:
- On `home`: show only when `!isHomeTopTabWebMode && !isHomeBannerOverlayVisible`
- On `categories`: unchanged true
- Else false

Reset home overlay flag when route leaves `home`.

### 5) Back handling (overlay)
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

Add `BackHandler(enabled = isBannerOverlayVisible)`:
1. If WebView can go back -> `goBack()`
2. Else -> close overlay

Priority:
- Banner overlay back handling should take precedence while visible.

### 6) Share action behavior
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

On share icon click:
- Use Android share sheet (`Intent.ACTION_SEND`)
- Share `bannerOverlayCurrentUrl` (fallback to initial URL if current empty)

## Testing Plan

### Unit-level checks
1. URL normalization cases:
- absolute URL preserved
- relative path becomes `https://www.digikala.com/...`
- malformed/blank returns empty and prevents open

### UI tests (androidTest)
Files:
- `/Users/amin/Projects/openkala/app/src/androidTest/java/com/openkala/app/ui/home/...`
- `/Users/amin/Projects/openkala/app/src/androidTest/java/com/openkala/app/ui/navigation/...`

Scenarios:
1. Clicking hero banner with deeplink shows overlay.
2. Clicking top/middle banners shows overlay.
3. Overlay top bar shows close + share + centered title.
4. Bottom bar hidden while overlay visible.
5. Back press closes overlay when no web history.
6. When web history exists, back navigates history first.
7. Closing overlay restores underlying home UI and bottom bar.

### Manual verification
1. Open home, tap each banner type.
2. Confirm fullscreen overlay appears with only top bar visible.
3. Navigate within web page, press back, confirm history back then close.
4. Tap share, confirm share sheet opens with current URL.
5. Confirm overlay stacks correctly even when top-tab web mode is not active.

## Assumptions and Defaults
1. Overlay feature applies only to home banners (not product/category links).
2. Title text in top bar is fixed as shown in screenshot.
3. External schemes are not handled in this iteration; only `http/https` web navigation is in-app.
4. Existing top-tab WebView mode remains unchanged and independent from banner overlay mode.
