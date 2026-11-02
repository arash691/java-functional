#!/usr/bin/env bash
# Usage: scripts/run-week.sh w04 [module] [TestClass]   (module defaults to core; TestClass e.g. Ex1TaxTest)
# Runs only the tests tagged with the given week, e.g. @Tag("w04").
set -u
cd "$(dirname "$0")/.."
WEEK="${1:?usage: scripts/run-week.sh wNN [module]}"
MODULE="${2:-core}"
ARGS=()
if [ -n "${3:-}" ]; then ARGS+=("-Dtest=$3"); fi
./mvnw -q -pl "$MODULE" -am test -Dgroups="$WEEK" -Dsurefire.failIfNoSpecifiedTests=false ${ARGS[@]+"${ARGS[@]}"}
