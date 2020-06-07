package com.github.euonmyoji.bulletcurtain;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author yinyangshi
 */
public class BulletSocket implements Closeable {
    private int roomID;
    private Socket socket;
    private DataInputStream in;
    private boolean closed = false;

    public BulletSocket(int roomID) {
        try {
            this.roomID = roomID;
            URL url = new URL("https://api.live.bilibili.com/room/v1/Danmu/getConf?room_id=" + roomID);
            URLConnection con = url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setReadTimeout(5000);
            con.setAllowUserInteraction(true);
            JsonObject json = new JsonParser().parse(new InputStreamReader(con.getInputStream())).getAsJsonObject();

            String token = json.get("data").getAsJsonObject().get("token").getAsString();
            String host = json.getAsJsonObject("data").get("host").getAsString();
            String port = json.getAsJsonObject("data").get("port").getAsString();
            socket = new Socket(host, Integer.parseInt(port));
            sendJoinChannel(new DataOutputStream(socket.getOutputStream()), token);
            Task.builder().async().interval(30, TimeUnit.SECONDS).execute((task) -> {
                try {
                    if (!socket.isClosed() && !closed) {
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        sendHeart(out);
                    } else {
                        task.cancel();
                    }
                } catch (IOException e) {
                    BulletSocket.this.gotException(e);
                }
            }).submit(BulletCurtain.plugin);
            in = new DataInputStream(socket.getInputStream());
            BulletCurtain.sockets.put(roomID, this);
            while (!socket.isClosed() && !this.closed) {
                try {
                    socket.setSoTimeout(0);
                    BulletHeader bulletHeader = new BulletHeader(in);
                    if (bulletHeader.packetLength < 16) {
                        throw new IllegalArgumentException("协议失败: (L:" + bulletHeader.packetLength + ")");
                    }
                    int payloadLength = bulletHeader.packetLength - 16;
                    if (payloadLength == 0) {
                        continue;
                    }
                    byte[] buffer = new byte[payloadLength];
                    int read = 0;
                    do {
                        read += in.read(buffer, read, payloadLength - read);
                    } while (read < payloadLength);
                    if (bulletHeader.version == 2 && bulletHeader.action == 5) {
                        try (InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(buffer, 2, buffer.length - 2),
                                new Inflater(true))) {
                            while (inflater.available() > 0) {
                                DataInputStream dataInputStream = new DataInputStream(inflater);
                                bulletHeader = new BulletHeader(dataInputStream);
                                processBullet(bulletHeader.action, dataInputStream);
                            }
                        } catch (IOException e) {
                            this.gotException(e);
                        }
                    } else {
                        processBullet(bulletHeader.action, new DataInputStream(new ByteArrayInputStream(buffer)));
                    }
                } catch (RuntimeException e) {
                    gotException(e);
                }
            }
        } catch (IOException e) {
            this.gotException(e);
        }
    }

    public boolean isOpen() {
        return !this.closed;
    }

    private void gotException(Throwable e) {
        Sponge.getServer().getBroadcastChannel().send(Text.of(this.roomID + " 发生异常:" + e.toString()));
        BulletCurtain.logger.info("Room Exception got ", e);
        try {
            close();
        } catch (IOException ioException) {
            BulletCurtain.logger.info("Close room socket got ", e);
        }
    }


    private void sendJoinChannel(DataOutputStream out, String token) throws IOException {
        StringWriter stringWriter = new StringWriter();
        new JsonWriter(stringWriter).beginObject()
                .name("roomid")
                .value(roomID)
                .name("uid").value(0)
                .name("token").value(token)
                .name("platform").value("MC BC M/P")
                .name("protover").value(2)
                .endObject().flush();
        sendSocketData(out, 7, stringWriter.toString());
        Sponge.getServer().getBroadcastChannel().send(Text.of("成功连接直播间:" + roomID));
    }

    private void sendHeart(DataOutputStream out) throws IOException {
        sendSocketData(out, 0, (short) 16, (short) 1, 2, 1, "");
    }

    private void sendSocketData(DataOutputStream out, int action, String body) throws IOException {
        sendSocketData(out, 0, (short) 16, (short) 1, action, 1, body);
    }

    private void sendSocketData(DataOutputStream out, int packetLength, short magic, short ver, int action,
                                int param, String body) throws IOException {
        byte[] bodyData = body.getBytes(StandardCharsets.UTF_8);
        if (packetLength == 0) {
            packetLength = bodyData.length + 16;
        }
        out.writeInt(packetLength);
        out.writeShort(magic);
        out.writeShort(ver);
        out.writeInt(action);
        out.writeInt(param);

        if (bodyData.length > 0) {
            out.write(bodyData);
        }
        out.flush();
    }

    private void processBullet(int action, DataInputStream in) {
        //3是人气回调 无视无视（

        switch (action) {
            case 5: {
                JsonElement json = new JsonParser().parse(new InputStreamReader(in, Charsets.UTF_8));
                try {
                    BulletData bulletData = new BulletData(json, roomID, 2);
                    if (bulletData.type != null) {
                        switch (bulletData.type) {
                            case BulletData.COMMENT_TYPE: {
                                Sponge.getServer().getBroadcastChannel().send(Text.of(bulletData.toString()));
                                break;
                            }
                            case BulletData.LIVE_START_TYPE: {
                                Sponge.getServer().getBroadcastChannel().send(Text.of(roomID + "开启了直播"));
                                break;
                            }
                            case BulletData.LIVE_STOP_TYPE: {
                                Sponge.getServer().getBroadcastChannel().send(Text.of(roomID + "关闭了直播"));
                                break;
                            }
                        }
                    }
                } catch (Throwable t) {
                    this.gotException(t);
                }
                break;
            }
            case 3:
            case 8:
            default: {
                break;
            }
        }
    }

    @Override
    public void close() throws IOException {
        Sponge.getServer().getBroadcastChannel().send(Text.of(this.roomID + " 断开连接"));
        closed = true;
        if (in != null) {
            in.close();
        }
        if (!socket.isClosed()) {
            socket.close();
        }
    }
}
