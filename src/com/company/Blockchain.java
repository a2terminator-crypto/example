package com.company;

public class Blockchain {
    public static int height;
    public static boolean fullNode;
    public String version = "1.0.0.0";

    public Blockchain(int height, boolean fullNode, String version) {
        this.height = height;
        this.fullNode = fullNode;
        this.version = version;
    }

}
