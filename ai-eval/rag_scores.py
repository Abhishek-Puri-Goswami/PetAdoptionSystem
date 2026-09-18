#!/usr/bin/env python3
"""Measure RAG similarity scores so the retrieval threshold is chosen from
data, not guessed. Standard library only; needs the OPENAI_* env vars
(source .env first) and local Postgres (DB_USERNAME/DB_PASSWORD, psql).

    python ai-eval/rag_scores.py

For every question it prints the BEST cosine similarity against the
ingested chunks (same metric as Spring AI's similarityThreshold:
1 - cosine_distance) and which source file it came from. A good threshold
sits ABOVE the out-of-scope scores and BELOW the in-scope ones.
"""
import glob
import json
import os
import subprocess
import urllib.request

IN_SCOPE = [
    "What are the adoption fees for dogs?",
    "How much does it cost to adopt a cat?",
    "Are senior pets cheaper to adopt?",
    "What does the adoption fee cover?",
    "How many steps are in the adoption process?",
    "How long does the shelter review take?",
    "Can I see other adopters' applications?",
    "Can I pay for my adoption through the platform?",
    "Do you handle payment or delivery of the pet?",
    "Who arranges pickup after my application is approved?",
    "How much exercise does a high energy dog need?",
    "What should I feed my new puppy?",
    "How do I introduce a new cat to my home?",
]

OUT_OF_SCOPE = [
    "What is the capital of France?",
    "Who won the football world cup?",
    "How do I bake sourdough bread?",
    "What's the weather like tomorrow?",
    "What is the adoption fee for a rabbit?",
    "What are Luna's medical records?",
    "Write me a poem about the sea.",
    "How do I file my taxes?",
]


def embed(text):
    base = os.environ["OPENAI_EMBEDDING_BASE_URL"].rstrip("/")
    body = json.dumps({"model": os.environ["OPENAI_EMBEDDING_MODEL"],
                       "input": text}).encode()
    req = urllib.request.Request(base + "/embeddings", data=body)
    req.add_header("Content-Type", "application/json")
    req.add_header("Authorization", "Bearer " + os.environ["OPENAI_API_KEY"])
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.load(resp)["data"][0]["embedding"]


def psql_path():
    found = glob.glob("C:/Program Files/PostgreSQL/*/bin/psql.exe")
    return found[0] if found else "psql"


def best_match(vector, top=1):
    literal = "[" + ",".join(f"{x:.7f}" for x in vector) + "]"
    sql = ("select round((1 - (embedding <=> '%s'::vector))::numeric, 3), "
           "metadata->>'source' from vector_store "
           "order by embedding <=> '%s'::vector limit %d;" % (literal, literal, top))
    env = dict(os.environ, PGPASSWORD=os.environ.get("DB_PASSWORD", "postgres"))
    out = subprocess.run(
        [psql_path(), "-h", "localhost", "-U",
         os.environ.get("DB_USERNAME", "postgres"), "-d", "pet_adoption_db",
         "-At", "-F", "|", "-c", sql],
        capture_output=True, text=True, env=env).stdout.strip().splitlines()
    return [(float(l.split("|")[0]), l.split("|")[1]) for l in out]


def run(label, questions):
    print(f"\n--- {label} ---")
    scores = []
    for q in questions:
        score, source = best_match(embed(q))[0]
        scores.append(score)
        print(f"{score:.3f}  {source:22} {q}")
    return scores


if __name__ == "__main__":
    inn = run("IN SCOPE (must be retrieved)", IN_SCOPE)
    out = run("OUT OF SCOPE (must NOT be retrieved)", OUT_OF_SCOPE)
    print(f"\nin-scope    min={min(inn):.3f} max={max(inn):.3f}")
    print(f"out-of-scope min={min(out):.3f} max={max(out):.3f}")
    gap = min(inn) - max(out)
    print("clean separation available" if gap > 0
          else "OVERLAP: no single threshold separates them")
    if gap > 0:
        print(f"a threshold anywhere in ({max(out):.3f}, {min(inn):.3f}) works")
