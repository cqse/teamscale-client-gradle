#!/usr/bin/bash
export JAVA_HOME=/e/bin/java8/

./gradlew webgoat:uploadBuildStatus
./gradlew webgoat:uploadTestResults

# Set the cache back just be before the last build
sed -i 's/67363/67362/g' projects/webgoat_vsts_cache.json
