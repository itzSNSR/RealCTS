# RealCTS

RealCTS registers as an Android digital assistant and requests Google’s native Circle to Search over the foreground app. The APK installs from Android 9 (API 28) and targets Android 16 (API 36). The Google app, Android framework configuration, and gesture routing on the phone determine what happens. The assistant-panel route was verified on Vivo V2437, Android 16. See COMPATIBILITY.md for the test scope.

## Install and try it

1. Install the supplied `RealCTS-debug.apk` from Files, or run `adb install -r RealCTS-debug.apk` from its directory.
2. Open RealCTS and select **Guided setup**. If holding Home or the navigation handle already opens Google’s selection overlay, keep the existing assistant configuration.
3. Follow the assistant choice shown on the dashboard. Choose Google for Direct mode, or RealCTS for the gesture when CSHelper or SearchScreen is available. If the role prompt is unavailable, use Android Settings → Apps → Default apps → Digital assistant app. Labels depend on the firmware.
4. Read **Candidate route** in the dashboard. Leave the trigger mode on **Automatic** for the first test. A direct voice request requires Google’s voice service to be active. Select **Setup without a computer**. On Android 11 or newer, the optional **RealCTS Search screen** accessibility service checks for the native launcher action. If no route is available, keep Google as the default assistant and test the RealCTS tile.
5. Tap **Test Circle to Search**. Check that you can circle text and dismiss Google’s overlay back to the original screen. In **Diagnostics**, record what actually appeared. Reporting a missing overlay or different interface pauses subsequent requests until you choose **Allow another manual test**.
6. Open another app and invoke the gesture assigned to the digital assistant. Use the Quick Settings tile or the launcher’s RealCTS long-press shortcut for a separate entry test. Each entry calls the same trigger service.

## Optional ADB permission

Use this only for the **Switch** route. With USB debugging authorized, run

```shell
adb shell pm grant com.realcts.app android.permission.WRITE_SECURE_SETTINGS
```

Then turn on **Configure permissions → Allow temporary Google voice-service switching**. Both the grant and this switch are required. Android presents no ordinary runtime dialog for this permission. Without it, configured helper, privileged module service, and already-active Google voice routes can still be eligible.

RealCTS writes only the current profile’s `voice_interaction_service` setting. It journals the exact previous value, including absent and empty values, and the observed assistant setting before changing anything. The assistant role is selected by the user in Android settings. RealCTS checks the current settings before restoration and preserves a visible user change. Android provides no cross-app atomic compare-and-set for these settings. If a permission is revoked mid-switch or a ROM prevents recovery, reopen RealCTS, check the ADB grant, use **Advanced settings → Recover saved voice service**, and verify Android’s assistant picker.

Finish recovery and turn off switching before revoking the grant.

```shell
adb shell pm revoke com.realcts.app android.permission.WRITE_SECURE_SETTINGS
```

## Native trigger routes

| Route | Required evidence | What RealCTS does |
|---|---|---|
| CSHelper | Android framework search key, Google provider, a matching factory/system Google activity, and a configurable legacy fallback | Sends the native contextual search bundle through the voice interaction manager and disables the legacy voice fallback. The optional scoped module can make the same request from System UI. |
| CSService | User-enabled LSPosed API 101-compatible module in System UI, a responding receiver in this user profile, Google configured as the system provider, a matching Google system activity, and System UI’s existing contextual-search permission | Calls the framework contextual search manager for the navigation-handle entry point. The service performs its normal permission check. |
| SearchScreen | User-enabled accessibility service on API 30+, advertised Launcher3 Search screen action (ID 500), and Google configured as the framework search provider | Rechecks the action before asking Android to perform it. Requests no window content, gesture injection, screenshots or event stream. |
| Direct VIS | Google voice service already active, with no conflicting configured provider | Sends the MiCTS native entry-point bundle using CTSLauncher’s Binder transport. |
| Switch VIS | Same activity evidence, discovered Google voice service, explicit switching opt-in, and ADB grant | Creates a recovery record, switches, checks the active service where possible, dispatches the VIS request, then restores. |

Automatic mode uses the stock helper, an available native Search screen action, or already-active Google voice service in that order. It never selects temporary switching or LSPosed. Advanced users can explicitly select VIS for authorized switching, or CSHelper / CSService for module integration. Selecting a mode manually constrains the request to that mode. A rejected request produces a diagnostic without starting a second search UI. Feature detection uses runtime evidence. Manufacturer and model appear only in diagnostics.

For module use, enable RealCTS with **System UI** scope in a compatible LSPosed manager, reload the scope, then enable **Check optional LSPosed System UI integration** in Advanced settings. The app probes helper and service availability separately. A framework permission denial or missing provider remains an unsupported configuration. RealCTS does not change the framework provider, spoof a phone model, or alter Google flags. The narrower module therefore covers fewer configurations than upstream MiCTS. In particular, it cannot create a contextual-search service that the ROM omitted.

## Troubleshooting

- **Inactivity after the gesture.** Compare the last request timestamp before and after invoking the assistant over a different application. If it did not advance and the dashboard test did, check the selected digital assistant and the phone’s navigation settings. A gesture dedicated to an OEM assistant or to built-in Circle to Search can bypass the assistant role.
- **Request accepted but the overlay is missing.** The framework can accept the bundle before the Google app decides what to display. VIS can yield a generic Google assistant screen on some devices. Record this as a failed visual test in Diagnostics. The optional accessibility service cannot read window content or capture the screen. Visual confirmation is required. Its code launches no Lens, Gemini, browser, or third-party search intent.
- **No eligible route.** Check that the Google app is enabled in the current Android user/profile. Review Diagnostics for service, activity and helper detection. A Google package alone is insufficient evidence for an attempt. Some Google builds can conceal a usable native entry point from these conservative checks.
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



## Assistant-panel mode

On supported Google assistant builds, leave Google selected as digital assistant, use Automatic mode, enable **Open Circle to Search when Assistant opens** on the dashboard and enable **RealCTS Search screen** in Android Accessibility. Invoking the assistant can briefly show its panel. RealCTS recognizes the Google FloatyActivity window, dismisses it, and requests native search on the previous screen. This mode only subscribes to Google window-state changes while opted in, and does not retrieve window contents. Disable the switch to resume normal assistant behavior. Different assistant implementations and voice-only invocations may not show the recognized panel.
