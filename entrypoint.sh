#!/bin/bash

file=$(find data/inputs -type f -name "0" -print -quit)
cp "$file" data/inputs && tar -xvf "$file" -C data/inputs --strip-components=1

java -jar /app/app.jar validate-opendrive data/inputs data/outputs