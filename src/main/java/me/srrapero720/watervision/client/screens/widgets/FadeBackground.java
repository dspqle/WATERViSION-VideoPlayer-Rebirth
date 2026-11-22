package me.srrapero720.watervision.client.screens.widgets;

import me.srrapero720.watervision.common.util.MathHelper;
import net.minecraft.client.gui.GuiGraphics;

public class FadeBackground {
    private float fadeTick = 0.0f;
    private final float fadeTickDuration; // Duration in ticks (1 tick = 1/20 second)

    public FadeBackground(final float duration) {
        this.fadeTickDuration = Math.max(0.01f, duration);
    }

    public void render(final GuiGraphics graphics, final int width, final int height, final boolean fadeIn, float partialTicks) {
        if (fadeIn) {
            if (this.fadeTick < this.fadeTickDuration) {
                this.fadeTick += partialTicks;
            }
            if (this.fadeTick > this.fadeTickDuration) this.fadeTick = this.fadeTickDuration;
        } else {
            if (this.fadeTick > 0) {
                this.fadeTick -= partialTicks;
            }
            if (this.fadeTick < 0) this.fadeTick = 0;
        }

        // Calculate alpha based on fadeTick
        final int alpha = (int) MathHelper.easeIn(0, 255, MathHelper.scaleTempo(0, this.fadeTickDuration, this.fadeTick));

        // Draw the background with the calculated alpha
        graphics.fill(0, 0, width, height, MathHelper.argb(alpha, 0, 0, 0)); // Black background with variable alpha
    }

    public boolean isFadedOut() {
        return this.fadeTick <= 0.0f;
    }
    public boolean isFadedIn() {
        return this.fadeTick >= this.fadeTickDuration;
    }

    public void forceFadeOut() {
        this.fadeTick = 0.0f;
    }

    public void forceFadeIn() {
        this.fadeTick = this.fadeTickDuration;
    }
}
