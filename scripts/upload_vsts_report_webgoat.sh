#!/usr/bin/bash
export JAVA_HOME=/e/bin/java8/
#export HTTP_PROXY=http://httpproxy.munich.munichre.com:3128

./gradlew cs:webgoat:downloadReports

# Set the cache back just be before the last build
sed -i 's/67363/67362/g' projects/webgoat_vsts_cache.json
