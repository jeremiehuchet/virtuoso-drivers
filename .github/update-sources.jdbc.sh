#!/usr/bin/env bash
set -e

OPENLINK_VIRTUOSO_REF=${1:-develop/7}

echo "⬆ Update sources reference to $OPENLINK_VIRTUOSO_REF"
nix-prefetch-github openlink virtuoso-opensource --rev $OPENLINK_VIRTUOSO_REF > virtuoso-github.json

echo "👨‍🏭 Generate up to date sources"
nix-build --quiet virtuoso-java-sources.nix

echo "Exporting some environment variables"
old_rev=$(xmlstarlet sel -N 'x=http://maven.apache.org/POM/4.0.0' -t -v '/x:project/x:properties/x:openlink.virtuoso-opensource.gitrev' virtuoso-jdbc-4.2/pom.xml)
new_rev=$(jq -r .rev virtuoso-github.json)
old_build=$(xmlstarlet sel -N 'x=http://maven.apache.org/POM/4.0.0' -t -v '/x:project/x:version' virtuoso-jdbc-4.2/pom.xml | sed 's/^42\.//')
new_build=$(cat result/build-version)

cat - <<EOF >> $GITHUB_ENV
old_rev=$old_rev
new_rev=$new_rev
old_build=$old_build
new_build=$new_build
EOF

echo "🎨 Override source files"
for version in 4.0 4.1 4.2 ; do
  # update gitrev and build version in pom file
  sed -i "s/$old_rev/$new_rev/g"     virtuoso-jdbc-$version/pom.xml
  sed -i "s/$old_build/$new_build/g" virtuoso-jdbc-$version/pom.xml
  # override sources
  rm -rf virtuoso-jdbc-$version/src/main/java
  cp -rv --no-preserve=mode result/$version virtuoso-jdbc-$version/src/main/java
done
