# RealCTS

Open Google’s native Circle to Search over the app you’re using when you invoke your Android assistant. Circle an image or tap text, then dismiss the overlay to return to your app.

**[Download RealCTS 1.3 for Android](https://github.com/itzSNSR/RealCTS/releases/download/v1.3/RealCTS-debug.apk)** · [Release notes](https://github.com/itzSNSR/RealCTS/releases/tag/v1.3) · [Device compatibility](COMPATIBILITY.md)

The APK installs on Android 9 and later and targets Android 16. The assistant shortcut below works entirely on the phone, with no ADB grant, root, or Shizuku. It requires a supported Google assistant panel and a working native Google search route.

## Set up the assistant shortcut

1. **Install the APK** on the phone. Open **Settings → Apps → Default apps → Digital assistant app** and choose **Google**. Keep Google selected for this setup.
2. Open **RealCTS**, leave **Trigger mode** on **Automatic**, and tap **Test Circle to Search**. Tap a word on screen to verify that Google can select it. If the overlay does not appear, check **Diagnostics** before continuing.
3. On the RealCTS dashboard, turn on **Open Circle to Search when Assistant opens**. Open Android **Accessibility** settings and enable **RealCTS Search screen**. Each phone needs its own setup.
4. Open another app and invoke your usual assistant shortcut. Google’s assistant panel may appear briefly. RealCTS recognizes the supported panel, dismisses it, and requests Google’s selection overlay over the original app.

To restore normal assistant behavior, turn off **Open Circle to Search when Assistant opens**. For direct access without the panel appearing first, use **Add Circle to Search tile** and tap the tile from Quick Settings.

### Samsung Galaxy

First check **Settings → Display → Navigation bar → Circle to Search**. On supported Galaxy models, enabling this setting lets you hold Home or the navigation handle to open Circle to Search directly. [Samsung’s Circle to Search guide](https://www.samsung.com/us/support/answer/ANS10000939/)

To try RealCTS with the Side button, follow the four steps above. On One UI versions that offer the choice, set **Settings → Advanced features → Side button → Long press → Digital assistant**. On the Galaxy M21 we verified a long press on the on-screen Home button in three-button navigation. The Side button was not tested. [Samsung’s assistant setup guide](https://www.samsung.com/us/support/answer/ANS10010644/)

## What has been verified

**Samsung Galaxy M21 (SM-M215F), Android 12, One UI 4.1.** With Google selected and assistant-panel mode enabled, holding the on-screen Home button opened the Google selection overlay over Display settings. The standard Android ASSIST key also worked. Tapping **Display** selected the text and offered **Copy** and **Translate**. Back returned to Display settings. USB supplied the test inputs; RealCTS had no secure-settings grant.

On a **Vivo V2437 running Android 16**, RealCTS 1.3 opened Google’s selection overlay over Display settings after the standard Android **ASSIST** key event. Google remained selected, the accessibility service was enabled, and RealCTS had no secure-settings grant. USB supplied the test key event and captured the result. A physical Home hold, spoken hotword, and other manufacturers remain unverified. The build passed 19 JVM tests. [Compatibility matrix and test details](COMPATIBILITY.md)

## If it doesn’t open

| What you see | What to check |
| --- | --- |
| **Candidate route · Unavailable** | Verify that the Google app is enabled and selected as digital assistant. Return to RealCTS and check **Google voice active** under **Diagnostics**. |
| **Test works, but Gemini stays open** | Check the dashboard switch and the RealCTS accessibility service. Verify that your gesture opens Google. Different panel implementations can require additional support. |
| **Request accepted, but no selection overlay** | Record what appeared in **Diagnostics**. Android can accept the request even when Google declines to display the overlay. |
| **Triggers paused** | After changing the setup, open **Diagnostics → Allow another manual test** and test again. |

When reporting a problem, include your model, Android and manufacturer software versions, Google app version, candidate route, last trigger result, and whether the dashboard button or assistant gesture failed. Crop personal information from screenshots. [Open an issue](https://github.com/itzSNSR/RealCTS/issues)

## Permissions and privacy

Assistant-panel mode listens for Google window-state changes and checks the panel’s class name. The accessibility service does not retrieve window contents or record audio. Google handles the selection and search. The RealCTS APK has no internet permission.

Advanced users can separately configure temporary voice-service switching with an ADB grant or a supported LSPosed integration. **Automatic** mode uses the available on-device routes. [Advanced setup, permissions, and recovery](SETUP.md)

## Build from source

Use JDK 17 and Android SDK Platform 36. From the project root on Windows, run

```bat
gradlew.bat assembleDebug
gradlew.bat testDebugUnitTest lintDebug
```

On macOS or Linux, use `./gradlew`. The installable artifact is `app/build/outputs/apk/debug/app-debug.apk`. The v1.3 download uses the Android debug signing configuration and package ID `com.realcts.app`.

## License

RealCTS adapts the native trigger from MiCTS and the assistant integration from CTSLauncher. The project is distributed under GPL-3.0-only. [Source credits and modifications](NOTICE.md) · [Full license](LICENSE)
