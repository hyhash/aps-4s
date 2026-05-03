package server;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private JList<String> clientList;
    private DefaultListModel<String> listModel;
    private JLabel statusLabel;
    private JLabel countLabel;

    public ServerGUI() {
        setTitle("🌊 Servidor - Sistema de Monitoramento Rio Tietê | UNIP");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 100, 60));
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("🌿 Secretaria do Meio Ambiente – Central de Monitoramento");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        statusLabel = new JLabel("● SERVIDOR ATIVO  |  Porta: 12345");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(150, 255, 150));

        header.add(title, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(15, 25, 20));
        logArea.setForeground(new Color(180, 255, 180));
        logArea.setCaretColor(Color.WHITE);
        logArea.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 100, 60)),
                " Log do Servidor ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(0, 100, 60)));

        // Client list panel
        listModel = new DefaultListModel<>();
        clientList = new JList<>(listModel);
        clientList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clientList.setBackground(new Color(235, 248, 240));
        clientList.setSelectionBackground(new Color(0, 150, 80));
        clientList.setSelectionForeground(Color.WHITE);
        clientList.setCellRenderer(new InspectorRenderer());

        JScrollPane listScroll = new JScrollPane(clientList);

        countLabel = new JLabel("Inspetores online: 0");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLabel.setForeground(new Color(0, 100, 60));
        countLabel.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 100, 60)),
                " Inspetores Conectados ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(0, 100, 60)));
        rightPanel.setPreferredSize(new Dimension(220, 0));
        rightPanel.add(listScroll, BorderLayout.CENTER);
        rightPanel.add(countLabel, BorderLayout.SOUTH);

        // Center split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logScroll, rightPanel);
        split.setDividerLocation(560);
        split.setDividerSize(5);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setBackground(new Color(230, 245, 235));
        footer.setBorder(new EmptyBorder(4, 8, 4, 8));
        JLabel info = new JLabel("Sistema de Monitoramento do Rio Tietê – APS 2025 | UNIP Ciência da Computação");
        info.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        info.setForeground(Color.GRAY);
        footer.add(info);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        log("=== Sistema de Monitoramento do Rio Tietê iniciado ===");
        log("Aguardando conexão dos inspetores de campo...");
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + time + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void updateClientList(List<String> names) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (String name : names) listModel.addElement(name);
            countLabel.setText("Inspetores online: " + names.size());
        });
    }

    // Custom renderer for inspector list
    private static class InspectorRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, "🔎 " + value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(5, 8, 5, 8));
            return label;
        }
    }
}
