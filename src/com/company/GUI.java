package com.company;

import network.*;
import org.apache.log4j.Logger;
import org.nustaq.serialization.FSTConfiguration;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GUI {
    static Logger logger = Logger.getLogger(GUI.class.getName());
    public Node node;
    public String bestPeer = null;
    private String totalRW = "";
    private Peers peers = new Peers();
    private JFrame frame = new JFrame();
    private JLabel msg;
    private int connectionCount = 0;
    private Blockchain blockchain = new Blockchain(0, true, "1.0.0.0");
    public static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    private DefaultTableModel model;
    private JPanel statusBar = new JPanel();
    public Boolean blockLoadingInProgress = false;

    public GUI() {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Long width = Math.round(screenSize.getWidth() / 2);
        Long height = Math.round(screenSize.getHeight() / 2);
        frame.setSize(width.intValue(), height.intValue());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowListener() {
            @Override public void windowClosing(WindowEvent e) {
                if (node != null) {
                    node.leave();
                }
            }
            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {}
        });

        msg = new JLabel("", JLabel.LEFT);
        msg.setForeground(Color.black);
        msg.setToolTipText("Tool Tip Here");

        statusBar.setLayout(new BorderLayout());//frame layout

        //Creating the StatusBar.
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setBackground(Color.lightGray);
        statusBar.add(msg, BorderLayout.WEST);

        frame.add("South", statusBar);

        JPanel PeersInfoPanel = new JPanel();
        PeersInfoPanel.setLayout(new GridLayout(11,1));

        JLabel PeerId = new JLabel("", JLabel.LEFT);
        JLabel PeerAddress = new JLabel("", JLabel.LEFT);
        JLabel PeerHeight = new JLabel("", JLabel.LEFT);
        JLabel PeerVersion = new JLabel("", JLabel.LEFT);
        JLabel PeerNODEPORT = new JLabel("", JLabel.LEFT);
        JLabel PeerFullNode = new JLabel("", JLabel.LEFT);
        JLabel PeerRead = new JLabel("", JLabel.LEFT);
        JLabel PeerWrite = new JLabel("", JLabel.LEFT);
        JLabel PeerSpeed = new JLabel("", JLabel.LEFT);
        JLabel PeerPing = new JLabel("", JLabel.LEFT);
        JLabel PeerIN = new JLabel("", JLabel.LEFT);

        PeersInfoPanel.add(PeerId);
        PeersInfoPanel.add(PeerAddress);
        PeersInfoPanel.add(PeerHeight);
        PeersInfoPanel.add(PeerVersion);
        PeersInfoPanel.add(PeerNODEPORT);
        PeersInfoPanel.add(PeerFullNode);
        PeersInfoPanel.add(PeerRead);
        PeersInfoPanel.add(PeerWrite);
        PeersInfoPanel.add(PeerSpeed);
        PeersInfoPanel.add(PeerPing);
        PeersInfoPanel.add(PeerIN);

        //Массив содержащий заголоки таблицы
        Object[] headers = { "ID", "IP", "HEIGHT" , "VERSION", "NODE PORT", "FULL NODE", "READ", "WRITE", "SPEED", "PING", "DIRECTION"};

        //Массив содержащий информацию для таблицы
        Object[][] data = { };

        model = new DefaultTableModel(data, headers)  {
            public boolean isCellEditable(int row, int column)
            {
                return false;//This causes all cells to be not editable
            }
        };
        //Объект таблицы
        JTable jTabPeers;

        //Создаем новую таблицу на основе двумерного массива данных и заголовков
        jTabPeers = new JTable(model);
        JScrollPane scrollPeers = new JScrollPane(jTabPeers);
        jTabPeers.getSelectionModel().addListSelectionListener(e -> {
            int sel = jTabPeers.getSelectedRow();
            if (sel != -1) {
                PeerId.setText("ID:" + jTabPeers.getModel().getValueAt(sel, 0).toString());
                PeerAddress.setText("IP ADDRESS:" + jTabPeers.getModel().getValueAt(sel, 1).toString());
                PeerHeight.setText("HEIGHT:" + jTabPeers.getModel().getValueAt(sel, 2).toString());
                PeerVersion.setText("VERSION:" + jTabPeers.getModel().getValueAt(sel, 3).toString());
                PeerNODEPORT.setText("NODE PORT:" + jTabPeers.getModel().getValueAt(sel, 4).toString());
                PeerFullNode.setText("FULL NODE:" + jTabPeers.getModel().getValueAt(sel, 5).toString());
                PeerRead.setText("READ:" + jTabPeers.getModel().getValueAt(sel, 6).toString());
                PeerWrite.setText("WRITE:" + jTabPeers.getModel().getValueAt(sel, 7).toString());
                PeerSpeed.setText("SPEED:" + jTabPeers.getModel().getValueAt(sel, 8).toString());
                PeerPing.setText("PING:" + jTabPeers.getModel().getValueAt(sel, 9).toString());
                PeerIN.setText("DIRECTION:" + jTabPeers.getModel().getValueAt(sel, 10).toString());
            } else {
                PeerId.setText("ID:");
                PeerAddress.setText("IP ADDRESS:");
                PeerHeight.setText("HEIGHT:");
                PeerVersion.setText("VERSION:");
                PeerNODEPORT.setText("NODE PORT:");
                PeerFullNode.setText("FULL NODE:");
                PeerRead.setText("READ:");
                PeerWrite.setText("WRITE:");
                PeerSpeed.setText("SPEED:");
                PeerPing.setText("PING:");
                PeerIN.setText("DIRECTION:");
            }
        });

        JPanel PeersPanel = new JPanel();
        PeersPanel.setLayout(new GridLayout(1,2));
        PeersPanel.add(scrollPeers);
        PeersPanel.add(PeersInfoPanel);

        JTabbedPane tabPane;
        tabPane = new JTabbedPane();
        tabPane.addTab("Peers", PeersPanel);
        tabPane.setTabPlacement(SwingConstants.LEFT);

        frame.add(tabPane);
        frame.setVisible(true);

        peers.Init();

        this.node = new Node(6666, this, blockchain);
        peers.addPeer(new Peers.Peer(node.getAddress(),node.getPort(), new Date().getTime(), false));

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (node.getMaxConnections() > node.getNodeCount()){
                    NodeThread thread_tmp = node.getConnections().get(bestPeer);
                    if (thread_tmp == null) {
                        bestPeer = null;
                        blockLoadingInProgress = false;
                    }
                    for (Peers.Peer peer: peers.Peers.values()) {
                        if (!peer.GetBanned()) {
                            Boolean found = false;
                            for (NodeThread connection: node.getActiveNodes()) {
                                if (!connection.getNodeAddress().equals("") && connection.getNodePort() != 0) {
                                    peers.addPeer(peer);
                                    if (bestPeer == null) {
                                        bestPeer = connection.id;
                                    } else if (connection.getHeight() >= node.getConnections().get(bestPeer).getHeight()) {
                                        bestPeer = connection.id;
                                    }
                                    if (connection.getNodePort() == peer.GetPort() && connection.getNodeAddress().equals(peer.GetIp())){
                                        found = true;
                                        peer.SetLastActive(new Date().getTime());
                                    }
                                }
                            }
                            if (!found) {
                                if ((!node.getAddress().equals(peer.GetIp()) || node.getPort() != peer.GetPort()) && node.getMaxConnections() > node.getNodeCount()){
                                    if (node.connect(peer.GetIp(), peer.GetPort())) {
                                        peer.SetLastActive(new Date().getTime());
                                    } else {
                                        if (new Date().getTime() - peer.GetLastActive() >= 3600000 && !peer.GetBanned()){
                                            peer.SetBanned(true);
                                        }
                                    }
                                    found = false;
                                }
                            }
                        } else {
                            if (new Date().getTime() - peer.GetLastActive() >= 86400000) {
                                peer.SetBanned(false);
                                peer.SetLastActive(new Date().getTime());
                            }
                        }
                    }
                    NodeThread thread = node.getConnections().get(bestPeer);
                    if (thread == null) {
                        bestPeer = null;
                        blockLoadingInProgress = false;
                    }
                    if (!blockLoadingInProgress && bestPeer != null && node.getConnections().get(bestPeer).getVersion().equals(blockchain.version)) {
                        if (node.getConnections().get(bestPeer).getHeight() > blockchain.height) {
                            if (blockchain.height == 0) {
                                String hash = "";
                                node.generateMessage(bestPeer, conf.asByteArray(hash), "get_block");
                            }else {
                                String hash = "example hash";
                                node.generateMessage(bestPeer, conf.asByteArray(hash), "get_block");
                            }
                            blockLoadingInProgress = true;
                        }
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);


    }

    public void messageReceived(String id, Message message) {
        switch (message.type) {
            case "connected": {
                logger.info(message.type + " id:" + id);
                if (!id.equals("all")) {
                    AddPeer(id);
                }
                node.generateMessage(id, new PeerInfo(blockchain.version, blockchain.height, node.getPort(), node.getAddress(), blockchain.fullNode).getEncoded(), "get_node_info");
                node.generateMessage(id, (peers).getEncoded(), "get_peers");
                break;
            }
            case "err": {
                logger.error(message.type + " " + id);
                break;
            }
            case "get_node_info": {
                logger.info(message.type + " id:" + id);

                PeerInfo peerInfo = new PeerInfo(message.messageContent);

                NodeThread nodeThread = node.getConnections().get(id);
                nodeThread.setNodeAddress(peerInfo.nodeaddress);
                nodeThread.setNodePort(peerInfo.nodeport);
                nodeThread.setHeight(peerInfo.height);
                nodeThread.setVersion(peerInfo.version);
                nodeThread.setFullnode(peerInfo.fullNode);

                UpdatePeer(id, nodeThread.getNodeAddress(), nodeThread.getHeight(), nodeThread.getVersion(), nodeThread.getNodePort(), nodeThread.getFullNode(), nodeThread.getTotalread(), nodeThread.getTotalwrite(), nodeThread.getSpeed(), nodeThread.getPing(), nodeThread.getIN());

                node.generateMessage(id, new PeerInfo(blockchain.version, blockchain.height, node.getPort(), node.getAddress(), blockchain.fullNode).getEncoded(), "set_node_info");

                break;
            }
            case "set_node_info": {
                logger.info(message.type + " id:" + id);

                PeerInfo peerInfo = new PeerInfo(message.messageContent);

                NodeThread nodeThread = node.getConnections().get(id);
                nodeThread.setNodeAddress(peerInfo.nodeaddress);
                nodeThread.setNodePort(peerInfo.nodeport);
                nodeThread.setHeight(peerInfo.height);
                nodeThread.setVersion(peerInfo.version);
                nodeThread.setFullnode(peerInfo.fullNode);

                UpdatePeer(id, nodeThread.getNodeAddress(), nodeThread.getHeight(), nodeThread.getVersion(), nodeThread.getNodePort(), nodeThread.getFullNode(), nodeThread.getTotalread(), nodeThread.getTotalwrite(), nodeThread.getSpeed(), nodeThread.getPing(), nodeThread.getIN());

                break;
            }
            case "get_peers": {
                logger.info(message.type + " id:" + id);
                HashMap<String, Peers.Peer> tmp_peers = (HashMap<String, Peers.Peer>) conf.asObject(message.messageContent);
                boolean need_resend = false;
                for (Peers.Peer peer_in: tmp_peers.values()) {
                    if (peers.Peers.get(peer_in.GetIp() + ":" + peer_in.GetPort()) == null) {
                        peers.addPeer(peer_in);
                        need_resend = true;
                    }
                }
                for (Peers.Peer peer_in: peers.Peers.values()) {
                    if (tmp_peers.get(peer_in.GetIp() + ":" + peer_in.GetPort()) == null) {
                        need_resend = true;
                    }
                }
                if (need_resend) {
                    node.generateMessage(id, (peers).getEncoded(), "set_peers");
                }
                break;
            }
            case "set_peers": {
                logger.info(message.type + " id:" + id);
                HashMap<String, Peers.Peer> tmp_peers = (HashMap<String, Peers.Peer>) conf.asObject(message.messageContent);
                boolean need_resend = false;
                for (Peers.Peer peer_in: tmp_peers.values()) {
                    if (peers.Peers.get(peer_in.GetIp() + ":" + peer_in.GetPort()) == null) {
                        peers.addPeer(peer_in);
                        need_resend = true;
                    }
                }
                for (Peers.Peer peer_in: peers.Peers.values()) {
                    if (tmp_peers.get(peer_in.GetIp() + ":" + peer_in.GetPort()) == null) {
                        need_resend = true;
                    }
                }
                if (need_resend) {
                    node.generateMessage("all", (peers).getEncoded(), "set_peers");
                }
                break;
            }
            case "ping": {
                logger.info(message.type + " id:" + id);
                break;
            }
            case "pong": {
                logger.info(message.type + " id:" + id);
                break;
            }
            case "get_block": {
                logger.info(message.type + " id:" + id);
                break;
            }
            case "set_block": {
                logger.info(message.type + " "  + " id:" + id);
                break;
            }
            case "set_tx": {
                logger.info(message.type + " "  + " id:" + id);
                break;
            }
        }
    }

    public void UpdateReadWrite(String message) {
        totalRW = message;
        msg.setText("Connections:" + String.valueOf(connectionCount) + " Block: " + String.valueOf(blockchain.height));
    }

    public void SetConnectionCount(int ConnectionCount) {
        connectionCount = ConnectionCount;
        msg.setText("Connections:" + String.valueOf(connectionCount) + " Block: " + String.valueOf(blockchain.height));
    }

    public void StatusMessage(String Status) {
        msg.setText(Status);
    }

    public void RemovePeer(String Id) {
        for (int count = 0; count < model.getRowCount(); count++) {
            if (model.getValueAt(count, 0).toString().equals(Id)) {
                model.removeRow(count);
            }
        }
    }

    public void AddPeer(String Id) {
        Object[] data0 = {Id};
        model.addRow(data0);
    }

    public void UpdatePeer(String Id, String Ip, int HEIGHT, String Version, int nodeport, Boolean fullnode, long read, long write, long speed, long ping, String DIRECTION) {
        for (int count = 0; count < model.getRowCount(); count++) {
            if (model.getValueAt(count, 0).toString().equals(Id)) {
                model.setValueAt(Ip, count, 1);
                model.setValueAt(HEIGHT, count, 2);
                model.setValueAt(Version, count, 3);
                model.setValueAt(nodeport, count, 4);
                model.setValueAt(fullnode, count, 5);
                model.setValueAt(read, count, 6);
                model.setValueAt(write, count, 7);
                model.setValueAt(speed + "/s", count, 8);
                model.setValueAt(ping + " ms", count, 9);
                model.setValueAt(DIRECTION, count, 10);
            }
        }
    }

}
