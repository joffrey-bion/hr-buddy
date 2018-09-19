#!/bin/bash

# injects env variables into the app's configuration
echo "{ \"serverUrl\": \"$SERVER_URL\" }" > /usr/share/nginx/html/assets/app-config.json

exec "$@"
