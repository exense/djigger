JAVA_PATH="/usr/sbin/jre1.8.0_77/bin"

JAVA_OPTS=

${JAVA_PATH}/java ${JAVA_OPTS} -cp "../lib/*" io.djigger.ui.MainFrame > ../log/client_$(date +"%s").log 2>&1

