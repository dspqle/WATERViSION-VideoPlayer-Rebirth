package me.srrapero720.watervision.client.screens;

import me.srrapero720.watervision.WaterVision;
import me.srrapero720.watervision.WaterVisionClient;
import me.srrapero720.watervision.client.PlayerFactory;
import me.srrapero720.watervision.client.render.TextureWrapper;
import me.srrapero720.watervision.client.screens.widgets.FadeBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.loading.FMLLoader;
import org.watermedia.api.player.MRL;
import org.watermedia.api.player.MediaPlayer;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class VisionScreen extends Screen {
    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild("watervision", "video_texture");

    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
    }

    private final boolean stretch;
    private final boolean controls;
    private final boolean exit;

    // MEDIA
    private final URI uri;
    private MRL mrl;
    private MediaPlayer videoPlayer;
    private TextureWrapper textureWrapper;

    // CONFIG CARRIED OVER FROM THE COMMAND
    private final int volume;
    private final float speed;

    // STATE
    private Status status = Status.OPENING_GAME;
    private final FadeBackground gameBackground;
    private final FadeBackground videoBackground;

    public VisionScreen(final URI uri, final int volume, final float speed, final boolean stretch, final float gameFadeDuration, final float videoFadeDuration, final boolean controls, final boolean exit) {
        super(Component.literal("WaterVision"));
        this.uri = uri;
        this.volume = Mth.clamp(volume, 0, 100);
        this.speed = Mth.clamp(speed, 0.1f, 3f);
        this.stretch = stretch;
        this.controls = controls;
        this.exit = exit;

        this.gameBackground = new FadeBackground(gameFadeDuration);
        this.videoBackground = new FadeBackground(videoFadeDuration);
        this.videoBackground.forceFadeIn();

        // RESOLVE ASYNCHRONOUSLY; THE PLAYER IS CREATED ONCE THE MRL IS LOADED (see tick()).
        this.mrl = PlayerFactory.resolve(uri);
        Minecraft.getInstance().getSoundManager().pause();
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int pMouseX, final int pMouseY, final float partialTick) {
        this.gameBackground.render(guiGraphics, this.width, this.height, this.status != Status.CLOSING_GAME, partialTick);

        if (this.videoPlayer != null && !this.videoPlayer.error() && (this.status == Status.OPENING_VIDEO || this.status == Status.CLOSING_VIDEO)) {
            if (this.stretch) {
                WaterVisionClient.internal$blit(guiGraphics, TEXTURE, 1, 0, 0, 0, 0, this.width, this.height);
            } else {
                final AspectRatioDimension dim = this.render$getAspectRatio(this.width, this.height, this.videoPlayer.width(), this.videoPlayer.height());
                if (dim.width > 0 && dim.height > 0) {
                    WaterVisionClient.internal$blit(guiGraphics, TEXTURE, 1, dim.x, dim.y, 0, 0, dim.width, dim.height);
                }
            }
        }

        if (this.status != Status.OPENING_GAME && this.status != Status.CLOSING_GAME) {
            this.videoBackground.render(guiGraphics, this.width, this.height, this.status == Status.CLOSING_VIDEO, partialTick);
        }

        if (this.status == Status.OPENING_GAME || this.status == Status.CLOSING_VIDEO || this.status == Status.CLOSING_GAME || this.isPlayerLoading()) {
            this.render$loadingIcon(guiGraphics, partialTick);
        }

        // DEBUG
        if (!FMLLoader.isProduction() && this.videoPlayer != null) {
            guiGraphics.drawString(this.font, String.format("State: %s", this.videoPlayer.status()), 0, (this.height / 2) - 12, 0xFFFFFF);
            guiGraphics.drawString(this.font, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(this.videoPlayer.time())), this.videoPlayer.time(), FORMAT.format(new Date(this.videoPlayer.duration())), this.videoPlayer.duration()), 0, (this.height / 2), 0xFFFFFF);
            guiGraphics.drawString(this.font, String.format("Orchestrator Status: %s", this.status.name()), 0, (this.height / 2) + 24, 0xFFFFFF);
            guiGraphics.drawString(this.font, String.format("Video Size: %sx%s", this.videoPlayer.width(), this.videoPlayer.height()), 0, (this.height / 2) + 36, 0xFFFFFF);
        }
    }

    private boolean isPlayerLoading() {
        if (this.videoPlayer == null) return true;
        return this.videoPlayer.loading() || this.videoPlayer.buffering() || this.videoPlayer.status() == MediaPlayer.Status.WAITING;
    }

    private void render$loadingIcon(final GuiGraphics graphics, final float partialTick) {
        // WaterMedia 3.0 dropped the built-in loading GIF; draw a simple pulsing square instead.
        final int size = 12;
        final int x = this.width - 40;
        final int y = this.height - 40;
        final int pulse = (int) ((1.0f - Math.abs(Math.sin(WaterVision.getTicks() + partialTick * 0.1f))) * 180);
        final int alpha = 60 + pulse;
        graphics.fill(x, y, x + size, y + size, (alpha << 24) | 0xFFFFFF);
    }

    private AspectRatioDimension render$getAspectRatio(final int screenWidth, final int screenHeight, final int videoWidth, final int videoHeight) {
        if (videoWidth <= 0 || videoHeight <= 0) {
            return new AspectRatioDimension(0, 0, screenWidth, screenHeight);
        }
        final float containerAspectRatio = (float) screenWidth / (float) screenHeight;
        final float videoAspectRatio = (float) videoWidth / (float) videoHeight;

        final int renderWidth, renderHeight;

        if (videoAspectRatio > containerAspectRatio) {
            renderWidth = screenWidth;
            renderHeight = (int) (screenWidth / videoAspectRatio);
        } else {
            renderWidth = (int) (screenHeight * videoAspectRatio);
            renderHeight = screenHeight;
        }

        final int offsetX = (screenWidth - renderWidth) / 2;
        final int offsetY = (screenHeight - renderHeight) / 2;

        return new AspectRatioDimension(offsetX, offsetY, renderWidth, renderHeight);
    }

    @Override
    public void renderBackground(final GuiGraphics guiGraphics) {}

    @Override
    public void tick() {
        // RESOLVE / CREATE THE PLAYER ONCE THE MRL IS LOADED
        if (this.videoPlayer == null && this.mrl != null) {
            switch (this.mrl.status()) {
                case LOADED -> {
                    this.videoPlayer = PlayerFactory.createVideo(this.mrl);
                    if (this.videoPlayer != null) {
                        this.videoPlayer.volume(this.volume);
                        this.videoPlayer.speed(this.speed);
                        this.textureWrapper = new TextureWrapper(this.videoPlayer);
                        Minecraft.getInstance().getTextureManager().register(TEXTURE, this.textureWrapper);
                        this.videoPlayer.startPaused();
                    }
                }
                case ERROR, BLOCKED -> {
                    WaterVision.LOGGER.error("Failed to resolve media: {}", this.uri);
                    this.videoPlayer = null;
                    this.mrl = null;
                    Minecraft.getInstance().getSoundManager().resume();
                    Minecraft.getInstance().setScreen(null);
                    return;
                }
                default -> { /* still FETCHING, wait */ }
            }
        }

        switch (this.status) {
            case OPENING_GAME -> {
                if (this.gameBackground.isFadedIn() && this.videoPlayer != null && !this.videoPlayer.error() && !this.isPlayerLoading()) {
                    this.status = Status.OPENING_VIDEO;
                    this.videoPlayer.resume();
                }
            }
            case OPENING_VIDEO -> {
                if (this.videoBackground.isFadedOut() && this.videoPlayer != null && (this.videoPlayer.ended() || this.videoPlayer.stopped() || this.videoPlayer.error())) {
                    this.status = Status.CLOSING_VIDEO;
                }
            }
            case CLOSING_VIDEO -> {
                if (this.videoBackground.isFadedIn()) {
                    this.status = Status.CLOSING_GAME;
                }
            }
            case CLOSING_GAME -> {
                if (this.gameBackground.isFadedOut()) {
                    this.onClose();
                }
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.exit;
    }

    @Override
    public void onClose() {
        if (this.videoPlayer != null && this.videoPlayer.playing()) {
            this.videoPlayer.stop();
            return;
        }
        if (this.status != Status.CLOSING_GAME && this.videoPlayer != null) {
            return;
        }
        Minecraft.getInstance().getSoundManager().resume();
        if (this.videoPlayer != null) {
            this.videoPlayer.release();
            this.videoPlayer = null;
        }
        super.onClose();
    }

    public record AspectRatioDimension(int x, int y, int width, int height) { }

    public enum Status {
        OPENING_GAME,
        OPENING_VIDEO,
        CLOSING_VIDEO,
        CLOSING_GAME,
    }
}