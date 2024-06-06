#!/bin/bash
PATH_HERE=C:/Users/SamP9/OneDrive/Documents/GitHub/train-ticket
OUTPUT_FOLDER=C:/Users/SamP9/OneDrive/Documents/GitHub/rad-source-sdg-dataset/sample_output
OUTPUT_FILE="output_commit_"

cd "$PATH_HERE" || exit

git fetch origin

DEFAULT_BRANCH=$(git symbolic-ref refs/remotes/origin/HEAD | sed 's@^refs/remotes/origin/@@')

COMMIT_HASHES=$(git rev-list --reverse "origin/$DEFAULT_BRANCH")

for COMMIT_HASH in $COMMIT_HASHES; do
    git checkout $COMMIT_HASH
    
    curl --request POST \
        --url http://localhost:8080/ \
        --header 'content-type: application/json' \
        --data "{
        \"pathToMsRoots\": [\"${PATH_HERE}\"]
    }" > "${OUTPUT_FOLDER}/${OUTPUT_FILE}${COMMIT_HASH}.json"

done
