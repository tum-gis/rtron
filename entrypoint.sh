#!/bin/bash

file=$(find data/inputs -type f -name "0" -print -quit)
cp "$file" data/inputs

java -jar /app/app.jar validate-opendrive data/inputs data/outputs