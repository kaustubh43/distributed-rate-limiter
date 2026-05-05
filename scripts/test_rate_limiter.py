#!/usr/bin/env python3
"""Simple rate limiter smoke test against a running Spring Boot app."""

from __future__ import annotations

import argparse
import sys
import time
from dataclasses import dataclass

import requests


@dataclass
class ResultSummary:
    ok: int = 0
    rate_limited: int = 0
    other: int = 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Rate limiter test client")
    parser.add_argument("--url", default="http://localhost:8081/api/test", help="Target URL")
    parser.add_argument("--requests", type=int, default=25, help="Number of requests")
    parser.add_argument("--sleep-ms", type=int, default=0, help="Sleep between requests in ms")
    parser.add_argument("--timeout", type=float, default=2.0, help="Request timeout in seconds")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    summary = ResultSummary()

    for i in range(1, args.requests + 1):
        try:
            response = requests.get(args.url, timeout=args.timeout)
        except requests.RequestException as exc:
            print(f"[{i:02d}] ERROR request failed: {exc}")
            summary.other += 1
            continue

        status = response.status_code
        if status == 200:
            print(f"[{i:02d}] OK 200")
            summary.ok += 1
        elif status == 429:
            print(f"[{i:02d}] RATE_LIMITED 429")
            summary.rate_limited += 1
        else:
            print(f"[{i:02d}] UNEXPECTED {status}: {response.text[:120]}")
            summary.other += 1

        if args.sleep_ms > 0:
            time.sleep(args.sleep_ms / 1000.0)

    print(
        "\nSummary: "
        f"ok={summary.ok}, "
        f"rate_limited={summary.rate_limited}, "
        f"other={summary.other}"
    )

    return 0


if __name__ == "__main__":
    sys.exit(main())

