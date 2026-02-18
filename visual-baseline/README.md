# Visual Baseline Assets

Store baseline PNGs for iteration-02 pixel checks in this folder.

Suggested files:
- `home-screen-baseline-384x820.png`
- `top-tabs-baseline-384x220.png`
- `search-location-baseline-384x160.png`
- `plus-banner-baseline-384x80.png`
- `hero-baseline-384x220.png`
- `shortcuts-baseline-384x180.png`
- `incredible-baseline-384x300.png`
- `bottom-nav-baseline-384x90.png`

Then compare with:

```bash
/Users/amin/Projects/openkala/scripts/visual_diff.sh \
  /Users/amin/Projects/openkala/visual-baseline/home-screen-baseline-384x820.png \
  /path/to/home-screen-candidate.png \
  1.0
```
