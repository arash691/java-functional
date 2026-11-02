#!/usr/bin/env bash
# Week 2 git incidents, played in throwaway repos (nothing touches your real work).
#
#   scripts/w02-git-incident.sh setup   # create both broken repos
#   scripts/w02-git-incident.sh check   # check whether you repaired them
#   scripts/w02-git-incident.sh reset   # delete the throwaway repos
#
# Repos live in $W02_DIR (default: /tmp/w02-git-incident):
#   wrong-branch/  INC-02-01  you committed on main instead of a feature branch
#   lost-commit/   INC-02-02  you ran 'git reset --hard' and a commit disappeared
set -u
DIR="${W02_DIR:-/tmp/w02-git-incident}"

git_in() { git -C "$1" -c user.name="Sara Dev" -c user.email="sara@example.com" "${@:2}"; }

commit_file() { # repo file content message
  printf '%s\n' "$3" > "$1/$2"
  git_in "$1" add "$2"
  git_in "$1" commit -q -m "$4"
}

setup() {
  rm -rf "$DIR"; mkdir -p "$DIR/wrong-branch" "$DIR/lost-commit"

  # INC-02-01: a commit that belongs on feature/discount was made on main
  R="$DIR/wrong-branch"
  git_in "$R" init -q -b main
  commit_file "$R" catalog.txt "mug 10.00" "Initial ShopFlow catalog"
  commit_file "$R" discount.txt "10 percent off" "Add 10% discount rule"

  # INC-02-02: a commit was thrown away with reset --hard
  R="$DIR/lost-commit"
  git_in "$R" init -q -b main
  commit_file "$R" catalog.txt "mug 10.00" "Initial ShopFlow catalog"
  commit_file "$R" inventory.txt "mug: 5" "Add inventory"
  commit_file "$R" reservation.txt "reserve on checkout" "Add stock reservation"
  git_in "$R" reset -q --hard HEAD~1

  cat <<MSG
Created two broken repos in $DIR

INC-02-01  $DIR/wrong-branch
  Ticket: "I committed 'Add 10% discount rule' on main but it should live on a branch called
  feature/discount, and main must go back to 'Initial ShopFlow catalog'."
  Goal: branch feature/discount contains the discount commit; main's last commit is the initial one.

INC-02-02  $DIR/lost-commit
  Ticket: "I ran git reset --hard HEAD~1 and my commit 'Add stock reservation' vanished.
  Please get it back on main."
  Goal: main's log shows 'Add stock reservation' again and reservation.txt exists.

Work inside the repos with plain git. Then run:  scripts/w02-git-incident.sh check
MSG
}

check() {
  local ok=0
  R="$DIR/wrong-branch"
  if [ ! -d "$R/.git" ]; then echo "Run setup first."; exit 1; fi
  if git -C "$R" rev-parse --verify -q feature/discount >/dev/null \
     && git -C "$R" log feature/discount --format=%s | grep -qx "Add 10% discount rule" \
     && [ "$(git -C "$R" log main -1 --format=%s)" = "Initial ShopFlow catalog" ] \
     && [ -z "$(git -C "$R" status --porcelain)" ]; then
    echo "PASS INC-02-01: discount commit is on feature/discount and main is back to the initial commit"
  else
    echo "FAIL INC-02-01: feature/discount must contain 'Add 10% discount rule'; main must end at 'Initial ShopFlow catalog'; tree must be clean"; ok=1
  fi

  R="$DIR/lost-commit"
  if [ "$(git -C "$R" branch --show-current)" = "main" ] \
     && git -C "$R" log main --format=%s | grep -qx "Add stock reservation" \
     && [ -f "$R/reservation.txt" ] \
     && [ -z "$(git -C "$R" status --porcelain)" ]; then
    echo "PASS INC-02-02: 'Add stock reservation' is back on main"
  else
    echo "FAIL INC-02-02: main must contain 'Add stock reservation', reservation.txt must exist, tree must be clean"; ok=1
  fi
  exit $ok
}

case "${1:-}" in
  setup) setup ;;
  check) check ;;
  reset) rm -rf "$DIR"; echo "removed $DIR" ;;
  *) sed -n '2,10p' "$0"; exit 1 ;;
esac
