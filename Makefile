.PHONY: up flush-redis

up:
	docker compose up -d

flush-redis:
	./scripts/clean-local-redis.sh --flushdb --db 0 -y
