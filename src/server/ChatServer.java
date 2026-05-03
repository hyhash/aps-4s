package server;

import common.Message;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static ServerGUI gui;
    private static boolean headless = false;

    public static void main(String[] args) {
        // Detect headless environment (no display)
        headless = GraphicsEnvironment.isHeadless();

        if (!headless) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    gui = new ServerGUI();
                    gui.setVisible(true);
                });
            } catch (Exception e) {
                System.out.println("[SERVER] Interface grafica nao disponivel, iniciando em modo console.");
                headless = true;
            }
        }

        startServer();

        // Keep main thread alive in console mode
        if (headless) {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignored) {}
        }
    }

    public static void startServer() {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                log("=================================================");
                log("  Sistema de Monitoramento do Rio Tietê - UNIP");
                log("=================================================");
                log("Servidor TCP iniciado na porta " + PORT);
                log("Aguardando conexões dos inspetores...");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    log("Nova conexão: " + clientSocket.getInetAddress().getHostAddress());
                    ClientHandler handler = new ClientHandler(clientSocket);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                log("ERRO no servidor: " + e.getMessage());
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(false); // keep JVM alive
        serverThread.start();
    }

    public static void registerClient(String name, ClientHandler handler) {
        clients.put(name, handler);
        log("Inspetor conectado: " + name + " | Total online: " + clients.size());

        // Notifica todos sobre a entrada
        broadcast(new Message(Message.Type.SERVER_INFO, "SERVIDOR",
                "Inspetor [" + name + "] entrou no sistema. Online: " + clients.size() + "\n"), null);

        // Envia lista atualizada para TODOS os clientes conectados
        broadcastUserList();
        updateClientList();
    }

    public static void removeClient(String name) {
        clients.remove(name);
        log("Inspetor desconectado: " + name + " | Total online: " + clients.size());

        // Notifica todos sobre a saída
        broadcast(new Message(Message.Type.SERVER_INFO, "SERVIDOR",
                "Inspetor [" + name + "] saiu do sistema. Online: " + clients.size() + "\n"), null);

        // Envia lista atualizada para TODOS os clientes restantes
        broadcastUserList();
        updateClientList();
    }

    public static void broadcast(Message msg, String excludeName) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (!entry.getKey().equals(excludeName)) {
                entry.getValue().sendMessage(msg);
            }
        }
        if (msg.getType() == Message.Type.TEXT) {
            log("[BROADCAST] " + msg.getSender() + ": " + msg.getContent());
        }
    }

    public static void sendPrivate(Message msg) {
        ClientHandler target = clients.get(msg.getRecipient());
        if (target != null) {
            target.sendMessage(msg);
            ClientHandler sender = clients.get(msg.getSender());
            if (sender != null) sender.sendMessage(msg);
            log("[PRIVADO] " + msg.getSender() + " → " + msg.getRecipient() + ": " + msg.getContent());
        } else {
            ClientHandler sender = clients.get(msg.getSender());
            if (sender != null) {
                sender.sendMessage(new Message(Message.Type.SERVER_INFO, "SERVIDOR",
                        "Usuário '" + msg.getRecipient() + "' não encontrado."));
            }
        }
    }

    public static void sendFile(Message msg) {
        // Broadcast file to all or send privately
        if (msg.getRecipient() != null && !msg.getRecipient().isEmpty()) {
            ClientHandler target = clients.get(msg.getRecipient());
            if (target != null) {
                target.sendMessage(msg);
                log("[ARQUIVO] " + msg.getSender() + " → " + msg.getRecipient() + ": " + msg.getFileName());
            }
        } else {
            broadcast(msg, msg.getSender());
            log("[ARQUIVO BROADCAST] " + msg.getSender() + ": " + msg.getFileName());
        }
    }

    /** Envia a lista de usuários online para todos os clientes conectados */
    public static void broadcastUserList() {
        String userList = String.join(",", clients.keySet());
        Message listMsg = new Message(Message.Type.SERVER_INFO, "SERVIDOR", "USERLIST:" + userList);
        for (ClientHandler ch : clients.values()) {
            ch.sendMessage(listMsg);
        }
    }

    private static void updateClientList() {
        if (gui != null) {
            gui.updateClientList(new ArrayList<>(clients.keySet()));
        }
    }

    public static List<String> getOnlineUsers() {
        return new ArrayList<>(clients.keySet());
    }

    public static void log(String message) {
        System.out.println("[SERVER] " + message);
        if (gui != null) gui.log(message);
    }
}
