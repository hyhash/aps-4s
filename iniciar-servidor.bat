@echo off
echo ================================================
echo   Sistema de Monitoramento do Rio Tietê - UNIP
echo ================================================
echo.

REM Compila se necessario
if not exist "out\server\ChatServer.class" (
    echo Compilando o projeto...
    if not exist "out" mkdir out
    javac -encoding UTF-8 -d out src\common\Message.java
    javac -encoding UTF-8 -cp out -d out src\server\ServerGUI.java src\server\ClientHandler.java src\server\ChatServer.java
    javac -encoding UTF-8 -cp out -d out src\client\ChatClient.java src\client\ClientGUI.java
    echo Compilacao concluida!
    echo.
)

echo Iniciando servidor...
java -cp out server.ChatServer
pause
