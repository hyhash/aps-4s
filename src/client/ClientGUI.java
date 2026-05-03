package client;

import common.Message;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class ClientGUI extends JFrame {

    // ── UI Components ──────────────────────────────────────────────────────────
    private ChatClient client;
    private JTextPane chatArea;
    private StyledDocument chatDoc;
    private JTextField inputField;
    private JButton sendBtn, fileBtn, emojiBtn;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JLabel statusLabel, ngrokLabel;
    private JComboBox<String> recipientCombo;

    // Pending file transfers: fileName -> fileData (aguardando download)
    private final Map<String, byte[]> pendingFiles = new LinkedHashMap<>();

    private static final String BROADCAST = "Todos";

    // ── Palette ────────────────────────────────────────────────────────────────
    private static final Color C_GREEN    = new Color(0, 110, 60);
    private static final Color C_GREEN2   = new Color(0, 160, 85);
    private static final Color C_BG       = new Color(243, 249, 245);
    private static final Color C_WHITE    = Color.WHITE;
    private static final Color C_BORDER   = new Color(200, 225, 210);
    private static final Color C_BUBBLE_ME    = new Color(210, 245, 225);
    private static final Color C_BUBBLE_OTHER = new Color(255, 255, 255);
    private static final Color C_BUBBLE_SYS   = new Color(230, 238, 255);
    private static final Color C_FILE_BTN     = new Color(230, 245, 255);

    // ══════════════════════════════════════════════════════════════════════════
    //  CONSTRUTOR
    // ══════════════════════════════════════════════════════════════════════════
    public ClientGUI() {
        setTitle("Sistema de Monitoramento - Rio Tietê");
        setSize(980, 680);
        setMinimumSize(new Dimension(800, 560));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { exitApp(); }
        });
        showLoginDialog();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DIALOG DE LOGIN
    // ══════════════════════════════════════════════════════════════════════════
    private void showLoginDialog() {
        // JFrame em vez de JDialog para aparecer na barra de tarefas do Windows
        JFrame dlg = new JFrame("Sistema de Monitoramento - Rio Tietê");
        dlg.setSize(430, 380);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(null);
        dlg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dlg.setLayout(new BorderLayout());

        // --- Cabeçalho ---
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(C_GREEN);
        hdr.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel hTitle = new JLabel("Sistema de Monitoramento do Rio Tietê");
        hTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        hTitle.setForeground(C_WHITE);
        JLabel hSub = new JLabel("UNIP - Ciência da Computação - APS 2025");
        hSub.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        hSub.setForeground(new Color(190, 235, 210));
        JPanel hText = new JPanel(new GridLayout(2,1,0,2));
        hText.setOpaque(false);
        hText.add(hTitle);
        hText.add(hSub);
        hdr.add(hText, BorderLayout.CENTER);

        // --- Formulário ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(C_BG);
        form.setBorder(new EmptyBorder(20, 28, 8, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 6, 7, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JTextField hostField = styledField("localhost");
        JTextField portField = styledField("12345");
        JTextField nameField = styledField("");

        addRow(form, g, 0, "Servidor (IP / ngrok):", hostField);
        addRow(form, g, 1, "Porta:",                  portField);
        addRow(form, g, 2, "Seu nome (inspetor):",    nameField);

        // Dica ngrok
        JLabel ngrokHint = new JLabel(
            "<html><font color='#555555'>Para acesso externo via ngrok: use o host e porta fornecidos pelo ngrok</font></html>");
        ngrokHint.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 11));
        g.gridx=0; g.gridy=3; g.gridwidth=2;
        form.add(ngrokHint, g);
        g.gridwidth=1;

        // --- Botão ---
        JButton connectBtn = new JButton("Conectar");
        connectBtn.setBackground(C_GREEN);
        connectBtn.setForeground(C_WHITE);
        connectBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        connectBtn.setFocusPainted(false);
        connectBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        connectBtn.setBorder(new EmptyBorder(10, 30, 10, 30));
        connectBtn.setOpaque(true);

        // Hover effect
        connectBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { connectBtn.setBackground(C_GREEN2); }
            public void mouseExited(MouseEvent e)  { connectBtn.setBackground(C_GREEN);  }
        });

        JPanel btnPnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        btnPnl.setBackground(C_BG);
        btnPnl.add(connectBtn);

        dlg.add(hdr,    BorderLayout.NORTH);
        dlg.add(form,   BorderLayout.CENTER);
        dlg.add(btnPnl, BorderLayout.SOUTH);

        ActionListener doConnect = e -> {
            String host = hostField.getText().trim();
            String portStr = portField.getText().trim();
            String name  = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Por favor, informe seu nome de inspetor.", "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int port;
            try { port = Integer.parseInt(portStr); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Porta inválida. Use apenas números (ex: 12345).", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            connectBtn.setEnabled(false);
            connectBtn.setText("Conectando...");
            client = new ChatClient(host, port, name, this);
            if (client.connect()) {
                dlg.dispose();
                setTitle("Rio Tietê - Inspetor: " + name);
                initUI();
                setVisible(true);
            } else {
                connectBtn.setEnabled(true);
                connectBtn.setText("Conectar");
            }
        };

        connectBtn.addActionListener(doConnect);
        nameField.addActionListener(doConnect);
        portField.addActionListener(doConnect);

        dlg.setVisible(true);
    }

    private JTextField styledField(String text) {
        JTextField f = new JTextField(text);
        f.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            new EmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JTextField field) {
        g.gridx=0; g.gridy=row; g.weightx=0.38;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        p.add(lbl, g);
        g.gridx=1; g.weightx=0.62;
        p.add(field, g);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  JANELA PRINCIPAL DO CHAT
    // ══════════════════════════════════════════════════════════════════════════
    private void initUI() {
        setLayout(new BorderLayout());

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildCenter(),     BorderLayout.CENTER);
        add(buildInputPanel(), BorderLayout.SOUTH);
    }

    // ── Header ─────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(C_GREEN);
        hdr.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel title = new JLabel("Secretaria do Meio Ambiente – Monitoramento Rio Tietê");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        title.setForeground(C_WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        statusLabel = new JLabel("● Conectado");
        statusLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        statusLabel.setForeground(new Color(150, 255, 150));

        right.add(statusLabel);
        hdr.add(title,  BorderLayout.WEST);
        hdr.add(right,  BorderLayout.EAST);
        return hdr;
    }

    // ── Centro: chat + lista de usuários ───────────────────────────────────────
    private JSplitPane buildCenter() {
        // Chat
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(C_BG);
        chatArea.setMargin(new Insets(10, 12, 10, 12));
        chatDoc = chatArea.getStyledDocument();
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(null);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);

        // Painel de usuários
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        userList.setBackground(new Color(235, 248, 240));
        userList.setSelectionBackground(C_GREEN2);
        userList.setSelectionForeground(C_WHITE);
        userList.setCellRenderer(new UserCellRenderer());
        userList.setFixedCellHeight(36);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(null);

        JLabel usersTitle = new JLabel("  Inspetores Online");
        usersTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        usersTitle.setForeground(C_GREEN);
        usersTitle.setBorder(new EmptyBorder(10, 8, 10, 8));
        usersTitle.setOpaque(true);
        usersTitle.setBackground(new Color(220, 242, 230));

        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setPreferredSize(new Dimension(190, 0));
        usersPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, C_BORDER));
        usersPanel.add(usersTitle, BorderLayout.NORTH);
        usersPanel.add(userScroll, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScroll, usersPanel);
        split.setDividerSize(1);
        split.setDividerLocation(770);
        split.setResizeWeight(1.0);
        return split;
    }

    // ── Painel de entrada ───────────────────────────────────────────────────────
    private JPanel buildInputPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(C_WHITE);
        outer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));

        // ---- Barra superior: destinatário + botões de ação ----
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        topBar.setBackground(new Color(248, 252, 250));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));

        // Destinatário
        JLabel toLabel = new JLabel("Para:");
        toLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        toLabel.setForeground(new Color(80, 80, 80));

        recipientCombo = new JComboBox<>();
        recipientCombo.addItem(BROADCAST);
        recipientCombo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        recipientCombo.setPreferredSize(new Dimension(150, 28));
        recipientCombo.setToolTipText("Selecione o destinatário");

        // Separador visual
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 24));
        sep.setForeground(C_BORDER);

        // Botão Emoji
        emojiBtn = buildToolButton("Emoji  \uD83D\uDE00", "Inserir emoji na mensagem", new Color(255, 248, 220));
        emojiBtn.addActionListener(e -> showEmojiPicker());

        // Botão Arquivo
        fileBtn = buildToolButton("Anexar Arquivo  \uD83D\uDCCE", "Enviar arquivo (pdf, imagem, etc.)", C_FILE_BTN);
        fileBtn.addActionListener(e -> sendFile());

        topBar.add(toLabel);
        topBar.add(recipientCombo);
        topBar.add(sep);
        topBar.add(emojiBtn);
        topBar.add(fileBtn);

        // ---- Barra inferior: campo de texto + enviar ----
        JPanel bottomBar = new JPanel(new BorderLayout(8, 0));
        bottomBar.setBackground(C_WHITE);
        bottomBar.setBorder(new EmptyBorder(8, 12, 8, 12));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            new EmptyBorder(7, 10, 7, 10)));
        inputField.addActionListener(e -> sendTextMessage());

        sendBtn = new JButton("Enviar");
        sendBtn.setBackground(C_GREEN);
        sendBtn.setForeground(C_WHITE);
        sendBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        sendBtn.setFocusPainted(false);
        sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendBtn.setBorder(new EmptyBorder(9, 22, 9, 22));
        sendBtn.setOpaque(true);
        sendBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { sendBtn.setBackground(C_GREEN2); }
            public void mouseExited(MouseEvent e)  { sendBtn.setBackground(C_GREEN);  }
        });
        sendBtn.addActionListener(e -> sendTextMessage());

        bottomBar.add(inputField, BorderLayout.CENTER);
        bottomBar.add(sendBtn,    BorderLayout.EAST);

        outer.add(topBar,    BorderLayout.NORTH);
        outer.add(bottomBar, BorderLayout.SOUTH);
        return outer;
    }

    /** Cria botão de ferramenta (emoji / arquivo) com label clara e estável */
    private JButton buildToolButton(String label, String tooltip, Color bg) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btn.setToolTipText(tooltip);
        btn.setBackground(bg);
        btn.setForeground(new Color(50, 50, 50));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            new EmptyBorder(5, 10, 5, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        // Hover
        Color hoverBg = bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ENVIO DE MENSAGEM DE TEXTO
    // ══════════════════════════════════════════════════════════════════════════
    private void sendTextMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || !client.isConnected()) return;

        String recipient = (String) recipientCombo.getSelectedItem();
        Message msg;
        if (recipient == null || recipient.equals(BROADCAST)) {
            msg = new Message(Message.Type.TEXT, client.getUsername(), text);
        } else {
            msg = new Message(Message.Type.PRIVATE, client.getUsername(), recipient, text);
        }
        client.sendMessage(msg);
        appendMessage(msg, true);
        inputField.setText("");
        inputField.requestFocus();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ENVIO DE ARQUIVO
    // ══════════════════════════════════════════════════════════════════════════
    private void sendFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar arquivo para enviar");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        long maxSize = 5L * 1024 * 1024; // 5 MB
        if (file.length() > maxSize) {
            JOptionPane.showMessageDialog(this,
                "Arquivo muito grande. Tamanho máximo: 5 MB.\nTamanho atual: " + (file.length()/1024) + " KB",
                "Arquivo grande demais", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            byte[] data = Files.readAllBytes(file.toPath());
            String recipient = (String) recipientCombo.getSelectedItem();
            String recip = (recipient == null || recipient.equals(BROADCAST)) ? null : recipient;

            // Usa construtor correto: privado ou broadcast
            Message msg = (recip != null)
                ? new Message(client.getUsername(), recip, file.getName(), data)
                : new Message(client.getUsername(), file.getName(), data);

            client.sendMessage(msg);
            appendFileMessage(client.getUsername(), file.getName(), data, true, recip);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao ler o arquivo: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SELETOR DE EMOJIS (sem bug de hover)
    // ══════════════════════════════════════════════════════════════════════════
    private void showEmojiPicker() {
        String[][] emojis = {
            {"😊", "😂", "😢", "😡", "👍", "👎", "❤️", "⚠️"},
            {"🌊", "🌿", "🐟", "🐊", "💧", "🏭", "🔎", "📋"},
            {"✅", "❌", "📎", "📸", "🔔", "📡", "🗓️", "📝"}
        };

        JDialog picker = new JDialog(this, false); // não-modal para não travar
        picker.setUndecorated(true);
        picker.setBackground(new Color(0, 0, 0, 0));

        JPanel bg = new JPanel(new BorderLayout());
        bg.setBackground(C_WHITE);
        bg.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1),
            new EmptyBorder(8, 8, 8, 8)));

        JLabel title = new JLabel("  Selecione um emoji:");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        title.setForeground(new Color(80, 80, 80));
        title.setBorder(new EmptyBorder(0, 0, 6, 0));

        JPanel grid = new JPanel(new GridLayout(3, 8, 4, 4));
        grid.setBackground(C_WHITE);

        for (String[] row : emojis) {
            for (String emoji : row) {
                JButton btn = new JButton(emoji);
                btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                btn.setPreferredSize(new Dimension(40, 38));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(true);
                btn.setBackground(C_WHITE);
                btn.setOpaque(true);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.setToolTipText(emoji);

                btn.setBackground(new Color(230, 248, 238));
                btn.setBorderPainted(true);
                btn.setBorder(BorderFactory.createLineBorder(C_GREEN2, 1));

                btn.setBackground(C_WHITE);
                btn.setBorderPainted(false);

                // Hover estável sem glitch
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        btn.setBackground(new Color(230, 248, 238));
                        btn.setBorderPainted(true);
                        btn.setBorder(BorderFactory.createLineBorder(C_GREEN2, 1));
                    }
                    public void mouseExited(MouseEvent e) {
                        btn.setBackground(C_WHITE);
                        btn.setBorderPainted(false);
                    }
                });

                btn.addActionListener(ev -> {
                    inputField.setText(inputField.getText() + emoji);
                    picker.dispose();
                    inputField.requestFocus();
                });
                grid.add(btn);
            }
        }

        bg.add(title, BorderLayout.NORTH);
        bg.add(grid,  BorderLayout.CENTER);
        picker.add(bg);
        picker.pack();

        // Posiciona acima do botão emoji
        Point p = emojiBtn.getLocationOnScreen();
        picker.setLocation(p.x, p.y - picker.getPreferredSize().height - 5);

        // Fecha ao clicar fora
        picker.addWindowFocusListener(new WindowAdapter() {
            public void windowLostFocus(WindowEvent e) { picker.dispose(); }
        });

        picker.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RECEBIMENTO DE MENSAGENS
    // ══════════════════════════════════════════════════════════════════════════
    public void receiveMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case TEXT:
                case PRIVATE:
                    appendMessage(msg, false);
                    break;
                case SERVER_INFO:
                    String content = msg.getContent();
                    if (content.startsWith("USERLIST:")) {
                        updateUserList(content.substring(9));
                    } else {
                        appendSystem(content);
                    }
                    break;
                case FILE:
                    appendFileMessage(msg.getSender(), msg.getFileName(),
                        msg.getFileData(), false, null);
                    break;
                case JOIN:
                    appendSystem("Inspetor [" + msg.getSender() + "] entrou no sistema.");
                    break;
                case LEAVE:
                    appendSystem("Inspetor [" + msg.getSender() + "] saiu do sistema.");
                    break;
                default:
                    break;
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RENDERIZAÇÃO DO CHAT
    // ══════════════════════════════════════════════════════════════════════════

    /** Mensagem de texto comum ou privada */
    private void appendMessage(Message msg, boolean isMine) {
        boolean isPrivate = msg.getType() == Message.Type.PRIVATE;
        String dest = isPrivate
            ? (isMine ? " → " + msg.getRecipient() : " → você")
            : "";
        String headerTxt = "[" + msg.getTimestamp() + "]  "
            + (isPrivate ? "[PRIVADO] " : "")
            + msg.getSender() + dest;

        Color headerColor = isMine
            ? new Color(0, 120, 60)
            : (isPrivate ? new Color(90, 60, 160) : new Color(60, 60, 60));

        appendStyled(headerTxt + "\n", headerColor, true,  12);
        appendStyled(msg.getContent() + "\n\n",
            new Color(30, 30, 30), false, 14);
    }

    /** Mensagem de arquivo com botão de download embutido no chat */
    private void appendFileMessage(String sender, String fileName,
                                   byte[] data, boolean isMine, String dest) {
        try {
            // Cabeçalho
            String label = isMine
                ? "[" + nowTime() + "]  " + sender
                    + (dest != null ? " → " + dest : " → Todos")
                    + "  enviou o arquivo:"
                : "[" + nowTime() + "]  " + sender + "  enviou o arquivo:";

            appendStyled(label + "\n", new Color(0, 100, 55), true, 12);

            // Botão de download inline no JTextPane
            JPanel fileCard = buildFileCard(sender, fileName, data, isMine);

            // Insere componente no documento
            chatArea.setCaretPosition(chatDoc.getLength());
            chatArea.insertComponent(fileCard);

            appendStyled("\n\n", Color.BLACK, false, 4);
            chatArea.setCaretPosition(chatDoc.getLength());

        } catch (Exception ex) {
            appendSystem("Arquivo recebido: " + fileName);
        }
    }

    /** Card visual com nome do arquivo + botão Baixar */
    private JPanel buildFileCard(String sender, String fileName, byte[] data, boolean isMine) {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        card.setBackground(isMine ? new Color(220, 248, 232) : new Color(240, 248, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isMine ? new Color(160, 220, 190) : new Color(170, 200, 240), 1),
            new EmptyBorder(4, 8, 4, 8)));
        card.setMaximumSize(new Dimension(420, 44));

        // Ícone por extensão
        String ext = fileName.contains(".")
            ? fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase() : "?";
        String icon = switch (ext) {
            case "pdf"  -> "📄";
            case "png", "jpg", "jpeg", "gif", "bmp" -> "🖼";
            case "zip", "rar", "7z" -> "🗜";
            case "doc", "docx" -> "📝";
            case "xls", "xlsx" -> "📊";
            case "mp4", "avi", "mov" -> "🎬";
            default -> "📎";
        };

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JLabel nameLbl = new JLabel(fileName + "  (" + formatSize(data.length) + ")");
        nameLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        nameLbl.setForeground(new Color(30, 30, 80));

        JButton downloadBtn = new JButton("Baixar");
        downloadBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        downloadBtn.setBackground(C_GREEN);
        downloadBtn.setForeground(C_WHITE);
        downloadBtn.setFocusPainted(false);
        downloadBtn.setBorder(new EmptyBorder(4, 12, 4, 12));
        downloadBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        downloadBtn.setOpaque(true);

        downloadBtn.addActionListener(e -> saveFile(fileName, data, downloadBtn));

        card.add(iconLbl);
        card.add(nameLbl);
        card.add(downloadBtn);
        return card;
    }

    /** Salva o arquivo no disco a partir do botão Baixar */
    private void saveFile(String fileName, byte[] data, JButton btn) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar arquivo");
        chooser.setSelectedFile(new File(fileName));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(chooser.getSelectedFile().toPath(), data);
                btn.setText("Salvo ✓");
                btn.setBackground(new Color(80, 160, 80));
                btn.setEnabled(false);
                appendSystem("Arquivo salvo: " + chooser.getSelectedFile().getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Mensagem de sistema (entrada, saída, avisos) */
    private void appendSystem(String text) {
        appendStyled("  " + text + "\n", new Color(90, 120, 180), false, 12);
    }

    /** Método auxiliar de estilo */
    private void appendStyled(String text, Color color, boolean bold, int size) {
        try {
            Style s = chatArea.addStyle(UUID.randomUUID().toString(), null);
            StyleConstants.setForeground(s, color);
            StyleConstants.setBold(s, bold);
            StyleConstants.setFontSize(s, size);
            StyleConstants.setFontFamily(s, "Segoe UI Emoji");
            chatDoc.insertString(chatDoc.getLength(), text, s);
            chatArea.setCaretPosition(chatDoc.getLength());
        } catch (BadLocationException ignored) {}
    }

    private String nowTime() {
        return new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024*1024) return (bytes/1024) + " KB";
        return String.format("%.1f MB", bytes/(1024.0*1024));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LISTA DE USUÁRIOS
    // ══════════════════════════════════════════════════════════════════════════
    private void updateUserList(String csv) {
        String[] users = csv.split(",");
        userListModel.clear();
        recipientCombo.removeAllItems();
        recipientCombo.addItem(BROADCAST);
        for (String u : users) {
            u = u.trim();
            if (!u.isEmpty()) {
                userListModel.addElement(u);
                if (!u.equals(client.getUsername())) {
                    recipientCombo.addItem(u);
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CALLBACKS DE ESTADO
    // ══════════════════════════════════════════════════════════════════════════
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("● Desconectado");
            statusLabel.setForeground(Color.RED);
            appendSystem("Conexão com o servidor encerrada.");
        });
    }

    public void showError(String msg) {
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, msg, "Erro de Conexão", JOptionPane.ERROR_MESSAGE));
    }

    private void exitApp() {
        int r = JOptionPane.showConfirmDialog(this,
            "Deseja sair do sistema de monitoramento?",
            "Confirmar saída", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            if (client != null) client.disconnect();
            dispose();
            System.exit(0);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RENDERER DA LISTA DE USUÁRIOS
    // ══════════════════════════════════════════════════════════════════════════
    private static class UserCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                list, "  " + value, index, isSelected, cellHasFocus);
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 235, 220)),
                new EmptyBorder(6, 10, 6, 10)));
            if (!isSelected) lbl.setBackground(new Color(235, 248, 240));

            // Indicador verde à esquerda
            lbl.setIcon(new Icon() {
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    g.setColor(new Color(0, 180, 80));
                    g.fillOval(x, y + 4, 9, 9);
                }
                public int getIconWidth()  { return 14; }
                public int getIconHeight() { return 18; }
            });
            return lbl;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MAIN
    // ══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
