#!/bin/bash
echo "================================================"
echo "  Sistema de Monitoramento do Rio Tietê - UNIP"
echo "================================================"
echo ""

# Compila se necessário
if [ ! -f "out/client/ClientGUI.class" ]; then
    echo "Compilando o projeto..."
    mkdir -p out
    javac -encoding UTF-8 -d out src/common/Message.java
    javac -encoding UTF-8 -cp out -d out src/server/ServerGUI.java src/server/ClientHandler.java src/server/ChatServer.java
    javac -encoding UTF-8 -cp out -d out src/client/ChatClient.java src/client/ClientGUI.java
    echo "Compilação concluída!"
    echo ""
fi

echo "Iniciando cliente (Inspetor)..."
java -cp out client.ClientGUI
