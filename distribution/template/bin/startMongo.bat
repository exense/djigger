rem @echo off

SET MONGO_PATH=
rem set your own mongo path above if the mongod.exe isn't on your PATH, for example :
rem SET MONGO_PATH="D:\Program Files\MongoDB\Server\3.0\bin\"

for /f "skip=1" %%x in ('wmic os get localdatetime') do if not defined mydate set mydate=%%x

%MONGO_PATH%mongod.exe -dbpath ..\data\mongodb > ..\log\mongod_%mydate%.log 2>&1
