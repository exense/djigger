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

# Custom JVM arguments can be set by exporting JAVA_OPTS in your profile

if [ -n "${JAVA_HOME:-${DJIGGER_JAVA_HOME}}" ]; then
    RUNJAVA="${JAVA_HOME:-${DJIGGER_JAVA_HOME}}/bin/java"
    JDK_LIBS="${JAVA_HOME:-${DJIGGER_JAVA_HOME}}/lib"
else
    RUNJAVA="java"
fi

cd "${DJIGGER_HOME}" \
    && exec "${RUNJAVA}" ${DJIGGER_OPTS[@]} -cp "${DJIGGER_LIBDIR}/*:${JDK_LIBS}/tools.jar"
         ${JAVA_OPTS} io.djigger.ui.MainFrame \
    || echo "Error: Invalid DJIGGER_HOME"