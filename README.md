# Holive (Android Live Demo)

## 1. How to run

1. Use Android Studio Giraffe+ (or command line) and JDK 17.
2. Sync Gradle.
3. Run `app` on Android 7.0+ device/emulator.

Command line:

```bash
./gradlew :app:assembleDebug
```

## 2. Switch Mock / Real environment

- Real API is enabled by default:
  - `BuildConfig.BASE_URL = http://api.hclyz.com:81/`
- You can switch by editing `app/build.gradle.kts`:
  - Change `BASE_URL` to your own server.
- Playback URL strategy:
  - API currently returns many `rtmp://` streams.
  - ExoPlayer demo uses HLS as primary protocol.
  - If stream is not HLS, app falls back to public HLS demo URL for runnable playback.

## 3. Protocol notes

- Play protocol: **HLS** (Media3 ExoPlayer HLS module).
- Push protocol: **RTMP** (upstream side, not implemented in this watch-only demo).
- DRM support:
  - Detail player builds `MediaItem.DrmConfiguration` when `drmLicenseUrl` exists.

## 4. FAQ / Troubleshooting

### Permission issue

- Android 13+ requires notification runtime permission (`POST_NOTIFICATIONS`).
- Camera/Microphone permissions are requested in homepage banner.

### Network issue

- App listens to `ConnectivityManager` via `ConnectivityObserver`.
- Detail page retries automatically after connectivity returns.

### Playback failed

1. Check room URL is HLS (`.m3u8`).
2. If source is RTMP, current demo cannot play it directly in ExoPlayer.
3. Confirm server allows HTTPS and CORS/CDN policy for your test URL.
4. Check Logcat in debug build (`ENABLE_HTTP_LOG=true`).

## 5. Code quality

- `ktlint` plugin added (format/lint baseline config in `.editorconfig`).
- `detekt` plugin added with sample `detekt.yml`.

Run:

```bash
./gradlew ktlintCheck detekt test
```
