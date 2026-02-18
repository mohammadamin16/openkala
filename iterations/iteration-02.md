# Iteration 02 Plan: Pixel-Perfect Home Screen Styling (384x820dp, Strict)

## Summary
Bring the current home screen (`/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`) to strict pixel parity with your provided screenshot, using Digikala's real CSS/font system as source data and your screenshot as the final visual truth.

Locked decisions:
1. Scope: full shown screenshot (top strip to bottom nav).
2. Reference: your screenshot is the primary baseline.
3. Font: bundle IRANYekan locally.
4. Baseline viewport: `384x820dp`.
5. Tolerance: strict (`<=1dp` spacing variance, `<=1%` per-section pixel diff).

## Grounded Inputs From Digikala CSS
1. Font family token: `--font-family: IRANYekan,sans-serif`.
2. Radius tokens: `--small-radius:4px`, `--medium-radius:8px`, `--medium-plus-radius:12px`, `--large-radius:16px`.
3. Spacing base token: `--spacing-base:4px`.
4. Key color tokens used in UI family:
- `--color-app-background:#f2f2f2`
- `--color-neutral-000:#fff`
- `--color-neutral-100:#f0f0f1`
- `--color-neutral-200:#e0e0e2`
- `--color-neutral-500:#81858b`
- `--color-neutral-700:#3f4064`
- `--color-neutral-850:#212121`
- `--color-primary-500:#ef4056`
- `--color-brand-primary:#e6123d`
- `--color-plus-500:#b12ba4`
5. Typography utility scale available in CSS:
- `text-h1..h5`, `text-subtitle`, `text-body-1`, `text-body-2`, `text-caption`, `text-button-*`
- Weights include `100,300,400,500,600,700,800,900,950`.

## Important Changes to Public Interfaces / Types
1. Replace current ad-hoc theme with tokenized design system in:
`/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/theme/`
2. Add explicit token contracts:
- `OpenKalaColorTokens`
- `OpenKalaRadiusTokens`
- `OpenKalaSpacingTokens`
- `OpenKalaTypographyTokens`
3. Add `HomeStyleSpec` type for section-level geometry:
- top tabs card size/radius/padding
- search row dimensions/radius/icon sizes
- plus bar height/padding/button dimensions
- hero card radius/margins
- shortcut circle size/label style
- incredible header/timer geometry
- bottom nav icon/text metrics
4. Add `PixelPerfectMode` switch for deterministic styling (no animation drift, fixed timer display) in debug test path.

## Implementation Plan (Decision-Complete)
1. Font integration:
- Download IRANYekan weights (`100,300,400,500,700,800,900,950`) from Digikala public font URLs discovered in CSS.
- Add to `/Users/amin/Projects/openkala/app/src/main/res/font/`.
- Build Compose `FontFamily` and map weights exactly in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/theme/Type.kt`.
2. Theme tokenization:
- Rewrite `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/theme/Color.kt` to mirror Digikala tokens.
- Add spacing/radius token files (`4px` grid-driven).
- Ensure all hardcoded literals in `HomeScreen.kt` are replaced by tokens/spec constants.
3. Section-by-section geometry pass:
- Rebuild each block using strict dp/px mapping against screenshot at `384x820dp`.
- Align text baselines and icon containers; remove Material defaults that add implicit paddings.
- Keep data behavior from iteration-01 unchanged (only styling/layout changes).
4. Deterministic rendering for visual validation:
- Freeze dynamic content for tests:
  - fixed sample payload fixture
  - fixed countdown value
  - no shimmer/auto-carousel/refresh indicator
- Keep runtime/live mode unchanged for normal app usage.
5. Pixel diff harness:
- Add screenshot capture test path for `384x820dp` reference.
- Generate baseline PNGs for each major section + full screen.
- Add diff check script with threshold policy (`<=1%` per section).
6. Final polish loop:
- Run 3 rounds of tune->capture->diff.
- Accept only when all section diffs meet strict threshold.

## Test Cases and Scenarios
1. Typography fidelity:
- Verify each mapped text style (`body`, `subtitle`, `caption`, `button`) uses IRANYekan and correct weight.
2. Token correctness:
- Spot-check critical colors/radii against Digikala token values.
3. Visual regression:
- Full-screen baseline diff at `384x820dp`.
- Section diffs: top tabs, search+location, plus banner, hero, shortcuts, incredible strip, bottom nav.
4. RTL correctness:
- Text flow, icon placement, and spacing mirror screenshot.
5. Non-style regression:
- Existing data loading/cache flow still works.
- Only `دیجی‌کالا` tab remains active as iteration-01 behavior.

## Deliverables
1. Updated tokenized theme files in:
`/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/theme/`
2. Refactored pixel-perfect screen in:
`/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`
3. Bundled font assets in:
`/Users/amin/Projects/openkala/app/src/main/res/font/`
4. Visual baseline assets and diff tooling in:
`/Users/amin/Projects/openkala/app/src/androidTest/` and `/Users/amin/Projects/openkala/scripts/`
5. Iteration documentation update in:
`/Users/amin/Projects/openkala/iteration-02.md`

## Assumptions and Defaults
1. This iteration is styling-only; functional scope from iteration-01 remains.
2. Screenshot is authoritative even if current live website drifts.
3. Pixel-perfect is guaranteed for baseline viewport `384x820dp`; other sizes get responsive best-effort.
4. IRANYekan webfont usage is acceptable for this dev iteration.
