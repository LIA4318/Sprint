#!/bin/sh
set -eu

PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
TOMCAT_HOME=${TOMCAT_HOME:-/tmp/apache-tomcat-10.1.44}
SERVLET_API="$PROJECT_DIR/webapp/WEB-INF/lib/servlet-api.jar"
BUILD_DIR=$(mktemp -d /tmp/sprint6-war.XXXXXX)
WAR="$BUILD_DIR/sprint4.war"

if [ ! -x "$TOMCAT_HOME/bin/catalina.sh" ]; then
    printf 'Tomcat introuvable: %s\n' "$TOMCAT_HOME/bin/catalina.sh" >&2
    exit 1
fi

mkdir -p "$BUILD_DIR/WEB-INF/classes" "$BUILD_DIR/WEB-INF/vues"
cp "$PROJECT_DIR/webapp/WEB-INF/web.xml" "$BUILD_DIR/WEB-INF/web.xml"
cp "$PROJECT_DIR/webapp/WEB-INF/vues/accueil.jsp" "$BUILD_DIR/WEB-INF/vues/accueil.jsp"

# Compiler les sources du Sprint 6, sans embarquer les anciens JAR du build.
find "$PROJECT_DIR/src" -type f -name '*.java' -print0 \
    | xargs -0 javac -cp "$SERVLET_API" -d "$BUILD_DIR/WEB-INF/classes"

jar --create --file "$WAR" -C "$BUILD_DIR" .
cp "$WAR" "$TOMCAT_HOME/webapps/sprint4.war"
printf 'WAR Sprint 6 deploye: %s/webapps/sprint4.war\n' "$TOMCAT_HOME"