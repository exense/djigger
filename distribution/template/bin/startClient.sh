#!/bin/bash

JAVA_PATH=""
#JAVA_PATH="/usr/sbin/jre1.8.0_77/bin/"

execdir="$(readlink -f `dirname ${BASH_SOURCE[0]}`)"

DJIGGER_HOME="${DJIGGER_HOME:-$(dirname ${execdir})}"
DJIGGER_CONFDIR="${DJIGGER_CONFDIR:-${DJIGGER_HOME}/conf}"
DJIGGER_LIBDIR="${DJIGGER_LIBDIR:-${DJIGGER_HOME}/lib}"

START_OPTS=()
START_OPTS+=("-Dlogback.configurationFile=${DJIGGER_CONFDIR}/logback-client.xml")
START_OPTS+=("${JAVA_OPTS}")

cd "${DJIGGER_HOME}" \
    && exec "${JAVA_HOME}java" ${START_OPTS[@]} -cp "${DJIGGER_LIBDIR}/*:${JAVA_PATH}/../lib/tools.jar" io.djigger.ui.MainFrame \
    || echo "Error: Invalid DJIGGER_HOME"
