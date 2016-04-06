@echo off
SET MONGO_PATH="D:\Tools\mongodb-win32-x86_64-enterprise-windows-64-3.2.4\bin\"

for /f "skip=1" %%x in ('wmic os get localdatetime') do if not defined mydate set mydate=%%x

"%MONGO_PATH%\mongod.exe" -dbpath ..\data\mongodb > ..\log\mongod_%mydate%.log 2>&1

