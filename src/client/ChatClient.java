package client;

import common.Message;

import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private String serverAddress;
    private int serverPort;
    private ClientGUI gui;
    private volatile boolean connected = false;

    public ChatClient(String serverAddress, int serverPort, String username, ClientGUI gui) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.username = username;
        this.gui = gui;
    }

    public boolean connect() {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;

            // Send JOIN message
            sendMessage(new Message(Message.Type.JOIN, username, "Conectado ao sistema."));

            // Start listener thread
            new Thread(this::listenForMessages).start();
            return true;
        } catch (IOException e) {
            gui.showError("Não foi possível conectar ao servidor: " + e.getMessage());
            return false;
        }
    }

    private void listenForMessages() {
        try {
            Message msg;
            while (connected && (msg = (Message) in.readObject()) != null) {
                gui.receiveMessage(msg);
            }
        } catch (EOFException | SocketException e) {
            if (connected) gui.onDisconnected();
        } catch (Exception e) {
            if (connected) gui.showError("Erro de conexão: " + e.getMessage());
        }
    }

    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            gui.showError("Erro ao enviar mensagem: " + e.getMessage());
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (out != null) sendMessage(new Message(Message.Type.LEAVE, username, "Saindo..."));
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public String getUsername() { return username; }
    public boolean isConnected() { return connected; }
}
