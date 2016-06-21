@ECHO OFF
setlocal

REM Скрипт установки сервиса сообщений.
REM V 1.0.0 - первая эксплуатационная версия.
REM V 1.0.1 - ошибка проверки наличия java.
REM V 1.0.2 - проверка результатов работы sc.

REM==================================================
ECHO .
ECHO ==================================================
ECHO %date% %time% - START ISTALL MTBmessenger SERVICE

REM==================================================
REM Установка переменных
SET RUNDIR=%~dp0
IF %RUNDIR:~-1%==\ SET LOGDIR=%RUNDIR:~0,-1%
SET serverAdress=10.104.4.43
SET serverPort=3000
SET clientGroup=all

REM==================================================
REM Проверка наличия сервиса.
sc getdisplayname MTBmessenger|find "SUCCESS" && (
ECHO %date% %time% - OK, SERVICE MTBmessenger ALREADY INSTALLED
EXIT /B 0
)
ECHO %date% %time% - SERVICE MTBmessenger NOT FOUND

REM==================================================
REM Проверка наличия java
java -version && (
ECHO %date% %time% - OK, JAVA ALREADY INSTALLED
GOTO installMessageService
)
ECHO %date% %time% - ERROR, JAVA NOT FOUND

REM==================================================
REM Установка java
ECHO %date% %time% - TRY TO INSTALL JAVA
for /f %%a IN ('dir /o:d "%RUNDIR%jre-*.exe" /b') do (
ECHO %date% %time% - RUN %RUNDIR%%%a
%RUNDIR%%%a /s || (
ECHO %date% %time% - ERROR INSTALL JAVA
EXIT /B 1
)
GOTO exitJREInstall
)
:exitJREInstall
ECHO %date% %time% - OK, java successfully installed.

:installMessageService
REM==================================================
REM Установка сервиса.
ECHO %date% %time% - TRY TO INSTALL MESSAGE SERVICE
"%RUNDIR%prunsrv.exe" //IS//MTBmessenger --DisplayName="MTBmessenger" --Install="%RUNDIR%prunsrv.exe" --Classpath="%RUNDIR%MTBmessenger.jar" --Jvm=auto --StartMode=jvm --StartClass=mtbmessenger.MTBmessenger --StartMethod=main --StartParams=start;%serverAdress%;%serverPort%;%clientGroup% --StopMode=jvm --StopClass=mtbmessenger.MTBmessenger --StopMethod=main --StopParams=stop --LogPath="%LOGDIR%" --StdOutput=auto --StdError=auto || (
ECHO %date% %time% - ERROR INSTALL SERVICE
GOTO uninstallMessageService
)
ECHO %date% %time% - OK, INSTALL MTBmessenger SERVICE

:configMessageService
REM==================================================
REM Настройка сервиса.
ECHO %date% %time% - TRY TO CONFIG MTBmessenger SERVICE
sc config MTBmessenger type= own type= interact start= auto|find "FAILED" && (
ECHO %date% %time% - ERROR CONFIG type AND start MTBmessenger SERVICE
GOTO uninstallMessageService
)
sc failure MTBmessenger reset= 0 actions= restart/60000|find "FAILED" && (
ECHO %date% %time% - ERROR CONFIG restart MTBmessenger SERVICE
GOTO uninstallMessageService
)
ECHO %date% %time% - OK, CONFIG MTBmessenger SERVICE

REM==================================================
REM Запуск сервиса.
sc start MTBmessenger|find "FAILED" && (
ECHO %date% %time% - ERROR START MTBmessenger SERVICE
GOTO uninstallMessageService
)
ECHO %date% %time% - OK, START MTBmessenger SERVICE
EXIT /B 0

:uninstallMessageService
REM==================================================
REM Удаление сервиса.
ECHO %date% %time% - TRY STOP MTBmessenger SERVICE
sc stop MTBmessenger|find "FAILED" && (
ECHO %date% %time% - ERROR STOP MTBmessenger SERVICE
EXIT /B 1
)
ECHO %date% %time% - TRY REMOVE MTBmessenger SERVICE
sc delete MTBmessenger|find "FAILED" && (
ECHO %date% %time% - ERROR REMOVE MTBmessenger SERVICE
EXIT /B 1
)
ECHO %date% %time% - OK, REMOVE MTBmessenger SERVICE
EXIT /B 0