JAVA_PATH=""
#JAVA_PATH="/usr/sbin/jre1.8.0_77/bin/"

JAVA_OPTS="-DcollectorConfig=../conf/Collector.xml -DconnectionsConfig=../conf/Connections.xml"
# Use -DconnectionsConfig=../conf/Connections.csv" if you wan't to use the CSV configuration format

${JAVA_PATH}java ${JAVA_OPTS} -cp "../lib/*" io.djigger.collector.server.Server > ../log/collector_$(date +"%s").log 2>&1