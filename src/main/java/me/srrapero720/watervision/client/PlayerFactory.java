package me.srrapero720.watervision.client;

import net.minecraft.client.Minecraft;
import org.watermedia.api.player.MRL;
import org.watermedia.api.player.MediaPlayer;
import org.watermedia.api.player.PlayerAPI;

import java.net.URI;
import java.util.concurrent.Executor;

/**
 * Bridges WaterVision to the WaterMedia 3.0 player API: resolves a {@code URI} into an {@link MRL}
 * and builds a {@link MediaPlayer} backed by an OpenGL video engine and an OpenAL audio engine,
 * bound to the Minecraft render thread.
 */
public final class PlayerFactory {
    private PlayerFactory() {}

    /** The Minecraft render thread owns the GL context. */
    private static Thread renderThread() {
        return Thread.currentThread();
    }

    /** An executor dispatching onto the Minecraft render thread, which owns the GL context. */
    private static Executor renderExecutor() {
        return Minecraft.getInstance()::tell;
    }

    /**
     * Resolves the given URI into an MRL. The MRL loads asynchronously; poll {@link MRL#status()}
     * until {@link MRL.Status#LOADED} before calling {@link #createVideo(MRL)}.
     */
    public static MRL resolve(final URI uri) {
        return PlayerAPI.mrl(uri);
    }

    /**
     * Builds a media player with both video (OpenGL) and audio (OpenAL) output for the given MRL,
     * or {@code null} when the MRL is not ready or no player can be built.
     */
    public static MediaPlayer createVideo(final MRL mrl) {
        return PlayerAPI.player(mrl,
                () -> PlayerAPI.glEngine(renderThread(), renderExecutor()),
                PlayerAPI::alEngine);
    }
}