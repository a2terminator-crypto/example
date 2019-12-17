package network;

import com.company.GUI;

import java.io.Serializable;

public class Message implements Serializable {
  public byte[]   messageContent;
  public String   type;

  public Message(byte[] message, String type) {
    this.messageContent = message;
    this.type = type;
  }

  public Message(String type, byte[] messageContent) {
    this.type = type;
    this.messageContent = messageContent;
  }

  public Message(byte[] parcel) {
    // read
    Message object = (Message) GUI.conf.asObject(parcel);
    this.messageContent = object.messageContent;
    this.type = object.type;
  }

  public byte[] getEncoded() {
    // write
    byte barray[] = GUI.conf.asByteArray(this);
    return barray;
  }

}