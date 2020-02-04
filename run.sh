#!/usr/bin/env bash

self="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
root="$1"

mvn install -DskipTests

rm -fr "$root/.bazel-gen"

cd "$root"
mvn -o -e org.neo4j:bazel-gen:module-locations -DrootDir="$root"
mvn -o -e org.neo4j:bazel-gen:module-deps      -DrootDir="$root"
mvn -o -e org.neo4j:bazel-gen:bazel-generate   -DrootDir="$root"

cat .bazel-gen/all-deps.txt | sort -u > .bazel-gen/all-deps.unique.txt
deps=$(cat .bazel-gen/all-deps.txt | sort -u)

cp -r "$self/src/bazel/tools" "$root/"
cp -r "$self/src/bazel/.bazelrc" "$root/"

cat <<EOF > WORKSPACE
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "bazel_skylib",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/1.0.2/bazel-skylib-1.0.2.tar.gz",
        "https://github.com/bazelbuild/bazel-skylib/releases/download/1.0.2/bazel-skylib-1.0.2.tar.gz",
    ],
    sha256 = "97e70364e9249702246c0e9444bccdc4b847bed1eb03c5a3ece4f83dfe6abc44",
)
load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")
bazel_skylib_workspace()

RULES_SCALA_VERSION="5dc2f776c2bab9b78148330b833573e191e7600f" # update this as needed

http_archive(
    name = "io_bazel_rules_scala",
    strip_prefix = "rules_scala-%s" % RULES_SCALA_VERSION,
    type = "zip",
    url = "https://github.com/bazelbuild/rules_scala/archive/%s.zip" % RULES_SCALA_VERSION,
)

load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")
scala_register_toolchains()

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")
scala_repositories((
    "2.12.10",
    {
       "scala_compiler": "cedc3b9c39d215a9a3ffc0cc75a1d784b51e9edc7f13051a1b4ad5ae22cfbc0c",
       "scala_library": "0a57044d10895f8d3dd66ad4286891f607169d948845ac51e17b4c1cf0ab569d",
       "scala_reflect": "56b609e1bab9144fb51525bfa01ccd72028154fc40a58685a1e9adcbe7835730"
    }
))

PROTOBUF_VERSION="09745575a923640154bcf307fba8aedff47f240a"
PROTOBUF_VERSION_SHA256="416212e14481cff8fd4849b1c1c1200a7f34808a54377e22d7447efdf54ad758"

http_archive(
    name = "com_google_protobuf",
    url = "https://github.com/protocolbuffers/protobuf/archive/%s.tar.gz" % PROTOBUF_VERSION,
    strip_prefix = "protobuf-%s" % PROTOBUF_VERSION,
    sha256 = PROTOBUF_VERSION_SHA256,
)

RULES_JVM_EXTERNAL_TAG = "3.0"
RULES_JVM_EXTERNAL_SHA = "62133c125bf4109dfd9d2af64830208356ce4ef8b165a6ef15bbff7460b35c3a"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [$(echo; for d in $deps; do echo "        \"$d\","; done)
      "org.junit.platform:junit-platform-console-standalone:1.5.2",
    ],
    repositories = [
        "https://repo1.maven.org/maven2",
        "https://dl.bintray.com/ldbc/snb/",
        "https://neo.jfrog.io/neo/benchmarking-thirdparty/",
    ],
)

maven_install(
    name = "maven-tools",
    artifacts = [
        "com.puppycrawl.tools:checkstyle:8.29",
    ],
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)
EOF
