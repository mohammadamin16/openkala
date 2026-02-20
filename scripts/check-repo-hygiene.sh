#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

fail() {
  echo "[FAIL] $1" >&2
  exit 1
}

# Nothing under .tmp should be tracked.
if git ls-files | rg -q '^\.tmp/'; then
  fail "Tracked files found under .tmp/. Remove them from git index."
fi

# Local/dev signing artifacts must never be tracked.
if git ls-files | rg -q '(local\.properties$|openkala-release\.jks$|\.jks$|\.keystore$|\.p12$|\.pem$|\.key$)'; then
  fail "Tracked key/cert/local config material found."
fi

echo "[OK] Repo hygiene checks passed."
