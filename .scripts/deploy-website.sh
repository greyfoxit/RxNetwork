#!/usr/bin/env bash
#
# Deploy to project's github pages.
#
# @author @radekkozak https://github.com/radekkozak

set -euo pipefail

SLUG="radekkozak/RxNetwork"
JDK="oraclejdk8"
BRANCH="master"

REPO=git@github.com:radekkozak/RxNetwork.git
GROUP_ID="it.greyfox"
ARTIFACT_ID="rxnetwork"

TEMP_DIR=temp

if [ "$TRAVIS_REPO_SLUG" != "$SLUG" ]; then
  echo "Skipping deployment: wrong repository. Expected '$SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_JDK_VERSION" != "$JDK" ]; then
  echo "Skipping deployment: wrong JDK. Expected '$JDK' but was '$TRAVIS_JDK_VERSION'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "Skipping deployment: was pull request."
elif [ "$TRAVIS_BRANCH" != "$BRANCH" ]; then
  echo "Skipping deployment: wrong branch. Expected '$BRANCH' but was '$TRAVIS_BRANCH'."
else
  echo "Deploying website..."

  CUSTOM_LAYOUT='---\nlayout: default\n---\n'
  CUSTOM_TITLE='Documentation'

  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis"

  # Fetch and checkout gh-pages
  git fetch origin +refs/heads/gh-pages:refs/remotes/origin/gh-pages
  git checkout -b gh-pages origin/gh-pages

  # Clean working directory before doing anything
  git clean -f -d

  # Prepend jekyll layout header
  echo -e ${CUSTOM_LAYOUT} > index.md

  # Prepend custom title
  echo -e '# '${CUSTOM_TITLE} >> index.md

  # Sync README from master branch (without project name header)
  git checkout master -- README.md
  tail -n +2 README.md >> index.md
  rm README.md

  # Download the latest javadoc
  #curl -L "http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=$GROUP_ID&a=$ARTIFACT_ID&v=LATEST&c=javadoc" > javadoc.zip
  #unzip javadoc.zip -d javadoc
  #rm javadoc.zip

  git add -A
  if [[ `git status --porcelain` ]]; then
	git commit -m "Website at $(date)"
    git push -fq origin gh-pages
    echo "Website deployed!"
  else
    echo "Skipping deployment: no changes detected"
  fi

fi
