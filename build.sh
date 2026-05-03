#!/bin/bash
# Build script for APS Chat System

mkdir -p out

echo "Compilando classes comuns..."
javac -d out src/common/Message.java

echo "Compilando servidor..."
javac -cp out -d out src/server/ServerGUI.java src/server/ClientHandler.java src/server/ChatServer.java

echo "Compilando cliente..."
javac -cp out -d out src/client/ChatClient.java src/client/ClientGUI.java

echo ""
echo "Build concluído! Para executar:"
echo "  Servidor: java -cp out server.ChatServer"
echo "  Cliente:  java -cp out client.ClientGUI"
