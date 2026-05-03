package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        TEXT, JOIN, LEAVE, FILE, PRIVATE, SERVER_INFO
    }

    private Type type;
    private String sender;
    private String recipient; // null = broadcast
    private String content;
    private byte[] fileData;
    private String fileName;
    private String timestamp;

    // Mensagem de texto
    public Message(Type type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // Mensagem de texto com destinatário
    public Message(Type type, String sender, String recipient, String content) {
        this(type, sender, content);
        this.recipient = recipient;
    }

    // Arquivo broadcast
    public Message(String sender, String fileName, byte[] fileData) {
        this.type = Type.FILE;
        this.sender = sender;
        this.fileName = fileName;
        this.fileData = fileData;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // Arquivo privado
    public Message(String sender, String recipient, String fileName, byte[] fileData) {
        this(sender, fileName, fileData);
        this.recipient = recipient;
    }

    public Type getType()       { return type; }
    public String getSender()   { return sender; }
    public String getRecipient(){ return recipient; }
    public String getContent()  { return content; }
    public byte[] getFileData() { return fileData; }
    public String getFileName() { return fileName; }
    public String getTimestamp(){ return timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + sender + ": " + content;
    }
}
