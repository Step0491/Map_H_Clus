@echo off

:: ----------------------------------
:: Configurazione
:: ----------------------------------
set "MYSQL_HOST=localhost"
set "MYSQL_PORT=3306"
set "MYSQL_USER=MapUser"
set "MYSQL_PASSWORD=map"
set "DATABASE_NAME=MapDB"
set "TABLE_NAME=exampletab"
set "SQL_SCRIPT=defaultSQLscript.sql"

:: ----------------------------------
:: Controllo esistenza tabella
:: ----------------------------------
echo Verifica se la tabella %TABLE_NAME% esiste...

:: Controlla l'esistenza della tabella e reindirizza l'output a nul
for /f "skip=1" %%A in ('mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% %DATABASE_NAME% -e "SHOW TABLES LIKE '%TABLE_NAME%';" 2^>nul') do (
    set "TABLE_EXISTS=%%A"
    goto :CHECK_TABLE
)

:CHECK_TABLE
if "%TABLE_EXISTS%"=="%TABLE_NAME%" (
    echo La tabella %TABLE_NAME% esiste gia'. Non verra' eseguito alcuno script SQL.
) else (
    echo La tabella %TABLE_NAME% non esiste. Esecuzione dello script SQL...
    mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% %DATABASE_NAME% < %SQL_SCRIPT% 2>nul 1>nul
    
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Errore durante l'esecuzione dello Script SQL: %SQL_SCRIPT%.
        pause
        exit /b 1
    ) else (
        echo Script SQL eseguito correttamente.
    )
)

:: ----------------------------------
:: Avvio Server
:: ----------------------------------
echo Avvio server...
java -jar server.jar "8080"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Errore durante l'avvio del server.
    pause
    exit /b 1
)

:: ----------------------------------
:: Fine dello script
:: ----------------------------------
pause
