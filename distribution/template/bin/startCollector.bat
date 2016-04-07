rem @echo off
SET JAVA_PATH=
rem SET JAVA_PATH="D:\Program Files\Java\jdk1.8.0_73\jre\bin\"

for /f "skip=1" %%x in ('wmic os get localdatetime') do if not defined mydate set mydate=%%x

SET JAVA_OPTS=-DcollectorConfig=..\conf\Collector.xml -DconnectionsConfig=..\conf\Connections.xml -Dlogback.configurationFile=logback-collector.xml
rem Use -DconnectionsConfig=../conf/Connections.csv" if you wan't to use the CSV configuration format

%JAVA_PATH%java.exe %JAVA_OPTS% -cp "..\lib\*;" io.djigger.collector.server.Server > collector_%mydate%.stdout 2>&1