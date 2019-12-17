package network;

import com.company.Blockchain;
import com.company.GUI;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class Node implements Runnable {
  static Logger logger = Logger.getLogger(Node.class.getName());
  private ServerSocket server;
  private Thread thread;
  private InetAddress address;
  private int port;
  public Blockchain blockchain;
  public GUI listener;
  private long totalread = 0;
  private long totalwrite = 0;
  private HashMap<String, NodeThread> connections = new HashMap<String, NodeThread>();
  private int maxConnections;

  public InetAddress getIP() {
    // This try will give the Public IP Address of the Host.
    try
    {
      URL url = new URL("http://bot.whatismyipaddress.com");
      BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
      String ipAddress = new String();
      ipAddress = (in.readLine()).trim();
      InetAddress ip = InetAddress.getByName(ipAddress);
      /* IF not connected to internet, then
       * the above code will return one empty
       * String, we can check it's length and
       * if length is not greater than zero,
       * then we can go for LAN IP or Local IP
       * or PRIVATE IP
       */
      if (!(ipAddress.length() > 0))
      {
        try
        {
          ip = InetAddress.getByName(ipAddress);
          return (ip);
        }
        catch(Exception ex)
        {
          return null;
        }
      }
      return (ip);
    }
    catch(Exception e)
    {
      // This try will give the Private IP of the Host.
      try
      {
        InetAddress ip = InetAddress.getLocalHost();
        return (ip);
      }
      catch(Exception ex)
      {
        return null;
      }
    }
  }

  public Node(int port, GUI listener, Blockchain BlockChain) {
    try{
      this.maxConnections = 3;
      this.listener = listener;
      this.blockchain = BlockChain;
      this.server = new ServerSocket(port);
      this.address = getIP();
      this.port = port;
      start();
    } catch (IOException ioe) {
      logger.error(String.format("Error:%s", ioe.getMessage()));
    }
  }

  public void start() {
    if (thread == null) {
      thread = new Thread(this);
      thread.start();
      logger.info(String.format("Node started: address:%s port:%s", getAddress(), getPort()));
    }
  }

  @Override
  public void run() {
    while (thread != null) {
      try {
        Socket socket = server.accept();
        if (maxConnections > getNodeCount()){
          newThread(socket, "IN");
        } else {
          socket.close();
        }
      } catch (IOException ioe) {
        logger.error(String.format("Error:%s", ioe.getMessage()));
      }
    }
  }

  public void newThread(Socket socket, String IN) {
    UUID uuid =  UUID.randomUUID();
    String uid = uuid.toString();
    connections.put(uid, new NodeThread(this, socket, uid, IN));
    try {
      NodeThread thread = connections.get(uid);
      if (thread != null) {
        thread.open();
        thread.start();
        listener.SetConnectionCount(connections.size());
      }
    } catch (IOException ioe) {
      swap(uid);
      logger.error(String.format("Error:%s", ioe.getMessage()));
      this.listener.messageReceived("all", new Message(("Error: " + ioe.getMessage()).getBytes(), "err"));
    }
  }

  public void swap(String id) {
    NodeThread thread = connections.get(id);
    if (thread != null) {
      thread.stopit();
      connections.remove(thread.id);
      listener.RemovePeer(thread.id);
      listener.SetConnectionCount(connections.size());
      logger.info(String.format("Client Thread stop: id:%s address:%s port:%s", id, thread.getNodeAddress(), thread.getNodePort()));
    }
  }

  public boolean connect(String addr, int port) {
    try {
      Socket socket = new Socket(addr, port);
      newThread(socket, "OUT");
      this.listener.messageReceived("all", new Message(("Connected to " + addr + ":"+ port).getBytes(), "connected"));
      return true;
    } catch (UnknownHostException uhe) {
      this.listener.messageReceived("all", new Message(("Unable to connect to: " + uhe.getMessage()).getBytes(), "err"));
      logger.error(String.format("Unable to connect to: address:%s port:%s error:%s", addr, port, uhe.getMessage()));
      return false;
    } catch (IOException ioe) {
      this.listener.messageReceived("all", new Message(("Error: " + ioe.getMessage()).getBytes(), "err"));
      logger.error(String.format("Unable to connect to: address:%s port:%s error:%s", addr, port, ioe.getMessage()));
      return false;
    }
  }

  public Message generateMessage(String id, byte[] input, String type) {
    Message newMessage = new Message(input, type);
    totalread = 0;
    totalwrite = 0;
    for (NodeThread thread:connections.values()) {
      totalread = totalread + thread.getTotalread();
      totalwrite = totalwrite + thread.getTotalwrite();
      if (thread.id.equals(id) || id.equals("all")) {
        thread.sendMessage(newMessage);
      }
    }
    return newMessage;
  }

  public void receiveMessage(String id, Message message) {
    this.listener.messageReceived(id, message);
    for (NodeThread thread:connections.values()) {
      if (!thread.getNodeAddress().equals(getAddress()) && thread.getNodePort() != getPort()) {
        if (thread.id.equals(id)){
          thread.sendMessage(message);
        }
      }
    }
    this.listener.UpdateReadWrite(" Traffic: R:"+totalread+" W:" + totalwrite);
  }

  public void leave() {
    for (NodeThread thread:connections.values()) {
      Message leaveMessage = new Message("".getBytes(), "leave");
      thread.sendMessage(leaveMessage);
      thread.stopit();
    }
  }

  public int getMaxConnections() {
    return maxConnections;
  }

  public String getAddress() {
    return address.getHostAddress();
  }

  public int getPort() {
    return port;
  }

  public long getTotalread() {
    return totalread;
  }

  public long getTotalwrite() {
    return totalwrite;
  }

  public ArrayList<NodeThread> getActiveNodes() {
    ArrayList<NodeThread> tmp = new ArrayList<>();
    for (NodeThread thread:connections.values()){
      tmp.add(thread);
    }
    return tmp;
  }

  public HashMap<String, NodeThread> getConnections() {
    return connections;
  }

  public int getNodeCount() {
    return connections.size();
  }

}
