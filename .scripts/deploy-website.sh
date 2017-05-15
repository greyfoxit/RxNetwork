#!/usr/bin/env bash
#
# Deploy to project's github pages.
#
# @author @radekkozak https://github.com/radekkozak

set -e +o pipefail

SLUG="greyfox/rxnetwork"
JDK="oraclejdk8"
BRANCH="master"

GROUP_ID="it.greyfox"
ARTIFACT_ID="rxnetwork"

if [ "$TRAVIS_REPO_SLUG" != "$SLUG" ]; then
  echo "Skipping snapshot deployment: wrong repository. Expected '$SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_JDK_VERSION" != "$JDK" ]; then
  echo "Skipping snapshot deployment: wrong JDK. Expected '$JDK' but was '$TRAVIS_JDK_VERSION'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "Skipping snapshot deployment: was pull request."
elif [ "$TRAVIS_BRANCH" != "$BRANCH" ]; then
  echo "Skipping snapshot deployment: wrong branch. Expected '$BRANCH' but was '$TRAVIS_BRANCH'."
else
  echo "Deploying website..."

  CUSTOM_LAYOUT='---\nlayout: default\n---\n'
  CUSTOM_TITLE='Documentation'

  # Switch to gh-pages branch to sync it with master
  git checkout gh-pages

  # Sync the README.md from master branch
  git checkout master -- README.md

  # Prepend jekyll layout header
  echo -e ${CUSTOM_LAYOUT} > index.md

  # Prepend custom title
  echo -e ${CUSTOM_TITLE} >> index.md

  # Sync README (without project name header)
  tail -n +2 README.md >> index.md
  rm README.md

  # Download the latest javadoc
  #curl -L "http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=$GROUP_ID&a=$ARTIFACT_ID&v=LATEST&c=javadoc" > javadoc.zip
  #unzip javadoc.zip -d javadoc
  #rm javadoc.zip

  git add -A
  if [[ `git status --porcelain` ]]; then
	git commit -m "Website at $(date)"
    git push origin gh-pages
    echo "Website deployed!"
  fi

fi
