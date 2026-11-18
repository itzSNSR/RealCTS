# RealCTS

Open Google’s native Circle to Search over the app you’re using when you invoke your Android assistant. Circle an image or tap text, then dismiss the overlay to return to your app.

**[Download RealCTS 1.3 for Android](https://github.com/itzSNSR/RealCTS/releases/download/v1.3/RealCTS-debug.apk)** · [Release notes](https://github.com/itzSNSR/RealCTS/releases/tag/v1.3) · [Device compatibility](COMPATIBILITY.md)

The APK installs on Android 9 and later and targets Android 16. The assistant shortcut below works entirely on the phone, with no ADB grant, root, or Shizuku. It requires a supported Google assistant panel and a working native Google search route.

## Download and install

Download **RealCTS-debug.apk** from the release link above. Open it from Downloads, allow the file manager or browser to install it if Android asks, then tap **Install → Open**. RealCTS needs Android 9 or newer and an enabled Google app. [Installation details](SETUP.md#install-on-the-phone)

## Setup without a computer (recommended)

1. Choose **Google** in Android **Settings → Apps → Default apps → Digital assistant app**.
2. Open RealCTS. Under **Advanced settings → Trigger mode**, choose **Automatic**. On the dashboard, tap **Test Circle to Search** and try selecting a word.
3. If the test works, enable **Open Circle to Search when Assistant opens** on the dashboard when offered. Enable **RealCTS Search screen** in Android Accessibility. That separate switch connects a supported Google assistant panel to your working search route.

Google stays selected. This path needs no ADB, root, or Shizuku. If your phone exposes an independent native route, the dashboard can also offer **Set RealCTS as assistant**. [Complete phone-only walkthrough](SETUP.md#setup-without-a-computer)

## Setup with a computer (optional)

Use this path if you specifically want RealCTS selected as assistant and need its manual **VIS** mode to switch temporarily to Google. Install Android Platform Tools, authorize USB debugging on your phone, and grant the optional secure-settings permission. Automatic mode never enables this switch. [Windows, macOS and Linux instructions](SETUP.md#setup-with-a-computer)

## Make long-press Home open Circle to Search

Open another app and hold the on-screen **Home** button in three-button navigation. With gesture navigation, invoke the gesture your phone assigns to the digital assistant. A Google panel may appear briefly before the selection overlay. If your phone already offers native Circle to Search on its navigation handle, you can use that shortcut directly. [Navigation and device settings](SETUP.md#device-settings)

## Choose a trigger mode

| Mode | When to choose it |
| --- | --- |
| Automatic | Start here. Checks the firmware helper, accessibility search action and active Google voice service. |
| VIS | Use Google’s voice service directly, or explicitly authorize a temporary switch. |
| CSHelper | The firmware has a compatible Google contextual-search helper. |
| CSService | A compatible LSPosed System UI module can reach the system search service. |
| SearchScreen | Android 11+ advertises the native Search screen accessibility action for Google. |

Each selection is under **Advanced settings → Trigger mode**. **Candidate route** shows what the app could use on this phone. [Mode comparison and requirements](SETUP.md#compare-modes)

## Enable Accessibility

Open Android **Settings → Accessibility → Installed apps** (sometimes **Downloaded apps**). Tap **RealCTS Search screen** and turn it on. Use this for the assistant-panel shortcut or the SearchScreen route. [Accessibility instructions](SETUP.md#enable-accessibility)

## Enable temporary assistant switching

After granting permission on this phone, choose **VIS** and turn on **Configure permissions → Allow temporary Google voice-service switching**. Finish any pending restoration before revoking access. [Switching and recovery](SETUP.md#temporary-switching-and-recovery)

## Configure LSPosed

On an eligible rooted device with a compatible framework, enable RealCTS for **System UI**, reload that scope, choose **CSHelper** or **CSService**, then enable the module check in **Advanced settings**. The phone must report an eligible route. [Module setup and limitations](SETUP.md#configure-lsposed)

## Test the native overlay

Tap **Test Circle to Search**, circle or tap a word, verify that Google lets you select it, and press Back. Repeat over another app with the assistant gesture. A recorded request alone cannot verify what appeared on screen. [Test and record the result](SETUP.md#test-the-native-overlay)

## Troubleshooting and supported devices

### Samsung Galaxy

First check **Settings → Display → Navigation bar → Circle to Search**. On supported Galaxy models, enabling this setting lets you hold Home or the navigation handle to open Circle to Search directly. [Samsung’s Circle to Search guide](https://www.samsung.com/us/support/answer/ANS10000939/)

To try RealCTS with the Side button, follow the four steps above. On One UI versions that offer the choice, set **Settings → Advanced features → Side button → Long press → Digital assistant**. On the Galaxy M21 we verified a long press on the on-screen Home button in three-button navigation. The Side button was not tested. [Samsung’s assistant setup guide](https://www.samsung.com/us/support/answer/ANS10010644/)

### What has been verified

**Samsung Galaxy M21 (SM-M215F), Android 12, One UI 4.1.** With Google selected and assistant-panel mode enabled, holding the on-screen Home button opened the Google selection overlay over Display settings. The standard Android ASSIST key also worked. Tapping **Display** selected the text and offered **Copy** and **Translate**. Back returned to Display settings. USB supplied the test inputs; RealCTS had no secure-settings grant.

On a **Vivo V2437 running Android 16**, RealCTS 1.3 opened Google’s selection overlay over Display settings after the standard Android **ASSIST** key event. Google remained selected, the accessibility service was enabled, and RealCTS had no secure-settings grant. USB supplied the test key event and captured the result. A physical Home hold, spoken hotword, and other manufacturers remain unverified. The build passed 19 JVM tests. [Compatibility matrix and test details](COMPATIBILITY.md)

### If it doesn’t open

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
