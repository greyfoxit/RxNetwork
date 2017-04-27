#!/bin/bash
#
# Deploy javadoc to project's github pages.
#
# @author @JakeWharton https://github.com/JakeWharton

set -ex

REPO="git@gitlab.com:radekkozak/RxNetwork.git"
GROUP_ID="inc.greyfox"
ARTIFACT_ID="rxnetwork"

DIR=temp-clone

# Delete any existing temporary website clone
rm -rf $DIR

# Clone the current repo into temp folder
git clone $REPO $DIR

# Move working directory into temp folder
cd $DIR

# Temporarily copy new README
cp README.md README-new.md

# Checkout and track the gh-pages branch
git checkout -t origin/pages

# Delete everything
#rm -rf *

# Delete old README
rm README.md

mv README-new.md README.md

# Download the latest javadoc
#curl -L "http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=$GROUP_ID&a=$ARTIFACT_ID&v=LATEST&c=javadoc" > javadoc.zip
#unzip javadoc.zip -d javadoc
#rm javadoc.zip

# Stage all files in git and create a commit
git add .
git add -u
git commit -m "Updated website"

# Push the new files up to GitHub
git push origin pages

# Delete temp folder
cd ..
rm -rf $DIR

