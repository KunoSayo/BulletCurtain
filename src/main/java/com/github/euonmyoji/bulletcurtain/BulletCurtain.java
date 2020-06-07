package com.github.euonmyoji.bulletcurtain;

import com.github.euonmyoji.bulletcurtain.command.BulletCommand;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yinyangshi
 */
@Plugin(id = "bulletcurtain", name = "BulletCurtain", version = "@spongeVersion@",
        description = "Show bilibili bullets", authors = "yinyangshi")
public class BulletCurtain {
    public static Map<Integer, BulletSocket> sockets = new HashMap<>();

    public static BulletCurtain plugin;
    public static Logger logger;

    @Inject
    public BulletCurtain(Logger logger) {
        BulletCurtain.plugin = this;
        BulletCurtain.logger = logger;
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        Sponge.getCommandManager().register(this, BulletCommand.danmu, "danmu", "bullet", "danmuku", "弹幕姬");
    }

    @Listener
    public void onStopping(GameStoppingEvent event) {
        for (BulletSocket value : sockets.values()) {
            try {
                value.close();
            } catch (IOException e) {
                logger.info("close socket error", e);
            }
        }
        sockets.clear();
    }
}
