package me.imgalvin.Mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class PublishLANServerMixin {
    @Shadow
    public abstract boolean publishServer(@Nullable GameType gameMode, boolean allowCommands, int port);

    @Inject(at = @At("TAIL"), method = "loadLevel")
    private void init(CallbackInfo info) {
        // This code is injected into the start of MinecraftServer.loadLevel()V
        // Wait until net.minecraft.client.Minecraft.getConnection() is not null
        new Thread(() -> {
            while (Minecraft.getInstance().getConnection() == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int port = HttpUtil.getAvailablePort();
            publishServer(GameType.CREATIVE, true, port);
            MinecraftServer server = (MinecraftServer) (Object) this;
            server.getPlayerList().broadcastSystemMessage(Component.literal("§aOpened to LAN on port " + port), false);
        }).start();
    }
}
