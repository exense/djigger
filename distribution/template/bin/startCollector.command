#!/bin/sh

JAVA_PATH=""
#JAVA_PATH="/usr/sbin/jre1.8.0_77/bin/"

ABSPATH=$(cd "$(dirname "$0")"; pwd)

JAVA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1101 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -DcollectorConfig=${ABSPATH}/../conf/Collector.xml -DconnectionsConfig=${ABSPATH}/../conf/Connections.csv -Dlogback.configurationFile=logback-collector.xml"
# Use -DconnectionsConfig=${ABSPATH}/../conf/Connections.csv" if you wan't to use the CSV configuration format

${JAVA_PATH}java ${JAVA_OPTS} -cp ${ABSPATH}/../lib/*: io.djigger.collector.server.Server