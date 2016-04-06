rem @echo off
SET JAVA_PATH=
rem SET JAVA_PATH="D:\Programs\jdk1.7.0_79\jre\bin\"

for /f "skip=1" %%x in ('wmic os get localdatetime') do if not defined mydate set mydate=%%x

SET JAVA_OPTS= 

%JAVA_PATH%java.exe %JAVA_OPTS% -cp "..\lib\*" io.djigger.ui.MainFrame > ..\log\client_%mydate%.log 2>&1

