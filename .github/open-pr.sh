#!/usr/bin/env bash
set -e

module_label=$1
tracked_path=$2

# prepare git commit message
cat - <<EOF > /tmp/commit-msg.txt
⬆️  Update $module_label $old_build → $new_build

**Changelog**

EOF

# generate changelog
echo "🗄 Cloning openlink/virtuoso-opensource to generate a changelog"
git clone --shallow-since=2019-01-01 https://github.com/openlink/virtuoso-opensource.git /tmp/virtuoso
export LOG_FORMAT='- [`%h`](https://github.com/openlink/virtuoso-opensource/commit/%H) %s'
git -C /tmp/virtuoso log --pretty="format:$LOG_FORMAT" $old_rev..$new_rev -- $tracked_path >> /tmp/commit-msg.txt

git config user.email "$GITHUB_ACTOR@users.noreply.github.com"
git config user.name "Github Actions"

# update readme with latest versions
sed -i "s/$old_build/$new_build/g" README.md

git checkout -b update-$(echo $module_label | tr '[:upper:] ' '[:lower:]-')
git add .
git commit -F /tmp/commit-msg.txt

echo "🎉 Opening pull request"
git log --graph --oneline --decorate --all
hub pull-request --push --no-edit --assign jeremiehuchet --base main
