# Compatibility and test matrix

The APK declares minimum API 28 and target API 36. Runtime success requires observing Google’s native selection overlay, searching an item, dismissing the overlay back to the same app, and checking the saved assistant afterward.

| Manufacturer | Android versions in scope | Assistant registration / gesture | Native overlay / restoration |
|---|---|---|---|
| Samsung Galaxy M21 (SM-M215F) | 12 | ASSIST key and on-screen Home hold verified with Google selected | Native overlay, text selection and return to Display settings verified |
| Other Samsung One UI / Core | 10–16 | Not tested | Not tested |
| Motorola | 10–16 | Not tested | Not tested |
| Vivo V2437 | 16 | ASSIST key event verified with Google selected | Native overlay verified; temporary switching untested |
| Other Vivo / iQOO | 10–16 | Not tested | Not tested |
| OPPO / Realme | 10–16 | Not tested | Not tested |
| OnePlus | 10–16 | Not tested | Not tested |
| Xiaomi / Redmi / POCO | 10–16 | Not tested | Not tested |
| Google Pixel | 10–16 | Not tested | Not tested |
| Nothing, HMD / Nokia, ASUS, Sony, Lenovo, Tecno, Infinix and other Android | 9–16 | Not tested | Not tested |

Vivo V2437 on Android 16 was tested separately below. “Verified working” and “verified failing” require a device observation with the exact build and mode. “Unsupported” applies to a specific missing platform prerequisite, such as installation below API 28, a missing Google app, CSService without an authorized framework endpoint, or switched VIS without its ADB grant. No manufacturer-wide failure is inferred.

## Gesture differences

- Samsung documents Home/navigation-handle hold for native Circle to Search on supported models, with assistant gesture behavior depending on navigation and Side button settings. Check both the system assistant choice and the configured gesture. [Samsung assistant settings](https://www.samsung.com/us/support/answer/ANS10001575/)
- Motorola documents a **Swipe to invoke assistant** switch under gesture navigation for the edge (2024). Confirm the equivalent setting on the test model. [Motorola gesture guide](https://en-us.support.motorola.com/app/answers/detail/a_id/180165/)
- Vivo/iQOO, OPPO/Realme/OnePlus and Xiaomi firmware can expose different navigation and assistant choices by region. If the system does not dispatch ASSIST for the selected gesture, use the tile to test the trigger independently. RealCTS does not remap Jovi, an OEM power button, or a launcher’s private gesture.
- On Pixel and other phones with the existing feature, try the built-in Home/navigation-handle hold before changing assistant. Android exposes no public API for verifying Google’s per-device eligibility or a successful overlay. [Google’s Circle to Search instructions](https://support.google.com/websearch/answer/14508957)

## Manual evidence to record

Record the model, firmware build, Android API, Google app version, default assistant, enabled routes, and pre-test voice component. Test the dashboard and gesture separately. Confirm the selection interaction, original foreground app after dismissal, repeated invocations, denial with the grant absent, and restoration after a successful or interrupted switch. Record observed and expected outcomes independently. A screenshot of the app dashboard or an accepted Binder request alone does not establish overlay success.

## Verified device result · Vivo V2437, Android 16

With RealCTS 1.3 in Automatic mode, Google selected as assistant, RealCTS Search screen enabled in Accessibility and assistant-panel mode on, the standard Android ASSIST key event opened the native Google selection overlay over Display settings. USB automated the test key press and captured the screenshot. The runtime route used no ADB grant, root, Shizuku or voice-service switch. A physical Home hold, spoken hotword and other assistant panel implementations remain unverified.

## Verified device result · Samsung Galaxy M21, Android 12

Tested RealCTS 1.3 on SM-M215F with Google app 17.72.17.ve.arm64 and three-button navigation. Google was the active voice service, RealCTS Search screen was enabled in Accessibility, and assistant-panel mode was on. The app did not have WRITE_SECURE_SETTINGS.

From Display settings, the standard ASSIST key opened the native Google selection overlay. Tapping the Display heading selected its text and offered Copy and Translate. Back returned to the same settings activity. A second invocation using a one-second touch at the on-screen Home button opened the overlay again and recorded a new RealCTS Direct request. USB automated the input. The hardware Side button and voice hotword were not tested.
