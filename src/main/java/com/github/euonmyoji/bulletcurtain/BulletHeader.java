package com.github.euonmyoji.bulletcurtain;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author yinyangshi
 */
public class BulletHeader {

    /**
     * 消息总长度 (协议头 + 数据长度)
     */
    public int packetLength;
    /**
     * 头长度 固定16 Bytes
     */
    public short headerLength;
    public short version;
    /**
     * 消息类型
     */
    public int action;
    /**
     * 参数 固定为1
     */
    public int parameter;

    public BulletHeader(DataInputStream in) throws IOException {
        //4B
        packetLength = in.readInt();
        //2B
        headerLength = in.readShort();
        //2B
        version = in.readShort();
        //4B
        action = in.readInt();
        //4B
        parameter = in.readInt();
    }

}
