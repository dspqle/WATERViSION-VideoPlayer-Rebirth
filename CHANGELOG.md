# Changelog

## 0.1.0-alpha-rebirth

Port of WATERViSION: VideoPlayer from WATERMeDIA 2.x to the WATERMeDIA 3.0 API line.

### Changed

- Migrated to the WATERMeDIA 3.0 media stack (`MRL` + `MediaPlayer` + `GLEngine` / `ALEngine`).
- WaterMedia dependency coordinate changed to `com.github.SrRapero720:watermedia` (JitPack),
  version `3.0.2`, declared as `compileOnly` so the library is not bundled into the jar.
- Declared compatibility range `[3.0.0,3.1)` — verified the public API surface is identical across
  3.0.1 and 3.0.2, so a single build covers the whole 3.0 line.

### Added

- `client/PlayerFactory` — centralises `MRL` resolution and `MediaPlayer` construction with the
  Minecraft render-thread `GLEngine` supplier and `ALEngine` sound engine.
- `common/util/MathHelper` — local replacement for the removed `MathAPI` (`easeIn`, `scaleTempo`,
  `argb`).

### Removed

- All usages of the removed `ImageAPI`, `ImageRenderer` and `loadingGif`. The animated loading
  indicator is replaced with a pulsing rectangle.
- The `preRender()` call — texture uploads are now handled by the render engine automatically.

### Notes

- `watermedia_android_bridge` must not be installed alongside WATERMeDIA 3.0; it pins
  `versionRange="[2.1.36,3.0.0)"` and aborts startup.
- WATERMeDIA 3.0.1 binaries only ship GNU/Linux (`glibc`) FFmpeg builds, so video decoding does not
  work on Android. The failure is caught by WATERMeDIA and does not crash the game.
