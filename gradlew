#!/bin/sh
#
# Gradle start up script for POSIX generated for ReTube (NOVA Tube)
#
set -e

APP_HOME=$( cd "${0%/*}" && pwd -P ) || exit

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -f "$WRAPPER_JAR" ]; then
    exec "$JAVACMD" -Xmx64m -Xms64m -jar "$WRAPPER_JAR" "$@"
elif command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "Gradle wrapper JAR not found. Run 'gradle wrapper' or use the GitHub Actions workflow (.github/workflows/android-build.yml) which sets up Gradle 8.9 automatically."
    exit 1
fi