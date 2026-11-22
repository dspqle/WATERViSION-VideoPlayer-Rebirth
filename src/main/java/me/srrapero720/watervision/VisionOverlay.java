package me.srrapero720.watervision;

import me.srrapero720.watervision.client.PlayerFactory;
import me.srrapero720.watervision.client.render.TextureWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.watermedia.api.player.MRL;
import org.watermedia.api.player.MediaPlayer;

import java.net.URI;

@Mod.EventBusSubscriber(modid = WaterVision.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class VisionOverlay {
    private static final int PADDING = 8;
    private static MediaPlayer player;
    private static MRL mrl;
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild("watervision", "overlay_texture");
    static URI uri;
    static URI activeUri;

    public static void onClientPause(boolean pause) {
        if (player != null && player.paused() != pause) {
            player.pause(pause);
        }
    }

    @SubscribeEvent
    public static void onRenderOverlayPost(final RenderGuiOverlayEvent.Pre e) {
        // START A NEW OVERLAY WHEN A URI IS REQUESTED AND NONE IS ACTIVE
        if (uri != null && player == null) {
            if (mrl == null || activeUri == null || !activeUri.equals(uri)) {
                mrl = PlayerFactory.resolve(uri);
                activeUri = uri;
            }
            // WAIT FOR THE MRL TO RESOLVE BEFORE BUILDING THE PLAYER
            if (mrl != null && mrl.status() == MRL.Status.LOADED) {
                player = PlayerFactory.createVideo(mrl);
                if (player != null) {
                    Minecraft.getInstance().getTextureManager().register(TEXTURE, new TextureWrapper(player));
                    player.start();
                }
            } else if (mrl != null && (mrl.status() == MRL.Status.ERROR || mrl.status() == MRL.Status.BLOCKED)) {
                Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal("Failed to open a video overlay"), true);
                mrl = null;
                activeUri = null;
                uri = null;
            }
        }

        if (player == null) {
            return;
        }

        if (player.error()) {
            Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal("Failed to open a video overlay"), true);
            player.release();
            player = null;
            return;
        }

        if (uri == null && activeUri != null) {
            player.release();
            player = null;
            activeUri = null;
            return;
        }

        if (activeUri != null && uri != null && !activeUri.equals(uri)) {
            player.release();
            player = null;
            mrl = PlayerFactory.resolve(uri);
            activeUri = uri;
            return;
        }

        if (player.playing() && player.texture() != MediaPlayer.NO_TEXTURE) {
            final GuiGraphics graphics = e.getGuiGraphics();

            final int screenWidth = graphics.guiWidth();
            final int screenHeight = graphics.guiHeight();

            final int x = (int) (screenWidth / 1.6f) - PADDING;
            final int y = (int) (screenHeight / 1.6f) - PADDING;
            int width = screenWidth - x;
            int height = screenHeight - y;
            width -= PADDING;
            height -= PADDING;

            WaterVisionClient.internal$blit(graphics, TEXTURE, 1.0f, x, y, 0, 0, width, height);
        } else if (player.ended()) {
            uri = null;
            activeUri = null;
            player.release();
            player = null;
        }
    }

    public static void onClientDisconnect() {
        if (player != null) {
            player.release();
        }
        player = null;
        mrl = null;
        uri = null;
        activeUri = null;
    }
}