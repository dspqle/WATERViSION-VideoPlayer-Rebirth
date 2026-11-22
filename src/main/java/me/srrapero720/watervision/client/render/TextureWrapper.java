package me.srrapero720.watervision.client.render;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.watermedia.api.player.MediaPlayer;

import java.util.function.LongSupplier;

/**
 * Wraps the current RGBA texture handle of a WaterMedia {@link MediaPlayer} as a Minecraft
 * {@link AbstractTexture} so it can be registered in the texture manager and bound by a
 * {@code ResourceLocation}. The handle is read fresh on every {@link #getId()} call, matching
 * WaterMedia 3.0's requirement to sample the texture in the render cycle.
 */
public class TextureWrapper extends AbstractTexture {
    private final LongSupplier textureSupplier;

    public TextureWrapper(final MediaPlayer player) {
        this.textureSupplier = player::texture;
    }

    public TextureWrapper(final LongSupplier textureSupplier) {
        this.textureSupplier = textureSupplier;
    }

    @Override
    public int getId() {
        return (int) this.textureSupplier.getAsLong();
    }

    @Override
    public void load(@NotNull final ResourceManager manager) { /* NO OP */ }

    @Override
    public void releaseId() { /* NO OP - owned by the player/engine */ }

    @Override
    public void close() { /* NO OP */ }
}