#!/usr/bin/env bash
#
# Deploy a jar, source jar, and javadoc jar to Sonatype's snapshot repo.
#
# Adapted from https://coderwall.com/p/9b_lfq and
# http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/

set -eo pipefail

SLUG="greyfoxit/RxNetwork"
BRANCH="master"

CIRCLE_REPO_SLUG="$CIRCLE_PROJECT_USERNAME/$CIRCLE_PROJECT_REPONAME"

if [ "$CIRCLE_REPO_SLUG" != "$SLUG" ]; then
  echo "Skipping snapshot deployment: wrong repository. Expected '$SLUG' but was '$CIRCLE_REPO_SLUG'."
elif [ "$CIRCLE_PULL_REQUEST" != "" ]; then
  echo "Skipping snapshot deployment: was pull request."
elif [ "$CIRCLE_BRANCH" != "$BRANCH" ]; then
  echo "Skipping snapshot deployment: wrong branch. Expected '$BRANCH' but was '$CIRCLE_BRANCH'."
else
  echo "Deploying snapshot..."
  ./gradlew uploadArchives
  echo "Snapshot deployed!"
fi
