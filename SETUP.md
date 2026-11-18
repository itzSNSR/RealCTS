# RealCTS setup guide

Start with the phone-only method. The success check is simple: open another app, invoke the assistant, select something on the original screen, then use Back to return. Installation is supported from Android 9. The routes available after installation depend on the phone, launcher and Google app.

## Install on the phone

1. On the phone, open the [RealCTS release](https://github.com/itzSNSR/RealCTS/releases/tag/v1.3). Under **Assets**, download **RealCTS-debug.apk**.
2. Open Downloads and tap the APK. If Android asks, allow installation from the app opening the file, return, and tap **Install**. You can turn that installation permission off afterward.
3. Open RealCTS. On the dashboard, check **Google app · Detected**. If it is missing, enable or install the Google app in this Android profile, update it through Google Play where available, and reopen RealCTS. If a work or family policy blocks installation or accessibility, check with the device administrator.

## Setup without a computer

1. Open Android **Settings**, search for **Digital assistant app**, then choose **Google**. A common path is **Apps → Default apps → Digital assistant app**. Some phones ask you to tap the current assistant a second time to open the list.
2. Open RealCTS. Tap **Advanced settings → Trigger mode → Automatic** and return to the dashboard. **Setup without a computer** also takes you to the relevant guided step.
3. Tap **Test Circle to Search**. Tap a word in the overlay and press Back. If the test fails, open **Diagnostics** before configuring a gesture.
4. For the Google-panel method, keep Google selected and turn on **Open Circle to Search when Assistant opens** when offered on the dashboard. Enable **RealCTS Search screen** in Android Accessibility. On v1.3 the dashboard can instead offer **Set RealCTS as assistant** if it detects an independent native route. You can use that route for an OS-assigned assistant gesture.
5. Open Settings or another ordinary app. With three-button navigation, hold the **Home** button. With gesture navigation, try the phone’s configured assistant gesture, such as swiping from a bottom corner. Check that the overlay lets you select something from that app. Dismiss it with Back.
6. If the manual test works and Gemini stays open, check the panel switch, **Automatic**, and the enabled accessibility service. If the launcher routes the gesture elsewhere, try the RealCTS Quick Settings tile to isolate the gesture from the trigger.

This setup requires a compatible Google panel for the panel shortcut and a working native route. It needs no PC, ADB, Shizuku or root. **Default assistant · Inactive** is expected when Google holds the role; that status describes RealCTS.

## Enable accessibility

Open **Settings → Accessibility**, then **Installed apps**, **Downloaded apps**, or **Installed services**. Choose **RealCTS Search screen**, enable the service, and review Android’s confirmation. Return to RealCTS. If Android restricts a sideloaded service, review the phone’s notice and the app’s **App info** settings. Managed phones can limit this choice.

The service has two uses. SearchScreen checks for an advertised system action. Separately, assistant-panel mode listens for window-state changes from the Google app and recognizes a specific panel class. Neither use retrieves window contents. Turning off the panel switch stops panel monitoring. Turning off the Android service also removes access to the SearchScreen action.

## Setup with a computer

Follow this only for manual **VIS** with temporary switching. Install RealCTS first.

1. On Android, open **About phone** and tap **Build number** seven times (on Samsung, look under **Software information**). Confirm your PIN if prompted. Go back to Settings, open **Developer options**, and enable **USB debugging**.
2. Download **SDK Platform Tools** for your computer and extract the archive. No Android Studio or emulator is required. [Official Windows, macOS and Linux downloads](https://developer.android.com/tools/releases/platform-tools)
3. **Windows:** Open the extracted `platform-tools` folder in PowerShell. Use `.\adb.exe` in place of `adb` in each command below. If Windows fails to recognize the phone, install its manufacturer’s USB driver. **macOS:** Open Terminal in the extracted folder and use `./adb`. **Linux:** Do the same; if ADB reports insufficient USB permissions, configure the appropriate udev rules for your distribution. If Platform Tools is already on your PATH, use `adb` as written. [Android USB setup](https://developer.android.com/studio/run/device)
4. Connect the unlocked phone with a data-capable USB cable. Run the following, accept the **Allow USB debugging** prompt on the phone, then run it again.

```shell
adb devices
```

5. Continue when the intended phone is listed as **device**. For **unauthorized**, unlock it and accept the prompt. For an empty list, check the cable, port, USB debugging, and OS-specific driver or permissions. If several devices appear, disconnect the others or insert `-s SERIAL` immediately after `adb` in each device command.
6. Grant the optional permission to the installed RealCTS package on this phone.

```shell
adb shell pm grant com.realcts.app android.permission.WRITE_SECURE_SETTINGS
```

7. Reopen RealCTS and check **Secure settings · Granted**. Follow the switching instructions below. This grant belongs to that installed package on that phone. Sharing the APK does not transfer it. Normal in-place updates with the same package and signing key generally retain it. Uninstalling removes it.

## Temporary switching and recovery

1. Select **Advanced settings → Trigger mode → VIS**. Open **Configure permissions** and turn on **Allow temporary Google voice-service switching**. Both the device grant and the in-app opt-in are required. Selecting **Automatic** never enables switching.
2. To have the OS launch RealCTS on the assistant gesture, choose **Set RealCTS as assistant** and select RealCTS in Android. With another voice service active or none selected, look for **Candidate route · Switch**. If Google is already active, **Direct** is expected.
3. Tap **Test Circle to Search**, dismiss the overlay and verify the original digital assistant in Android settings. Then test from another app. RealCTS records the previous voice-service value before writing and checks ownership before restoring. A persisted job and the next app launch can retry an interrupted restoration; execution timing depends on Android.
4. Before revoking the grant, finish the active test. Check **Diagnostics → Pending voice-service restoration**. If true, keep the grant, open **Advanced settings → Recover saved voice service**, and verify the intended assistant in Android Settings. Turn off temporary switching and run

```shell
adb shell pm revoke com.realcts.app android.permission.WRITE_SECURE_SETTINGS
```

If you already revoked access mid-switch, restore the grant long enough to run recovery, or manually reselect your intended assistant. Avoid clearing app data during recovery because it holds the saved value.

## Compare modes

Choose **Advanced settings → Trigger mode**. All routes require an enabled Google app. “Home” below means a Home hold or gesture that the OS actually assigns to the assistant. A manufacturer can reserve a gesture for a different action.

| Mode | Android requirement | Default assistant / voice service | Accessibility | PC / ADB | Root / LSPosed | Home gesture and limits |
| --- | --- | --- | --- | --- | --- | --- |
| Automatic | App minimum 9; each route has its own checks | Google for panel method; RealCTS may hold role for independent routes | Panel or SearchScreen only | No | No | Panel handoff or OS dispatch to RealCTS; requires a working candidate |
| VIS · Direct | 9–16 in this build | Active Google voice service | No | No | No | Tile or test directly; for a Google-panel gesture, use Automatic |
| VIS · Switch | 9–16 | Another assistant may be selected; Google voice service must be discoverable | No | Yes, one-time grant per installation | No | OS must dispatch to RealCTS; switching and restoration depend on firmware |
| CSHelper | Framework helper, generally Android 14 QPR3+; current client checks through API 36 | Firmware provider must be Google; role can be RealCTS | No | No app ADB grant | Optional for module path | OS dispatch to RealCTS; firmware must expose required resources and Google activity |
| CSService | System contextual search manager, normally Android 15+; module loads from API 34 | Google configured as provider; role can be RealCTS | No | No app ADB grant | Yes | OS dispatch to RealCTS; requires privileged System UI endpoint |
| SearchScreen | Android 11+ system actions; route checked through API 36 | Google configured as framework provider; role can be RealCTS | Yes | No | No | OS dispatch to RealCTS; launcher must advertise Search screen |

### Automatic

Select **Automatic** in the mode picker. RealCTS tries the configured stock helper, the available Search screen system action, then direct Google voice service eligibility, in that order. It selects one candidate before dispatch. Automatic never invokes temporary switching or a privileged module service. Test on the dashboard first. If the candidate is unavailable, select Google as digital assistant and recheck **Diagnostics → Google voice active**.

### VIS

Select **VIS** in the mode picker. **Direct** requires Google’s voice interaction service to be active and no conflicting configured search provider. Keep Google as digital assistant and use the Test button or Quick Settings tile. No PC, root or accessibility service is required. A blocked private framework interface or Google eligibility can still prevent the overlay.

**Switch** temporarily selects the discovered Google voice service, sends the native request, then restores the recorded value. Use the computer and recovery steps above if you need RealCTS to receive the OS gesture. If **Switch** is unavailable, check the Google component, visible native activity, grant and in-app toggle in Diagnostics or Configure permissions. Do not grant ADB access for a working **Direct** route.

### CSHelper

Select **CSHelper** in the picker. RealCTS checks the framework search key, Google provider, a configurable legacy fallback and a matching system Google activity. When supported, the voice manager dispatches to the native helper independently of the default Google voice service. Try the dashboard test without root first. If **Firmware helper configured** is false, see the optional module setup below. A missing firmware helper can remain unavailable even with a module.

### CSService

Select **CSService** in the picker and complete **Configure LSPosed** below. System UI must have its existing contextual-search permission, Google must be configured as system provider, and the manager and Google system activity must exist. Test only after **Module service** reports ready. If it reports a missing manager, wrong provider or denied permission, choose another eligible mode. The secure-settings ADB grant does not provide this permission.

### SearchScreen

Select **SearchScreen** in the picker. On Android 11 or newer, enable **RealCTS Search screen** in Android Accessibility and reopen RealCTS. With the Google provider configured, the service checks Android’s current action list for the launcher's Search screen action (ID 500). Look for **Candidate route · SearchScreen**, then test. Accessibility being enabled by itself does not create the action. If unavailable, try Automatic with Google selected. This mode needs neither a PC nor root.

### Assistant-panel mode

**Open Circle to Search when Assistant opens** is a separate dashboard switch. Leave the trigger mode on **Automatic**, keep Google’s voice service active, and enable Accessibility. When the supported Google panel appears on an unlocked screen, RealCTS dismisses it and requests search over the preceding app. The panel can flash briefly. A different panel class, a spoken hotword without a panel, or a reserved gesture may not trigger the handoff. Disable the switch to resume normal assistant behavior.

## Configure LSPosed

1. Use a device already configured with a compatible rooted LSPosed environment supporting module API 101. The RealCTS System UI module loads on Android 14+; the underlying service or helper must also exist.
2. Enable **RealCTS** in your module manager and select **System UI** as its scope. Reload the scope using the manager’s supported restart procedure.
3. In **Advanced settings**, explicitly choose **CSHelper** or **CSService** and enable **Check optional LSPosed System UI integration**. Return to Advanced settings or Diagnostics and read **Module helper** and **Module service** separately.
4. Test an eligible route on the dashboard before configuring Android to use RealCTS as assistant. The module checks the current user’s System UI response and respects framework permissions. It cannot supply an omitted provider, bypass a permission denial, or turn on a feature Google has disabled.

## Test the native overlay

Use an unlocked, ordinary screen such as Display settings. First tap **Test Circle to Search** in RealCTS. Look for Google’s selection UI over the current screen, select a visible word or circle an item and press Back. Repeat over another app with your assistant gesture. In **Diagnostics**, record what appeared. An accepted request proves dispatch only. Reporting a different interface or no overlay pauses requests until you select **Allow another manual test**.

## Device settings

Menu names vary by OS release, navigation mode, region and launcher. The terms below are places to look or search for in Android Settings. Only the models listed in the [test matrix](COMPATIBILITY.md) have device evidence.

| Phone | Where to look |
| --- | --- |
| Samsung One UI / Core | **Apps → Choose default apps → Digital assistant app**. Check **Display → Navigation bar** for native Circle to Search. Compatible Side button assignments are under **Advanced features**. |
| Motorola | Look under **Apps → Default apps** and **Gestures → System navigation**. Check the gesture-navigation options for **Swipe to invoke assistant**. |
| Vivo / iQOO | Search **Default apps** or **Digital assistant app**, then **System navigation**. Check whether the shortcut opens Google or an OEM panel. |
| Realme | Search **Default apps**, then **System navigation** or **Gestures**. Verify which app the configured gesture opens. |
| OPPO | Look under **Apps → Default apps** where available. Search **System navigation** and **Power button** for gesture assignments. |
| OnePlus | Search **Default apps** and **System navigation**. For a button gesture, also search **Power button**. |
| Xiaomi / Redmi / POCO | Search **Default apps** or **Assist & voice input**. Check **Gesture shortcuts** or **System navigation** for launcher-specific assignments. |
| Nothing OS | Look under **Apps → Default apps**. Search **Gestures** and **Navigation mode** for the current navigation controls. |
| Google Pixel | Look under **Apps → Default apps**, then search **Circle to Search** and **Navigation mode**. Try the built-in Home or navigation-handle hold first. |
| Other Android phones and tablets | Search Settings for **Digital assistant app**, **Assist**, **Accessibility** and **System navigation**. Use the tile to test independently of a reserved gesture. |

## Troubleshooting by symptom

| Symptom | Check and next step |
| --- | --- |
| VIS needs Google active | Read **Google voice active** in Diagnostics. Select Google in Android default apps, unlock and retry Direct. For an intentional temporary switch, follow the manual VIS procedure. |
| Switching permission missing | Check **Secure settings**, the **Allow temporary…** toggle and **VIS** mode. Recheck that ADB targeted the same phone and installed package. |
| Gemini opens; manual test works | Confirm **Automatic**, Google active, panel switch enabled and the accessibility service enabled. Check whether the last-trigger timestamp changes after the gesture. A different Google panel can be unrecognized. |
| Accessibility disabled or stopped | Check Android Accessibility for the service. Re-enable it if stopped, reopen RealCTS and retry. Check per-app battery or background restrictions if this recurs. |
| SearchScreen unavailable | Verify API 30+, active service, Google framework provider and candidate route. Use Direct if the launcher omits the action. |
| CSHelper unavailable | Check **Firmware helper configured**; for explicit module use, check **Module helper**. Return to Automatic or Direct when neither path is eligible. |
| CSService unavailable | Check **Module service**, module scope, framework API and selected mode. ADB switching permission cannot replace System UI access. |
| Holding Home does nothing | Check three-button versus gesture navigation, the assistant selected in Android, and whether the OS reserves Home. Compare the dashboard timestamp before and after and test the configured assistant gesture. |
| Native request accepted; no overlay | Try a non-sensitive screen, update Google, and check Android’s assistant screen-data settings. Record the actual visual result in Diagnostics. |
| Android blocks background invocation | Run a dashboard test while unlocked. Check the phone’s background restrictions for RealCTS and Google, then retry the gesture over Settings. |
| Google app missing, disabled or outdated | Check the current Android profile in **Settings → Apps → Google**. Enable or update Google, then reopen RealCTS to refresh detection. |
| Restoration pending | Keep the grant, reopen RealCTS, run **Recover saved voice service** and check Android’s assistant picker. Use the recovery procedure above before revoking access. |

### Recovery details and diagnostic logs

RealCTS writes only the current profile’s `voice_interaction_service` setting. It journals the exact previous value, including absent and empty values, and the observed assistant setting before changing anything. The assistant role is selected by the user in Android settings. RealCTS checks the current settings before restoration and preserves a visible user change. Android provides no cross-app atomic compare-and-set for these settings. If a permission is revoked mid-switch or a ROM prevents recovery, reopen RealCTS, check the ADB grant, use **Advanced settings → Recover saved voice service**, and verify Android’s assistant picker.

- **Temporary switch rejected.** Check both the ADB grant and the in-app switch. Some firmware restricts writes to secure settings. RealCTS stops if it detects a change of assistant during setup. When the active-service probe is inaccessible, a 250 ms settling interval and settings readback are the available checks. The 1 s post-request window follows CTSLauncher. Neither proves that Google finished showing the overlay.
- **Interruption while switching.** A persisted one-shot recovery job is scheduled before the write. Android controls its execution time. Reopening RealCTS also checks for a transaction. Force-stop, app data removal, and OEM job restrictions can prevent or defer automatic recovery. Manually reselect the intended assistant in settings if necessary.
- **Report a failure.** Include the phone model, Android build, Google app version, selected mode, dashboard vs. gesture result, and Diagnostics text. For additional local logging, turn on **Advanced settings → Write local diagnostic messages to Logcat** and run `adb logcat -s RealCTS`. Verify what screen appeared before marking a device as working.

## Build

Use JDK 17, Android SDK Platform 36, and Gradle’s Android build tools. Open this directory in Android Studio or run the wrapper from the project root.

```bat
gradlew.bat assembleDebug
gradlew.bat testDebugUnitTest lintDebug
```

The debug artifact is `app/build/outputs/apk/debug/app-debug.apk`, package `com.realcts.app`. The distributed copy is named `RealCTS-debug.apk`. Release signing is optional. To build a signed release set `REALCTS_KEYSTORE` to the keystore path, and supply `REALCTS_STORE_PASSWORD`, `REALCTS_KEY_ALIAS`, and `REALCTS_KEY_PASSWORD` via your build environment, then run `gradlew.bat assembleRelease`. Keep signing credentials outside the project.

## Source and licensing

This project is distributed under GPL-3.0-only. See [LICENSE](LICENSE), [source attribution and modifications](NOTICE.md), and the [compatibility and test matrix](COMPATIBILITY.md).
