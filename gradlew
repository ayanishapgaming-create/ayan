#!/bin/sh
# Gradle wrapper shell script for POSIX-compatible shells

APP_HOME=$(cd "$(dirname "$0")" && pwd)
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Determine JVM options
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

exec "$JAVA_HOME/bin/java" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  "-Dorg.gradle.appname=$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain "$@"
