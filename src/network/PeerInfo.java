package network;

import com.company.GUI;

import java.io.Serializable;

public class PeerInfo implements Serializable {
    public String version = "1.0.0.0";
    public int height;
    public int nodeport;
    public String nodeaddress;
    public Boolean fullNode;

    public byte[] getEncoded() {
        // write
        byte barray[] = GUI.conf.asByteArray(this);
        return barray;
    }

    public PeerInfo(String version, int height, int nodeport, String nodeaddress, Boolean fullNode) {
        this.version = version;
        this.height = height;
        this.nodeport = nodeport;
        this.nodeaddress = nodeaddress;
        this.fullNode = fullNode;
    }

    public PeerInfo(byte[] parcel) {
        // read
        PeerInfo object = (PeerInfo) GUI.conf.asObject(parcel);
        this.version = object.version;
        this.height = object.height;
        this.nodeport = object.nodeport;
        this.nodeaddress = object.nodeaddress;
        this.fullNode = object.fullNode;
    }

}
