#!/usr/bin/env bash
set -e

LATEST_REF=${1:-develop/7}

echo "🕵 Get latest jars checksums for $LATEST_REF"
for version in 4.0 4.1 4.2 4.3 ; do
  v=$(echo $version | tr '.' '_' | sed 's/_0$//')
  latest_baseurl=https://github.com/openlink/virtuoso-opensource/raw/$LATEST_REF/libsrc/JDBCDriverType4
  wget -nv $latest_baseurl/virtjdbc$v.jar
done
# compute checksums
sha256sum virtjdbc*.jar > latest.sha256
rm virtjdbc*.jar

echo "🕵 Compare with current jars checksums..."
for version in 4.0 4.1 4.2 4.3 ; do
  # openlink use short underscore version naming: 4.0 → 4, 4.1 → 4_1
  v=$(echo $version | tr '.' '_' | sed 's/_0$//')
  gitrev=$(xmlstarlet sel -N 'x=http://maven.apache.org/POM/4.0.0' \
     -t -v '/x:project/x:properties/x:openlink.virtuoso-opensource.gitrev' virtuoso-jdbc-$version/pom.xml)
  current_baseurl=https://github.com/openlink/virtuoso-opensource/raw/$gitrev/libsrc/JDBCDriverType4
  wget -nv $current_baseurl/virtjdbc$v.jar
done
# compare checksums
sha256sum --check latest.sha256 && {
  echo "✔ no update available"
  exit 0
} || {
  echo "✋ an update is available"
  exit 1
}
