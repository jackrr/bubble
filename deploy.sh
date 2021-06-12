#!/usr/bin/env bash
set -euo pipefail

lein ring uberjar

IMAGE_VERSION=$(git rev-parse --short=8 HEAD)
IMAGE_TAG=jackratner/bubble:${IMAGE_VERSION}
docker build . -t ${IMAGE_TAG}
docker push ${IMAGE_TAG}
