package net.sf.sockettest.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;

import net.sf.sockettest.Util;
import net.sf.sockettest.module.SocketServer;

public class SocketTestProxy extends JPanel {
    private final String NEW_LINE = "\r\n";
    private ClassLoader cl = getClass().getClassLoader();
    public ImageIcon logo = new ImageIcon(cl.getResource("icons/logo.gif"));

    private JPanel topPanel;
    private JPanel toPanel;

    private JPanel centerPanel;
    private JPanel textPanel;
    private JPanel buttonPanel;
    private JPanel sendPanel;

    private JLabel ipLabel = new JLabel("IP Address");
    private JLabel portLabel = new JLabel("Port");
    private JLabel logoLabel = new JLabel("SocketTest v 3.0", logo, JLabel.CENTER);
    private JTextField ipField = new JTextField("0.0.0.0", 20);
    private JTextField portField = new JTextField("21", 10);
    private JButton portButton = new JButton("Port");
    private JButton connectButton = new JButton("Start Listening");

    private JLabel convLabel = new JLabel("Conversation with Client");
    private Border connectedBorder = BorderFactory.createTitledBorder(new EtchedBorder(),
            "Connected Client : < NONE >");
    private JTextArea messagesField = new JTextArea();

    private JLabel sendLabel = new JLabel("Message");
    private JTextField sendField = new JTextField();

    private JButton sendButton = new JButton("Send");
    private JButton disconnectButton = new JButton("Disconnect");
    private JButton saveButton = new JButton("Save");
    private JButton clearButton = new JButton("Clear");

    private GridBagConstraints gbc = new GridBagConstraints();

    private Socket socket;
    private ServerSocket server;
    private SocketServer socketServer;
    private PrintWriter out;

    private JCheckBox sslButton = new JCheckBox("SSL");
    private boolean isSSL = false;

    protected final JFrame parent;

    //client
    //private JPanel topClientPanel;
    private JPanel toClientPanel;

    private JLabel ipClientLabel = new JLabel("IP Address");
    private JLabel portClientLabel = new JLabel("Port");
    private JLabel logoClientLabel = new JLabel("SocketTest v 3.0", logo, JLabel.CENTER);
    private JTextField ipClientField = new JTextField("127.0.0.1", 20);
    private JTextField portClientField = new JTextField("21", 10);
    private JButton portClientButton = new JButton("Port");
    private JButton connectClientButton = new JButton("Connect");

    private JCheckBox secureButton = new JCheckBox("Secure");
    private boolean isSecure = false;

    /**
     * Create the panel.
     */
    public SocketTestProxy(final JFrame parent) {
        //Container cp = getContentPane();
        this.parent = parent;
        Container cp = this;

        topPanel = new JPanel();
        toPanel = new JPanel();
        toPanel.setLayout(new GridBagLayout());
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        toPanel.add(ipLabel, gbc);

        gbc.weightx = 1.0; //streach
        gbc.gridx = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        ActionListener ipListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                portField.requestFocus();
            }
        };
        ipField.addActionListener(ipListener);
        toPanel.add(ipField, gbc);

        gbc.weightx = 0.0;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        toPanel.add(portLabel, gbc);

        gbc.weightx = 1.0;
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        ActionListener connectListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //connect();
            }
        };
        portField.addActionListener(connectListener);
        toPanel.add(portField, gbc);

        gbc.weightx = 0.0;
        gbc.gridy = 1;
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        portButton.setMnemonic('P');
        portButton.setToolTipText("View Standard Ports");
        ActionListener portButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PortDialog dia = new PortDialog(parent, PortDialog.TCP);
                dia.show();
            }
        };
        portButton.addActionListener(portButtonListener);
        toPanel.add(portButton, gbc);

        gbc.weightx = 0.0;
        gbc.gridy = 1;
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        connectButton.setMnemonic('S');
        connectButton.setToolTipText("Start Listening");
        connectButton.addActionListener(connectListener);
        toPanel.add(connectButton, gbc);

        gbc.weightx = 0.0;
        gbc.gridy = 1;
        gbc.gridx = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        sslButton.setToolTipText("Set Has Alert");
        sslButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                isSSL = !isSSL;
            }
        });
        toPanel.add(sslButton, gbc);

        toPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), "Listen On"));
        topPanel.setLayout(new BorderLayout(10, 0));
        topPanel.add(toPanel, BorderLayout.CENTER);
        logoLabel.setVerticalTextPosition(JLabel.BOTTOM);
        logoLabel.setHorizontalTextPosition(JLabel.CENTER);
        topPanel.add(logoLabel, BorderLayout.EAST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        //client
        //topClientPanel = new JPanel();
        toClientPanel = new JPanel();
        toClientPanel.setLayout(new GridBagLayout());
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        toClientPanel.add(ipClientLabel, gbc);

        gbc.weightx = 1.0; //streach
        gbc.gridx = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        ActionListener ipClientListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                portClientField.requestFocus();
            }
        };
        ipClientField.addActionListener(ipListener);
        toClientPanel.add(ipClientField, gbc);

        gbc.weightx = 0.0;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        toClientPanel.add(portClientLabel, gbc);

        gbc.weightx = 1.0;
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        ActionListener connectClientListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //connect();
            }
        };
        portClientField.addActionListener(connectClientListener);
        toClientPanel.add(portClientField, gbc);

        gbc.weightx = 0.0;
        gbc.gridy = 1;
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        portClientButton.setMnemonic('P');
        portClientButton.setToolTipText("View Standard Ports");
        ActionListener portClientButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PortDialog dia = new PortDialog(parent, PortDialog.TCP);
                dia.show();
            }
        };
        portClientButton.addActionListener(portClientButtonListener);
        toClientPanel.add(portClientButton, gbc);

        gbc.weightx = 0.0;
        gbc.gridy = 1;
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        connectClientButton.setMnemonic('C');
        connectClientButton.setToolTipText("Start Connection");
        connectClientButton.addActionListener(connectListener);
        toClientPanel.add(connectClientButton, gbc);

        gbc.weightx = 0.0;
        gbc.gridy = 1;
        gbc.gridx = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        secureButton.setToolTipText("Set Has Secure");
        secureButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                isSecure = !isSecure;
            }
        });
        toClientPanel.add(secureButton, gbc);

        toClientPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), "Connect To"));
        topPanel.add(toClientPanel, BorderLayout.SOUTH);
        //logoClientLabel.setVerticalTextPosition(JLabel.BOTTOM);
        //logoClientLabel.setHorizontalTextPosition(JLabel.CENTER);
        //topPanel.add(logoClientLabel, BorderLayout.EAST);
        //topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        //text
        textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout(0, 5));
        textPanel.add(convLabel, BorderLayout.NORTH);
        messagesField.setEditable(false);
        JScrollPane jsp = new JScrollPane(messagesField);
        textPanel.add(jsp);
        textPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));

        sendPanel = new JPanel();
        sendPanel.setLayout(new GridBagLayout());
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        sendPanel.add(sendLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        sendField.setEditable(false);
        sendPanel.add(sendField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        sendButton.setEnabled(false);
        sendButton.setToolTipText("Send text to client");
        ActionListener sendListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String msg = sendField.getText();
                if (!msg.equals("")) {
                    //sendMessage(msg);
                } else {
                    int value = JOptionPane.showConfirmDialog(SocketTestProxy.this, "Send Blank Line ?",
                            "Send Data To Client", JOptionPane.YES_NO_OPTION);
                    if (value == JOptionPane.YES_OPTION) {
                        //sendMessage(msg);
                    }
                }
            }
        };
        sendButton.addActionListener(sendListener);
        sendField.addActionListener(sendListener);
        sendPanel.add(sendButton, gbc);
        ActionListener disconnectListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //disconnect();
            }
        };
        gbc.gridx = 3;
        disconnectButton.addActionListener(disconnectListener);
        disconnectButton.setEnabled(false);
        sendPanel.add(disconnectButton, gbc);

        sendPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3),
                BorderFactory.createTitledBorder("Send")));

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        gbc.weighty = 0.0;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.BOTH;
        buttonPanel.add(sendPanel, gbc);
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        gbc.gridx = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        saveButton.setToolTipText("Save conversation with client to a file");
        saveButton.setMnemonic('S');
        ActionListener saveListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = messagesField.getText();
                if (text.equals("")) {
                    //error("Nothing to save", "Save to file");
                    return;
                }
                String fileName = "";
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File("."));
                int returnVal = chooser.showSaveDialog(SocketTestProxy.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fileName = chooser.getSelectedFile()
                                      .getAbsolutePath();
                    try {
                        Util.writeFile(fileName, text);
                    } catch (Exception ioe) {
                        JOptionPane.showMessageDialog(SocketTestProxy.this, "" + ioe.getMessage(),
                                "Error saving to file..", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        saveButton.addActionListener(saveListener);
        buttonPanel.add(saveButton, gbc);
        gbc.gridy = 1;
        clearButton.setToolTipText("Clear conversation with client");
        clearButton.setMnemonic('C');
        ActionListener clearListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                messagesField.setText("");
            }
        };
        clearButton.addActionListener(clearListener);
        buttonPanel.add(clearButton, gbc);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 3));

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout(0, 10));
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        centerPanel.add(textPanel, BorderLayout.CENTER);

        CompoundBorder cb = new CompoundBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10), connectedBorder);
        centerPanel.setBorder(cb);

        cp.setLayout(new BorderLayout(10, 0));
        cp.add(topPanel, BorderLayout.NORTH);
        //cp.add(topClientPanel, BorderLayout.CENTER);
        cp.add(centerPanel, BorderLayout.CENTER);
    }

}
