# Top Tabs WebView Mode (Non-Digikala Tabs) Implementation Plan

## Summary
Implement top-tab switching so:
1. `digikala` tab shows the current native home content.
2. Every other top tab opens a full-screen WebView area using URL from API (`active_pillars[].web_url`).
3. While WebView mode is active, the rest of home UI and bottom nav are hidden.
4. Top tabs row stays visible at top for switching back/forth.
5. Android back: navigate WebView history first, then return to `digikala`.

Confirmed API source:
- `GET https://api.digikala.com/v1/super-app/pillars/`
- URL field: `data.active_pillars[].web_url`
- Example tabs currently include:
  - `units -> https://www.digikala.com/superapp/units/`
  - `jet -> https://api.digikala.com/super-app/v1/sso/jet/?redirect_url=...`
  - `gold -> https://www.digikala.com/wealth/my-assets/?utm_source=digikala-superweb`
  - `supermarket -> https://www.digikala.com/fresh/?utm_source=digikala-superweb`
  - `fintech -> https://api.digikala.com/super-app/v1/sso/fintech/?redirect_url=...`

## Important Public Interface/Type Changes
1. Update `SuperAppTab` model in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/domain/model/HomeModels.kt`
- Add `isWebView: Boolean`
- Add `webUrl: String`

2. Update mapper in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/domain/mapper/HomeMappers.kt`
- Parse `is_webview` and `web_url` from each pillar.
- Keep existing tab styling fields untouched.

No API service/repository signature changes are required.

## Implementation Design

### 1) Home screen layout refactor (top row always visible)
File: `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

- Refactor from single `LazyColumn` containing top tabs item to:
  - Outer `Column`:
    - `TopTabsRow` fixed at top.
    - Content area below:
      - If selected tab is `digikala`: render current native `LazyColumn` sections (search/hero/shortcuts/incredible/top_banners).
      - Else: render WebView container filling remaining height.
- Selected tab source:
  - Keep local state initialized from `data.selectedTabName`.
- Tab click behavior:
  - Always update selected tab.
  - `digikala` => native content mode.
  - non-`digikala` => WebView mode, load tab URL.

### 2) WebView container composable
File: `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt` (new composable in same file for now)

- Add `TopTabWebViewContainer(tab: SuperAppTab, onCanGoBackChanged, onLoadingChanged, ...)`.
- Use `AndroidView` + `WebView`.
- Web settings:
  - `javaScriptEnabled = true`
  - `domStorageEnabled = true`
  - `loadsImagesAutomatically = true`
  - `mixedContentMode` default unless required by failures
- Web client:
  - Keep navigation inside WebView (`shouldOverrideUrlLoading` returns false for http/https).
  - Track loading state for optional spinner.
- URL handling:
  - Use `tab.webUrl` directly.
  - If empty URL, show inline error placeholder and keep tabs interactive.
  - If relative URL appears, normalize with `https://www.digikala.com`.

### 3) Bottom nav visibility coordination
File: `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/navigation/OpenKalaNavHost.kt`

- Add state hoisted in nav host: `isHomeTopTabWebMode: Boolean`.
- Extend `HomeScreenRoute` API with callback:
  - `onWebModeChanged: (Boolean) -> Unit`
- Home reports `true` when selected tab != `digikala`, else `false`.
- Bottom bar visibility rule:
  - Current behavior: visible on `home`/`categories`
  - New behavior: visible on `home`/`categories` only when `!isHomeTopTabWebMode`.
- Ensure `isHomeTopTabWebMode` resets to `false` when leaving home route (via effect tied to route).

### 4) Android back behavior
File: `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

- In WebView mode only, install `BackHandler`.
- Behavior:
  - If WebView can go back: call `webView.goBack()`.
  - Else: select `digikala` tab and return to native home content.
- No nav-controller pop for this flow (home route remains active).

## Edge Cases and Failure Modes
1. `webUrl` empty/invalid:
- Show non-blocking unable-to-load placeholder in content area.
- Keep tab switching functional.
2. Slow pages:
- Show loading indicator over WebView until first `onPageFinished`.
3. WebView process recreation:
- Accept reload-on-return for now (no state bundle persistence in this iteration).
4. Tab refresh behavior:
- Re-selecting current non-digikala tab keeps current WebView state (no forced reload).
5. SSL or non-http schemes:
- Let system handle unsupported schemes via intent fallback only if needed; otherwise keep in WebView for http/https.

## Test Cases and Scenarios

### Unit tests
1. Mapper test in `/Users/amin/Projects/openkala/app/src/test/.../HomeMappersTest.kt`:
- Parses `is_webview` and `web_url`.
- Handles missing fields with defaults (`false`, empty string).

### UI tests (Compose)
1. In `/Users/amin/Projects/openkala/app/src/androidTest/.../home/...`:
- Selecting non-digikala tab hides native home list sections and shows WebView container tag.
- Selecting `digikala` restores native content.
- Top tabs remain visible in both modes.
- Bottom nav hidden while web mode active (validate via nav host test tags).

### Manual verification
1. Launch app on home.
2. Tap each non-digikala tab (`units`, `jet`, `gold`, `supermarket`, `fintech`) and confirm WebView loads.
3. Confirm bottom nav disappears in web mode.
4. Press Android back:
- goes back within WebView history first
- then returns to `digikala` native home.
5. Switch tabs repeatedly and confirm no crash/leak.

## Assumptions and Defaults
1. Scope is all non-`digikala` tabs, regardless of backend `is_webview` flag.
2. Top tabs remain the persistent header while WebView replaces the rest of screen.
3. Existing home API calls/caching remain unchanged; only tab metadata usage is expanded.
4. Initial implementation keeps WebView logic in home feature (no separate feature module yet).
