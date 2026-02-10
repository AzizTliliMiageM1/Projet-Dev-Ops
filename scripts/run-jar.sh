#!/usr/bin/env bash
if [ -z "$(ls target/*-shaded.jar 2>/dev/null)" ]; then
  echo "No shaded jar found in target/. Build first: ./scripts/build.sh"
  exit 1
fi
java -jar target/*-shaded.jar
