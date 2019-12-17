package network;

import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

public class NodeThread extends Thread {
  static Logger logger = Logger.getLogger(NodeThread.class.getName());
  private DataInputStream messageIn  =  null;
  private DataOutputStream messageOut = null;
  private Node node;
  private Socket socket;
  public String id;
  private Boolean running = false;
  private String nodeAddress = "";
  private int nodePort = 0;
  private int height;
  private String version;
  private long totalread = 0;
  private long totalwrite = 0;
  private long ping = 0;
  private long speed = 0;
  private Boolean fullnode = false;
  private String IN = "";

  public NodeThread(Node node, Socket socket, String id, String IN) {
    this.IN = IN;
    this.node = node;
    this.socket = socket;
    this.id = id;
    this.running = true;
    node.listener.messageReceived(id,  new Message(new byte[0], "connected"));
    logger.info("Client Thread started:" + id + " - " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
  }

  public void open() throws IOException {
    this.messageIn = new DataInputStream(this.socket.getInputStream());
    this.messageOut = new DataOutputStream(this.socket.getOutputStream());
  }

  public void run() {
    while (running) {
      try {
        Date start = new Date();
        int length = messageIn.readInt();
        byte[] messageData = new byte[length];
        messageIn.readFully(messageData, 0, length);
        totalread = totalread + length;
        float time = new Date().getTime() - start.getTime();
        if (time > 0 && length > 0) {
          speed = (long) ((float) length / (time / 1000));
        }
        try {
          Message message = parseMessage(messageData);
        } catch (Exception ex) {
          logger.error(String.format("Error:%s", ex.getMessage()));
          node.swap(id);
        }
      } catch (IOException ioe) {
        node.swap(id);
        this.node.listener.messageReceived(id, new Message(("Error: " + ioe.getMessage()).getBytes(), "err"));
      }
    }
  }

  public void stopit() {
    this.running = false;
  }

  public void sendMessage(Message msg) {
    try {
      byte[] data = msg.getEncoded();
      int length = data.length;
      totalwrite = totalwrite + length;
      this.messageOut.writeInt(length); // write length of the message
      this.messageOut.write(data); // write Data of the message
    } catch (IOException ioe) {
      logger.error(String.format("Error:%s", ioe.getMessage()));
      node.swap(id);
      this.node.listener.messageReceived(id, new Message(("Error: " + ioe.getMessage()).getBytes(), "err"));
    }
  }

  private Message parseMessage(byte[] msg) {
    Message mess = new Message(msg);
    byte[] parcelData = mess.messageContent;
    String type = mess.type;
    byte[] msgrs = parcelData;
    Message message = null;
    switch(type) {
      case "leave": {
        this.node.swap(id);
        break;
      }
      case "ping": {
        message = new Message(msgrs, type);
        this.node.receiveMessage(id, message);
        break;
      }
      case "pong": {
        message = new Message(msgrs, type);
        this.node.receiveMessage(id, message);
        break;
      }
      case "get_node_info": {
        message = new Message(msgrs, type);
        this.node.receiveMessage(id, message);
        break;
      }
      case "set_node_info": {
        message = new Message(msgrs, type);
        this.node.receiveMessage(id, message);
        break;
      }
      case "get_peers": {
        message = new Message(msgrs, type);
        this.node.receiveMessage(id, message);
        break;
      }
      case "set_peers": {
        message = new Message(msgrs, type);
        this.node.receiveMessage(id, message);
        break;
      }
      case "get_block": {
        message = new Message(msgrs, type);
        this.node.receiveMessage(id, message);
        break;
      }
      case "set_block": {
        message = new Message(msgrs, type);
        this.node.receiveMessage(id, message);
        break;
      }
      case "set_tx": {
        message = new Message(msgrs, type);
        this.node.receiveMessage(id, message);
        break;
      }
    }
    return message;
  }

  public Socket getSocket() {
    return socket;
  }

  public String getNodeAddress() {
    return nodeAddress;
  }

  public void setNodeAddress(String nodeAddress) {
    this.nodeAddress = nodeAddress;
  }

  public int getNodePort() {
    return nodePort;
  }

  public void setNodePort(int nodePort) {
    this.nodePort = nodePort;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public long getTotalread() {
    return totalread;
  }

  public long getTotalwrite() {
    return totalwrite;
  }

  public long getPing() {
    return ping;
  }

  public void setPing(long ping) {
    this.ping = ping;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public long getSpeed() {
    return speed;
  }

  public String getIN() {
    return IN;
  }

  public boolean getFullNode() {
    return fullnode;
  }

  public void setFullnode(Boolean fullnode) {
    this.fullnode = fullnode;
  }
}
