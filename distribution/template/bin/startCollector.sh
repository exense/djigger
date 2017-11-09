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
DJIGGER_OPTS+=("-DcollectorConfig=${DJIGGER_CONFDIR}/Collector.xml")
DJIGGER_OPTS+=("-Dlogback.configurationFile=${DJIGGER_CONFDIR}/logback-collector.xml")

# Custom JVM arguments can be set by exporting JAVA_OPTS in your profile

if [ -n "${JAVA_HOME:-${DJIGGER_JAVA_HOME}}" ]; then
    RUNJAVA="${JAVA_HOME:-${DJIGGER_JAVA_HOME}}/bin/java"
else
    RUNJAVA="java"
fi

cd "${DJIGGER_HOME}" \
    && exec "${RUNJAVA}" ${DJIGGER_OPTS[@]} -cp "${DJIGGER_LIBDIR}/*" \
         ${JAVA_OPTS} io.djigger.collector.server.Server \
    || echo "Error: Invalid DJIGGER_HOME"
