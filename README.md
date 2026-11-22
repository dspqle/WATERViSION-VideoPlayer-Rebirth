# WATERViSION: VideoPlayer — Rebirth

Unofficial port of [WATERViSION: VideoPlayer](https://github.com/SrRapero720/watervision) to the
**WATERMeDIA: Multimedia API 3.0 line**.

WATERMeDIA 3.0.0 was a full rewrite of the media stack: the VLC backend and the whole 2.x
`PlayerAPI` surface were replaced by a new `MRL` + `MediaPlayer` + engine (`GLEngine` / `ALEngine`)
architecture backed by FFmpeg. Mods written against 2.x cannot load against 3.0, so this project
re-implements the WATERViSION client rendering and playback layer on top of the new API.

- Original mod: <https://www.mcmod.cn/class/28618.html>
- WaterMedia: <https://www.mcmod.cn/class/11571.html>
- Not affiliated with Mr.Puzzle or PuzzleVision

## Compatibility

| Item | Version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.6+ |
| Java | 17 |
| WATERMeDIA | `[3.0.0,3.1)` — 3.0.0 / 3.0.1 / 3.0.2 all supported |
| WATERMeDIA: Binaries | required at runtime (provides FFmpeg) |

The whole 3.0 line is supported because the public API used by this mod was verified to be
identical between 3.0.1 and 3.0.2 (`PlayerAPI`, `MediaPlayer`, `MediaPlayer.Status`, `MRL.Status` —
checked with `javap` against both published jars). No version-specific branching is needed.

## Commands

All commands require permission level 4 unless noted.

### `/playvideo <url> <targets> [volume] [speed] [stretch_video] [game_fade_duration] [video_fade_duration] [allow_controls] [allow_exit]`

Plays a video full-screen on the given players.

- `url` — direct link to a video file (`.mp4`, `.webm`, …). **Must be quoted.**
- `targets` — player selector, e.g. `@a` or `@p`
- `volume` — `0`–`100`, default `100`
- `speed` — `0.25`–`2.0`, default `1.0`
- `stretch_video` — `true`/`false`, default `false`
- `game_fade_duration` — `0.0`–`100.0`, default `20.0`
- `video_fade_duration` — `0.0`–`100.0`, default `20.0`
- `allow_controls` — `true`/`false`, default `true`
- `allow_exit` — `true`/`false`, default `true`

Example:

```
/playvideo "https://example.com/clip.mp4" @a 80 1.0 false 20.0 20.0 true true
```

### `/stopvideo <targets>`

Stops full-screen playback for the given players.

### `/playoverlay <url> <targets>`

Plays a video as a HUD overlay instead of taking over the screen.

### `/stopoverlay <targets>`

Stops the HUD overlay playback.

### `/playvideoclient <url> [volume] [speed] [stretch_video]`

Client-side only, no server interaction and no permission required.

## Migrating from WaterMedia 2.x — what changed

| WaterMedia 2.x | WaterMedia 3.0 |
|---|---|
| `PlayerAPI.getFactory()` | `PlayerAPI.glEngine(thread, executor)` / `PlayerAPI.alEngine()` |
| `VideoPlayer` | `MRL` + `MediaPlayer` |
| `VideoPlayer.start(uri)` | `PlayerAPI.mrl(uri)` → poll `MRL.status()` → `PlayerAPI.player(...)` → `start()` |
| `texture()` returns `int` | `texture()` returns **`long`** (`NO_TEXTURE = 0L`) |
| `preRender()` | removed — the render engine uploads automatically |
| `isSafeUse()` / `isBroken()` | `error()` |
| `isReady()` | `!loading() && !buffering() && status() != WAITING` |
| `MathAPI` | removed; `MathHelper` added locally (`easeIn`, `scaleTempo`, `argb`) |
| `ImageAPI` / `ImageRenderer` / `loadingGif` | removed entirely |

Files rewritten for the port:

- `client/PlayerFactory.java` — **new**, wraps `MRL` resolution and `MediaPlayer` construction
- `client/render/TextureWrapper.java` — now backed by a `LongSupplier` reading `MediaPlayer.texture()`
- `client/screens/VisionScreen.java` — full-screen player driven by an `MRL` → `MediaPlayer` state machine
- `VisionOverlay.java` — same state machine for the HUD overlay
- `common/util/MathHelper.java` — **new**, replaces the deleted `MathAPI`
- `WaterVision.java`, `client/screens/widgets/FadeBackground.java` — trimmed of removed API usage

## Known limitations

- **The bundled WATERMeDIA binaries ship GNU/Linux (`glibc`) FFmpeg builds only** — there is no
  Android/`bionic` variant. On Android the native libraries fail with
  `dlopen failed: library "libstdc++.so.6" not found`. WATERMeDIA catches this and keeps loading,
  so the game runs, but no video can be decoded. This is a packaging gap in WATERMeDIA upstream,
  not something this port can fix.
- `watermedia_android_bridge` (a 2.x-era artifact) must not be installed alongside 3.0 — it declares
  `versionRange="[2.1.36,3.0.0)"` and aborts startup.
- The animated loading GIF was provided by the removed `ImageAPI`. It is replaced by a simple
  pulsing indicator. This does not affect playback.

## Building

```bash
./gradlew build
```

The output jar is written to `build/libs/`.

The WaterMedia dependency is fetched from JitPack:

```groovy
compileOnly "com.github.SrRapero720:watermedia:$watermediaversion"
```

## Credits

- Original mod and WaterMedia API — **SrRapero720**
- WATERMeDIA 3.0 port — **dspqle**

## License

PolyForm Strict License 1.0.0 — see [LICENSE.md](LICENSE.md).