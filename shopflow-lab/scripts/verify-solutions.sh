#!/usr/bin/env bash
# Author tool. Usage: scripts/verify-solutions.sh w04 [module]
# 1) starter must COMPILE and have FAILING tests for the week (red)
# 2) with solutions/<week>/ overlaid, all tests for the week must PASS (green)
set -u
cd "$(dirname "$0")/.."
WEEK="${1:?usage: scripts/verify-solutions.sh wNN [module]}"
MODULE="${2:-core}"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
rsync -a --exclude target --exclude solutions --exclude .git ./ "$TMP/"
cd "$TMP"

run() { ./mvnw -B -pl "$MODULE" -am test -Dgroups="$WEEK" -Dsurefire.failIfNoSpecifiedTests=false 2>&1; }

OUT="$(run)"; CODE=$?
if grep -q "COMPILATION ERROR" <<<"$OUT"; then echo "FAIL: starter does not compile"; echo "$OUT" | tail -30; exit 2; fi
if ! grep -q "Tests run:" <<<"$OUT"; then echo "FAIL: no tests ran for tag $WEEK"; echo "$OUT" | tail -30; exit 2; fi
if [ $CODE -eq 0 ]; then echo "FAIL: starter is already green for $WEEK (expected red)"; exit 3; fi
echo "OK red:   $(echo "$OUT" | grep 'Tests run:' | tail -1)"

SRC="$OLDPWD/solutions/$WEEK"
[ -d "$SRC" ] || { echo "FAIL: missing solutions/$WEEK"; exit 4; }
cp -R "$SRC"/. "$TMP"/
OUT="$(run)"; CODE=$?
if [ $CODE -ne 0 ]; then echo "FAIL: solution not green"; echo "$OUT" | tail -40; exit 5; fi
echo "OK green: $(echo "$OUT" | grep 'Tests run:' | tail -1)"
