# Distributed Rate Limiter

This repository contains a Spring Boot rate limiter backed by Redis, plus a small Python script to exercise the `/api/test` endpoint.

## Python test script

Prerequisite: the Spring Boot app must be running locally on `http://localhost:8081`.

Install Python dependencies:

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Run the test:

```bash
python scripts/test_rate_limiter.py --requests 25 --sleep-ms 0
```

You should see a mix of `OK 200` and `RATE_LIMITED 429` when the limiter is working.

