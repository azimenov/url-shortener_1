#!/bin/bash

cd ../url-shortener-read
./gradlew build -x test

cd ../url-shortener-write
./gradlew build -x test

cd ../docker

docker-compose up -d