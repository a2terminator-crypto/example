package network;

import com.company.GUI;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Peers implements Serializable {
    static Logger logger = Logger.getLogger(Peers.class.getName());
    public HashMap<String, Peer> Peers = new HashMap<String, Peer>();

    public static class Peer implements Serializable {
        private static final long serialVersionUID = 1L;
        private String IP;
        private int PORT;
        private long lastActive;
        private Boolean Banned;

        public Peer(String ip, int port, long lastactive, Boolean banned ) {
            this.IP = ip;
            this.PORT = port;
            this.lastActive = lastactive;
            this.Banned = banned;
        }

        public String GetIp() {
            return this.IP;
        }

        public int GetPort() {
            return this.PORT;
        }

        public long GetLastActive() {
            return this.lastActive;
        }

        public void SetLastActive(long lastactive) {
            this.lastActive = lastactive;
        }

        public void SetBanned(Boolean banned) {
            this.Banned = banned;
        }

        public Boolean GetBanned() {
            return this.Banned;
        }
    }

    public void Init() {
        logger.info("Initialize the list of nodes");
        Peers.put("79.140.234.66" + ":" + 6666, new Peer("79.140.234.66", 6666, new Date().getTime(), false));
    }

    public Peers(HashMap<String, Peer> Peers) {
        this.Peers = Peers;
    }

    public Peers() {}

    public void addPeer(Peer peer) {
        Boolean found = false;
        for (Peer mypeer : Peers.values()) {
            if (mypeer.IP.equals(peer.IP) && mypeer.PORT == peer.PORT) {
                found = true;
                break;
            } else {
                found = false;
            }
        }
        if (!found) {
            Peers.put(peer.GetIp() + ":" + peer.GetPort(), peer);
        }
    }

    public byte[] getEncoded() {
        // write
        byte barray[] = GUI.conf.asByteArray(Peers);
        return barray;
    }

    public Peers(byte[] parcel) {
        // read
        HashMap<String, Peer> object = (HashMap<String, Peer>) GUI.conf.asObject(parcel);
        this.Peers = object;
    }

}
