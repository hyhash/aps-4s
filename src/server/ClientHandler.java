package server;

import common.Message;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // First message is the JOIN with the username
            Message joinMsg = (Message) in.readObject();
            if (joinMsg.getType() == Message.Type.JOIN) {
                clientName = joinMsg.getSender();
                ChatServer.registerClient(clientName, this);
                // A lista de usuários online é enviada para TODOS dentro de registerClient()
            }

            // Main loop
            Message msg;
            while ((msg = (Message) in.readObject()) != null) {
                switch (msg.getType()) {
                    case TEXT:
                        ChatServer.broadcast(msg, clientName); // exclui remetente para evitar duplicidade
                        break;
                    case PRIVATE:
                        ChatServer.sendPrivate(msg);
                        break;
                    case FILE:
                        ChatServer.sendFile(msg);
                        break;
                    case LEAVE:
                        return;
                    default:
                        break;
                }
            }
        } catch (EOFException | SocketException e) {
            // Client disconnected
        } catch (Exception e) {
            ChatServer.log("Erro com cliente " + clientName + ": " + e.getMessage());
        } finally {
            if (clientName != null) {
                ChatServer.removeClient(clientName);
            }
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public synchronized void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            ChatServer.log("Erro ao enviar para " + clientName + ": " + e.getMessage());
        }
    }
}
