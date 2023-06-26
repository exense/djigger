rem @echo off

rem if java.exe isn't on your path or is too old, then set your own as follows (watch for the backslash at the end):
rem SET JAVA_PATH=C:\Program Files\Java\jdk1.8.0_101\bin\
SET JAVA_PATH=

set execdir=%~dp0
pushd %execdir%\..

set DJIGGER_HOME=%CD%
set DJIGGER_CONFDIR=%DJIGGER_HOME%\conf\
set DJIGGER_LIBDIR=%DJIGGER_HOME%\lib\
popd

set START_OPTS="-Dlogback.configurationFile=%DJIGGER_CONFDIR%logback-client.xml" %JAVA_OPTS%
REM use following line to scale the UI size (in case of Windows scaling issues)
REM set START_OPTS=" -Dsun.java2d.uiScale=2" %START_OPTS%
cd /d %DJIGGER_HOME%
"%JAVA_PATH%java.exe" %START_OPTS% -cp "%DJIGGER_LIBDIR%*;%JAVA_PATH%\..\lib\tools.jar" io.djigger.ui.MainFrame