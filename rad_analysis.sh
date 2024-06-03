#!/bin/bash
PATH_HERE=/Users/SamP9/OneDrive/Documents/GitHub/train-ticket
curl --request POST \
    --url http://localhost:8080/ \
    --header 'content-type: application/json' \
    --data "{
      \"pathToMsRoots\": [\"${PATH_HERE}\"]
  }" > output.json

