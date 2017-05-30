#!/usr/bin/env bash
#
# Deploy to project's github pages.
#
# @author radekkozak

set -eo pipefail

CIRCLE_REPO_SLUG="$CIRCLE_PROJECT_USERNAME/$CIRCLE_PROJECT_REPONAME"

SLUG="radekkozak/RxNetwork"
BRANCH="master"

REPO=git@github.com:radekkozak/RxNetwork.git
GROUP_ID="it.greyfox"
ARTIFACT_ID="rxnetwork"

if [ "$CIRCLE_REPO_SLUG" != "$SLUG" ]; then
  echo "Skipping deployment: wrong repository. Expected '$SLUG' but was '$CIRCLE_REPO_SLUG'."
elif [ "$CIRCLE_PULL_REQUEST" != "" ]; then
  echo "Skipping deployment: was pull request."
elif [ "$CIRCLE_BRANCH" != "$BRANCH" ]; then
  echo "Skipping deployment: wrong branch. Expected '$BRANCH' but was '$CIRCLE_BRANCH'."
else
  echo "Deploying website..."

  DIR=temp

  # Clone the current repo into temp folder
  git clone $REPO $DIR && cd $DIR

  git checkout -t origin/gh-pages

  CUSTOM_LAYOUT='---\nlayout: default\n---\n'
  CUSTOM_TITLE='Documentation'

  git config --global user.email "bot@greyfox.it"
  git config --global user.name "Greyfox Bot"

  # Prepend jekyll layout header
  echo -e $CUSTOM_LAYOUT > index.md

  # Append custom title
  echo -e '# '$CUSTOM_TITLE >> index.md

  # Sync README from master branch (without project name header)
  git checkout master -- README.md
  tail -n +2 README.md >> index.md
  rm README.md

  # Download the latest javadoc
  curl -L "http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=$GROUP_ID&a=$ARTIFACT_ID&v=LATEST&c=javadoc" > javadoc.zip
  mkdir -p javadoc

  JAVADOC_RESULT="$(unzip -o javadoc.zip -d javadoc 2>&1 > /dev/null)"

  if [[ $JAVADOC_RESULT == "" ]]; then
	echo "Javadoc extracted"
  else
	echo "Skipping Javadoc: $JAVADOC_RESULT"
  fi

  rm javadoc.zip

  # Stage files
  git add -A .

  # Commit if needed
  if [[ `git status --porcelain` ]]; then
	git commit -m "Website at $(date)"
    git push origin gh-pages
    echo "Website deployed!"
  else
    echo "Skipping deployment: no changes detected"
  fi

  # Clean up
  cd ..
  rm -rf $DIR

fi
