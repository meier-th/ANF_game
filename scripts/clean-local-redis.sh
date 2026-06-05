#!/usr/bin/env bash
set -euo pipefail

HOST="${REDIS_HOST:-127.0.0.1}"
PORT="${REDIS_PORT:-6379}"
DB="${REDIS_DB:-0}"
PATTERN="${REDIS_PATTERN:-anf:*}"
DRY_RUN=false
FLUSH_DB=false
ASSUME_YES=false

usage() {
  cat <<'EOF'
Usage: ./scripts/clean-local-redis.sh [options]

Clean local Redis data used by this project.

Options:
  --host <host>       Redis host (default: 127.0.0.1)
  --port <port>       Redis port (default: 6379)
  --db <db>           Redis DB index (default: 0)
  --pattern <glob>    Key pattern to delete (default: anf:*)
  --flushdb           Flush the whole selected DB (dangerous)
  --dry-run           Only print matching keys, do not delete
  -y, --yes           Skip confirmation prompts
  -h, --help          Show this help

Examples:
  ./scripts/clean-local-redis.sh
  ./scripts/clean-local-redis.sh --pattern 'anf:runtime:*'
  ./scripts/clean-local-redis.sh --flushdb --db 0 -y
EOF
}

require_redis_cli() {
  if ! command -v redis-cli >/dev/null 2>&1; then
    echo "Error: redis-cli not found in PATH." >&2
    exit 1
  fi
}

redis_cmd() {
  redis-cli -h "${HOST}" -p "${PORT}" -n "${DB}" "$@"
}

confirm() {
  local prompt="$1"
  if [[ "${ASSUME_YES}" == "true" ]]; then
    return 0
  fi
  read -r -p "${prompt} [y/N]: " answer
  [[ "${answer}" =~ ^[Yy]$ ]]
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --host)
        HOST="$2"
        shift 2
        ;;
      --port)
        PORT="$2"
        shift 2
        ;;
      --db)
        DB="$2"
        shift 2
        ;;
      --pattern)
        PATTERN="$2"
        shift 2
        ;;
      --flushdb)
        FLUSH_DB=true
        shift
        ;;
      --dry-run)
        DRY_RUN=true
        shift
        ;;
      -y|--yes)
        ASSUME_YES=true
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        echo "Unknown option: $1" >&2
        usage
        exit 1
        ;;
    esac
  done
}

delete_by_pattern() {
  mapfile -t keys < <(redis_cmd --scan --pattern "${PATTERN}")
  local count="${#keys[@]}"

  if [[ "${count}" -eq 0 ]]; then
    echo "No keys found for pattern '${PATTERN}' in db ${DB}."
    return
  fi

  echo "Found ${count} key(s) matching '${PATTERN}' in db ${DB}:"
  printf '  %s\n' "${keys[@]}"

  if [[ "${DRY_RUN}" == "true" ]]; then
    echo "Dry run enabled, nothing deleted."
    return
  fi

  if ! confirm "Delete these ${count} key(s)?"; then
    echo "Aborted."
    return
  fi

  printf '%s\n' "${keys[@]}" | xargs -r redis_cmd del >/dev/null
  echo "Deleted ${count} key(s)."
}

flush_db() {
  echo "About to FLUSHDB on ${HOST}:${PORT} db ${DB}."
  if [[ "${DRY_RUN}" == "true" ]]; then
    echo "Dry run enabled, FLUSHDB skipped."
    return
  fi
  if ! confirm "This removes ALL keys from db ${DB}. Continue?"; then
    echo "Aborted."
    return
  fi
  redis_cmd flushdb >/dev/null
  echo "DB ${DB} flushed."
}

main() {
  parse_args "$@"
  require_redis_cli

  if [[ "${FLUSH_DB}" == "true" ]]; then
    flush_db
  else
    delete_by_pattern
  fi
}

main "$@"
