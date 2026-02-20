# Top Tabs WebView Speedup: Preload + Reuse + Web Cache

## Summary
Implement a lightweight WebView warmup/cache system for **top tabs** so first open is faster and tab switches are near-instant.

Locked choices:
1. Preload depth: **1 non-digikala tab**.
2. Warmup timing: **after Home becomes idle**.
3. Reload policy: **keep existing page state** (no forced reload when returning to a tab).

This plan targets only top-tab WebViews (not banner/shortcut overlay).

## Important Interface / Architecture Changes
1. Add an internal top-tab WebView pool manager in home UI layer:
- File: `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`
- New helper class/object (file-local), e.g. `TopTabsWebViewPool`.
- Responsibilities:
  - create/configure WebViews
  - retain/reuse WebViews by tab `name`
  - expose `getOrCreate(tabName, url)`
  - track last-used ordering for eviction
  - destroy evicted/all instances safely

2. Update `TopTabWebViewContainer(...)` signature to support pooled WebView:
- Accept `tabKey` (`SuperAppTab.name`) and pool reference.
- Render pooled instance instead of always creating a new WebView.

3. Add per-tab loading state map:
- in `HomeScreen`, keyed by tab name.
- used to show/hide current loading indicator reliably across reused views.

No network/repository/domain model changes.

## Implementation Plan

### 1) Build reusable WebView configuration + pooling
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

Implementation details:
1. Centralize WebView setup in one function:
- `javaScriptEnabled = true`
- `domStorageEnabled = true`
- `loadsImagesAutomatically = true`
- `cacheMode = WebSettings.LOAD_DEFAULT`
- accept cookies via `CookieManager` (global + third-party for this WebView)

2. Pooled instance map:
- key: top-tab `name`
- value: `WebView` + metadata (`lastLoadedUrl`, `lastUsedAt`, `isPageReady`)

3. Retention policy (decision-locked for safety):
- keep at most **2** WebViews in memory:
  - preloaded tab
  - currently active/most recent tab
- evict least-recently-used beyond limit (`destroy()` safely)

4. Parent handling:
- before attaching pooled WebView to `AndroidView`, remove it from previous parent if needed.

### 2) Add warmup preload after Home idle
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

Implementation details:
1. Determine preload candidate:
- first tab in `data.superAppTabs` where `name != "digikala"` and `webUrl` is valid after normalization.

2. Warmup timing:
- in `LaunchedEffect(data.superAppTabs)`, delay ~`1200ms` after Home content is stable (idle-ish window), then preload candidate via pool.

3. Warmup action:
- create pooled WebView if absent
- call `loadUrl(normalizedUrl)` once
- do **not** display it yet

4. Guardrails:
- skip preload if already preloaded or if user has already entered web mode.

### 3) Reuse WebView for top-tab navigation
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

Implementation details:
1. In top-tab web mode:
- `TopTabWebViewContainer` requests pooled view by selected tab name.
- if current pooled WebView already has content, show immediately.
- if first use and URL differs/empty, load normalized URL.

2. Keep page state:
- no forced reload when returning to the same tab if pooled view already loaded.
- preserve internal history + scroll position.

3. Loading indicator behavior:
- show spinner only until first `onPageFinished` for that tab session state.
- if pooled view is already page-ready, skip spinner.

### 4) Lifecycle and cleanup
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

Implementation details:
1. On `HomeScreen` disposal:
- destroy all pooled WebViews.
2. On eviction:
- destroy removed WebView and clear metadata.
3. Avoid leaks:
- ensure no stale parent reference and no dangling client callbacks.

### 5) Keep existing overlay logic untouched
- Banner/shortcut overlay WebView remains separate (current behavior preserved).
- Bottom-tab visibility logic in nav host remains unchanged.

## Test Cases and Scenarios

### Unit-like logic checks (if extracted helper is testable)
1. Pool returns existing instance for same tab key.
2. Eviction happens when capacity > 2 (LRU removed).
3. URL normalization + preload candidate selection works with relative URLs.

### UI/Integration tests
1. Home loads on digikala tab; after idle delay, one non-digikala tab is preloaded.
2. First tap on preload target tab opens faster (no blank creation lag).
3. Switching away and back to same top tab keeps prior page state (no forced reload).
4. Visiting multiple top tabs still works with retention cap (eviction/reload only when necessary).
5. Spinner appears only for tabs not yet page-ready; not for already warm tabs.
6. Back behavior in top-tab web mode unchanged (history first, then return to digikala).

### Manual verification
1. Launch app, wait briefly on Home.
2. Tap first non-digikala top tab; verify faster first paint.
3. Navigate inside that tab, switch to digikala, then return; verify state is preserved.
4. Open additional top tabs; verify app remains stable and responsive.
5. Observe memory usage baseline with repeated tab switches (no unbounded growth).

## Assumptions and Defaults
1. WebView HTTP caching is delegated to Chromium default cache (`LOAD_DEFAULT`); no custom disk cache layer added.
2. Preload is limited to one tab to protect startup smoothness and memory.
3. A small pool cap (`2`) is used to balance speed and memory safety.
4. This optimization is scoped to top tabs only; banner/shortcut overlays are out of scope for this iteration.
