#!/bin/bash
PATH_HERE=/Users/SamP9/OneDrive/Documents/GitHub/train-ticket
OUTPUT_FILE="output_all_commits.json"

cd "$PATH_HERE"

git fetch origin

DEFAULT_BRANCH=$(git symbolic-ref refs/remotes/origin/HEAD | sed 's@^refs/remotes/origin/@@')

COMMIT_HASHES=$(git rev-list "origin/$DEFAULT_BRANCH")

> "$OUTPUT_FILE"

for COMMIT_HASH in $COMMIT_HASHES; do
    git checkout $COMMIT_HASH

    echo "### Commit $COMMIT_HASH" >> "$OUTPUT_FILE"
    
    curl --request POST \
        --url http://localhost:8080/ \
        --header 'content-type: application/json' \
        --data "{
        \"pathToMsRoots\": [\"${PATH_HERE}\"]
    }" >> "$OUTPUT_FILE"

done
