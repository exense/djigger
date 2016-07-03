rem @echo off

rem if java.exe isn't on your path or is too old, then set your own as follows (watch for the backslash and double quote at the end):
rem  SET JAVA_PATH="D:\Program Files\Java\jdk1.8.0_73\jre\bin"\
SET JAVA_PATH=

SET JAVA_OPTS=-DcollectorConfig=..\conf\Collector.xml -Dlogback.configurationFile=logback-collector.xml

%JAVA_PATH%java.exe %JAVA_OPTS% -cp "..\lib\*;" io.djigger.collector.server.Server