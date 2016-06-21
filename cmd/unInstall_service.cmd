@ECHO OFF
setlocal

REM Скрипт удаления сервиса сообщений.
REM V 1.0.0 - первая эксплуатационная версия.
REM V 1.0.1 - разделение цепочки остановки и удаления.
REM V 1.0.2 - обработка сообщений соманды sc.

REM==================================================
ECHO .
ECHO ==================================================
ECHO %date% %time% - TRY UNISTALL MTBmessenger SERVICE

REM==================================================
REM Проверка наличия сервиса.
sc getdisplayname MTBmessenger|find "FAILED" && (
ECHO %date% %time% - OK, SERVICE MTBmessenger NOT FOUND
EXIT /B 0
)

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