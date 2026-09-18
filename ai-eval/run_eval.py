#!/usr/bin/env python3
"""Repeatable live eval for the GenAI layer (Phase 6).

Runs ai-eval/cases.json against a RUNNING server and prints PASS / WARN /
FAIL per case. Exit code 1 if any hard FAIL. Standard library only.

    python ai-eval/run_eval.py                 # http://localhost:8080
    python ai-eval/run_eval.py --base-url http://host:8080 --only INJ

Restart the server before a run: chat memory is in-memory and the memory
(MEM-*) cases assume a clean start. Costs roughly 30-40 model calls.
"""
import argparse
import json
import pathlib
import sys
import urllib.error
import urllib.request

HERE = pathlib.Path(__file__).parent
PASSWORD = "Password@123"
USERS = {
    "A": {"email": "nandini@example.com"},
    "B": {"email": "eval.adopter.b@example.com",
          "first": "Eval", "last": "AdopterB"},
}


def call(base, path, body=None, token=None, timeout=180):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(base + path, data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, resp.read().decode("utf8", "replace")
    except urllib.error.HTTPError as err:
        return err.code, err.read().decode("utf8", "replace")


def login(base, email):
    status, text = call(base, "/api/auth/login",
                        {"email": email, "password": PASSWORD})
    if status != 200:
        return None
    return json.loads(text)["data"]["token"]


def get_tokens(base):
    tokens = {"A": login(base, USERS["A"]["email"]), "None": None}
    if tokens["A"] is None:
        sys.exit("Cannot log in as user A (nandini@example.com).")

    b = USERS["B"]
    tokens["B"] = login(base, b["email"])
    if tokens["B"] is None:
        call(base, "/api/auth/register",
             {"firstName": b["first"], "lastName": b["last"],
              "email": b["email"], "password": PASSWORD})
        tokens["B"] = login(base, b["email"])
    if tokens["B"] is None:
        sys.exit("Cannot create/log in throwaway user B.")
    return tokens


def reply_text(status, text):
    if status != 200:
        return text
    try:
        return json.loads(text)["data"]["reply"]
    except (ValueError, KeyError, TypeError):
        return text


def evaluate(case, status, reply, global_must_not):
    """Returns list of (kind, message); kind is 'FAIL' or 'WARN'."""
    problems = []
    low = reply.lower()
    expected = case.get("status", 200)

    if status != expected:
        problems.append(("FAIL", f"HTTP {status}, expected {expected}"))
        return problems

    if expected != 200:
        return problems

    for bad in global_must_not + case.get("must_not", []):
        if bad.lower() in low:
            problems.append(("FAIL", f"forbidden text present: {bad!r}"))

    kind = "WARN" if case.get("soft") else "FAIL"
    for group in case.get("must_all_groups", []):
        if not any(option.lower() in low for option in group):
            problems.append((kind, f"none of {group} found"))

    return problems


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost:8080")
    parser.add_argument("--only", help="run only case ids starting with this")
    args = parser.parse_args()

    spec = json.loads((HERE / "cases.json").read_text(encoding="utf8"))
    global_must_not = spec["global_must_not"]
    tokens = get_tokens(args.base_url)

    passed = warned = failed = 0
    by_suite = {}

    for case in spec["cases"]:
        if args.only and not case["id"].startswith(args.only):
            continue

        message = case["message"]
        if message == "PLACEHOLDER_LONG":
            message = "a" * 1001

        token = tokens[case["user"] or "None"]
        status, text = call(args.base_url, "/api/ai/" + case["endpoint"],
                            {"message": message}, token)
        reply = reply_text(status, text)

        problems = evaluate(case, status, reply, global_must_not)
        hard = [m for k, m in problems if k == "FAIL"]
        soft = [m for k, m in problems if k == "WARN"]

        if hard:
            verdict, failed = "FAIL", failed + 1
        elif soft:
            verdict, warned = "WARN", warned + 1
        else:
            verdict, passed = "PASS", passed + 1

        by_suite.setdefault(case["suite"], []).append(verdict)
        print(f"[{verdict}] {case['id']:8} {case['endpoint']:14} "
              f"{message[:60]!r}")
        for problem in hard + soft:
            print(f"         - {problem}")
        if hard or soft:
            print("         reply: " + reply.replace("\n", " ")[:300])

    print("\n=== Summary ===")
    for suite, verdicts in by_suite.items():
        print(f"{suite:13} pass={verdicts.count('PASS')} "
              f"warn={verdicts.count('WARN')} fail={verdicts.count('FAIL')}")
    print(f"TOTAL pass={passed} warn={warned} fail={failed}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
