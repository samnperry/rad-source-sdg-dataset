#!/bin/bash
PATH_HERE=/Users/abakhtin22/Documents/TempNet/graal/train-ticket/
curl --request POST \
    --url http://localhost:8080/ \
    --header 'content-type: application/json' \
    --data "{
      \"pathToMsRoots\": [\"${PATH_HERE}\"]
  }" > output.json

