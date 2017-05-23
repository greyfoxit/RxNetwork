#!/usr/bin/env bash
#
# Deploy code coverage report to Codecov
#

set -euo pipefail

REMOTE_RUN_BRANCH="remote-run"

if [[ "$TRAVIS_BRANCH" =~ "$REMOTE_RUN_BRANCH" ]]; then
  echo "Skipping code coverage deployment: on a 'remote-run' branch."
else
  bash <(curl -s https://codecov.io/bash) -t $CODECOV_TOKEN;
fi
