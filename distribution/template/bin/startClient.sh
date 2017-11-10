#!/bin/bash
execdir="$(readlink -f `dirname ${BASH_SOURCE[0]}`)"

# If you don't have `java` in your $PATH or you want to use a different JDK, either export
# JAVA_HOME in your profile or adjust and uncomment the definition below
#
#DJIGGER_JAVA_HOME="/usr/lib/jdk1.8.0_77"

DJIGGER_HOME="${DJIGGER_HOME:-$(dirname ${execdir})}"
DJIGGER_CONFDIR="${DJIGGER_CONFDIR:-${DJIGGER_HOME}/conf}"
DJIGGER_LIBDIR="${DJIGGER_LIBDIR:-${DJIGGER_HOME}/lib}"

# Required JVM arguments for djigger
DJIGGER_OPTS=("-Dlogback.configurationFile=${DJIGGER_CONFDIR}/logback-client.xml")

# For the direct attach feature the JDK home must be defined so we can properly add tools.jar
# to the classpath
if [ -n "${JAVA_HOME:-${DJIGGER_JAVA_HOME}}" ]; then
    JAVA="${JAVA_HOME:-${DJIGGER_JAVA_HOME}}/bin/java"
    TOOLS_JAR=":${JAVA_HOME:-${DJIGGER_JAVA_HOME}}/lib/tools.jar"
fi

# Custom JVM arguments can be set by exporting JAVA_OPTS in your profile
cd "${DJIGGER_HOME}" \
    && exec "${JAVA:-java}" ${DJIGGER_OPTS[@]} -cp "${DJIGGER_LIBDIR}/*${TOOLS_JAR}" \
         ${JAVA_OPTS} io.djigger.ui.MainFrame \
    || echo "Error: Invalid DJIGGER_HOME"