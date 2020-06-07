package com.github.euonmyoji.bulletcurtain.command;

import com.github.euonmyoji.bulletcurtain.BulletCurtain;
import com.github.euonmyoji.bulletcurtain.BulletSocket;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.IOException;

public class BulletCommand {
    private static final CommandSpec subscribe = CommandSpec.builder()
            .arguments(GenericArguments.integer(Text.of("BLiveRoom")))
            .executor((src, args) -> {
                for (Integer bLiveRoom : args.<Integer>getAll("BLiveRoom")) {
                    BulletSocket bulletSocket = BulletCurtain.sockets.get(bLiveRoom);
                    if (bulletSocket != null && bulletSocket.isOpen()) {
                        src.sendMessage(Text.of(bLiveRoom + " 已连接了"));
                    } else {
                        Task.builder().async().execute(() -> new BulletSocket(bLiveRoom)).submit(BulletCurtain.plugin);
                    }
                }
                return CommandResult.success();
            }).build();
    private static final CommandSpec disconnect = CommandSpec.builder()
            .arguments(GenericArguments.integer(Text.of("BLiveRoom")))
            .executor((src, args) -> {
                for (Integer bLiveRoom : args.<Integer>getAll("BLiveRoom")) {
                    BulletSocket bulletSocket = BulletCurtain.sockets.get(bLiveRoom);
                    if (bulletSocket != null && bulletSocket.isOpen()) {
                        try {
                            bulletSocket.close();
                            BulletCurtain.sockets.remove(bLiveRoom, bulletSocket);
                        } catch (IOException e) {
                            src.sendMessage(Text.of("断开连接异常:" + e.toString()));
                            BulletCurtain.logger.warn("断开连接异常", e);
                        }
                    } else {
                        src.sendMessage(Text.of(bLiveRoom + " 已断开了"));
                    }
                }
                return CommandResult.success();
            }).build();
    private static final CommandSpec list = CommandSpec.builder()
            .executor((src, args) -> {
                BulletCurtain.sockets.forEach((integer, bulletSocket) -> src.sendMessage(Text.of(integer + ": " + bulletSocket.isOpen())));
                return CommandResult.success();
            }).build();

    public static final CommandSpec danmu = CommandSpec.builder()
            .executor((src, args) -> {
                src.sendMessage(Text.of("/danmu subscribe <BLiveRoom>"));
                src.sendMessage(Text.of("/danmu disconnect <BLiveRoom>"));
                src.sendMessage(Text.of("/danmu list"));
                return CommandResult.success();
            })
            .child(subscribe, "subscribe", "watch")
            .child(disconnect, "disconnect")
            .child(list, "list")
            .build();
}
