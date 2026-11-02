#!/usr/bin/env bash
# Week 2: practise 'git bisect' on a generated repo.
#   scripts/w02-bisect-demo.sh setup   # creates /tmp/w02-bisect with 16 commits; one of them breaks ./test.sh
#   scripts/w02-bisect-demo.sh reveal  # prints the culprit commit (only look after you have bisected)
#   scripts/w02-bisect-demo.sh reset
set -u
DIR="${W02_BISECT_DIR:-/tmp/w02-bisect}"
g() { git -C "$DIR" -c user.name="Sara Dev" -c user.email="sara@example.com" "$@"; }
case "${1:-}" in
  setup)
    rm -rf "$DIR"; mkdir -p "$DIR"; g init -q -b main
    printf '#!/usr/bin/env bash\n# exits 0 when the shipping total is correct\n[ "$(cat total.txt)" = "42" ]\n' > "$DIR/test.sh"
    chmod +x "$DIR/test.sh"
    echo 42 > "$DIR/total.txt"; g add -A; g commit -q -m "commit 01: initial, total is 42"
    for i in $(seq 2 16); do
      n=$(printf '%02d' "$i")
      echo "change $i" > "$DIR/notes-$n.txt"
      if [ "$i" -eq 11 ]; then echo 41 > "$DIR/total.txt"; fi   # the bug sneaks in here
      g add -A; g commit -q -m "commit $n: work item $n"
    done
    echo "Created $DIR with 16 commits. HEAD is broken, commit 01 is good."
    echo "Try:  cd $DIR && git bisect start && git bisect bad && git bisect good \$(git rev-list --max-parents=0 HEAD) && git bisect run ./test.sh"
    ;;
  reveal) g log --format='%h %s' -S41 -- total.txt ;;
  reset) rm -rf "$DIR"; echo removed "$DIR" ;;
  *) sed -n '2,6p' "$0"; exit 1 ;;
esac
