#!/usr/bin/env bash
#
# Deploy code coverage report to Codecov
#
# @author radekkozak

set -eo pipefail

REMOTE_RUN_BRANCH="remote-run"

if [[ "$CIRCLE_BRANCH" =~ "$REMOTE_RUN_BRANCH" ]]; then
  echo "Skipping code coverage deployment: on a 'remote-run' branch."
else
  bash <(wget -O - https://codecov.io/bash) -t $CODECOV_TOKEN;
fi
