#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 2 ] || [ "$#" -gt 3 ]; then
  echo "Usage: $0 <baseline.png> <candidate.png> [threshold_percent]"
  exit 1
fi

BASELINE="$1"
CANDIDATE="$2"
THRESHOLD="${3:-1.0}"

if ! command -v compare >/dev/null 2>&1; then
  echo "ImageMagick 'compare' command is required. Install imagemagick first."
  exit 1
fi

if [ ! -f "$BASELINE" ] || [ ! -f "$CANDIDATE" ]; then
  echo "Baseline or candidate image does not exist."
  exit 1
fi

TMP_DIFF="$(mktemp /tmp/openkala-diff-XXXXXX.png)"
set +e
RAW_OUTPUT=$(compare -metric AE "$BASELINE" "$CANDIDATE" "$TMP_DIFF" 2>&1)
COMPARE_EXIT=$?
set -e

if [ $COMPARE_EXIT -gt 1 ]; then
  echo "compare failed: $RAW_OUTPUT"
  rm -f "$TMP_DIFF"
  exit 1
fi

WIDTH=$(identify -format "%w" "$BASELINE")
HEIGHT=$(identify -format "%h" "$BASELINE")
TOTAL_PIXELS=$((WIDTH * HEIGHT))
DIFF_PIXELS=${RAW_OUTPUT:-0}
DIFF_PERCENT=$(awk -v d="$DIFF_PIXELS" -v t="$TOTAL_PIXELS" 'BEGIN{printf "%.4f", (d/t)*100}')

printf "Diff pixels: %s / %s\n" "$DIFF_PIXELS" "$TOTAL_PIXELS"
printf "Diff percent: %s%%\n" "$DIFF_PERCENT"
printf "Threshold: %s%%\n" "$THRESHOLD"
printf "Diff image: %s\n" "$TMP_DIFF"

AWK_EXIT=$(awk -v dp="$DIFF_PERCENT" -v th="$THRESHOLD" 'BEGIN{if (dp <= th) print 0; else print 1}')
if [ "$AWK_EXIT" -ne 0 ]; then
  echo "FAILED: visual diff is above threshold"
  exit 2
fi

echo "PASSED: visual diff is within threshold"
