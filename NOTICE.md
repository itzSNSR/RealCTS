# Source attribution

RealCTS is distributed under the GNU General Public License version 3. The complete license accompanies the source.

- **MiCTS** by parallelcc and contributors. Inspected branch `main`, commit `2ead158104bdb8605cbb5eae39a30d44db71bab6`. RealCTS adapts the contextual entry point, invocation timestamp, flags, API-34 signature split, framework resource detection, and contextual service selection from the trigger and hook implementations. [MiCTS source at reviewed revision](https://github.com/parallelcc/MiCTS/tree/2ead158104bdb8605cbb5eae39a30d44db71bab6)
- **CTSLauncher** by Andrew Gunnerson. Copyright 2025 Andrew Gunnerson for the adapted Java implementation and manifest. Inspected branch `master`, commit `9e743892477d539beb9399428f1cf658dc526302`. Derived components include the no-display assistant activity, Binder transaction, Google service discovery, authorized temporary switch, delayed restoration, and version-aware Quick Settings tile. [CTSLauncher source at reviewed revision](https://github.com/chenxiaolong/CTSLauncher/tree/9e743892477d539beb9399428f1cf658dc526302)
- AndroidX and Material Components for Android supply dashboard widgets and Android compatibility libraries under Apache 2.0. The Gradle wrapper is distributed by the Gradle project under Apache 2.0. The optional module compiles against the libxposed API and obtains the API at runtime from the installed framework.

## RealCTS changes, 2026

RealCTS adds its own package, assistant role selection, setup wizard, diagnostic dashboard, capability-based routing, manual modes, a process-wide worker, settings journal, persisted recovery job, and unit tests. It translates the Kotlin trigger to Java and combines it with CTSLauncher’s transport. The native VIS payload uses entry point 1 and flags 7. RealCTS uses its caller attribution and omits the upstream module-routing marker so a coinstalled MiCTS hook cannot select a different strategy on its behalf.

The optional module is a separate System UI-scoped implementation. It checks Google configuration and exposes fixed helper and service operations through a signature-protected receiver. Normal platform permission checks and secure-screen handling remain in Android’s framework. Upstream device spoofing, system-wide permission interception, and interception of OEM Home key events are excluded.

Reviewed MiCTS files included MainActivity, VIMSHooker, CSMSHooker, InvokeOmniHooker, LongPressHomeHooker, XposedConfig, ModuleMain, the manifest and build scripts. Reviewed CTSLauncher files included CtsLauncher, CtsImmediateActivity, CtsDelayedActivity, CtsTileService, AssistantSwitcher, AssistantRestoreService, the manifest and build scripts.
