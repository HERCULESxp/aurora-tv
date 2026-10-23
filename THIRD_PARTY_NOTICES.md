# Homestead attribution

Aurora TV 0.3.0 is distributed under **GPL-3.0-or-later**; see LICENSE.

The HOME mechanism is adapted from [Sarg338/Homestead](https://github.com/Sarg338/Homestead), Copyright (C) 2026 Homestead contributors, GPL-3.0-or-later:

- `HomeKeyWatcher.java`: separate-process log observer and Messenger protocol; parser extracted into `HomeLogParser`, with strict rejection of invalid timestamps.
- `HomeRedirectService.java`: accessibility overlay, early HOME notification, delayed activity handoff, foreground UID check and temporary settings suspension. Reworked for Aurora's UI, bounded lifecycle and standalone session model.
- `BootReceiver.java`: delayed opening after the home stack starts; restricted to an enabled Aurora service.

Aurora uses its own existing interface in `AuroraHome.java` for both activity and overlay. Homestead's UI, backgrounds, VPN, ADB client, package/debloat controls, telemetry adjustments and updater are not included.

Inspected upstream source identifiers:

- HomeKeyWatcher blob: `09c5020e63b08423df621c2ba9c84f2e8dca706b`
- HomeRedirectService blob: `46661e8580c033449c3fc6e278a96797dff43319`
- BootReceiver blob: `9357feb0706c73429e558456e755ef5c8a9b0a72`

Complete corresponding Aurora source is in this public repository and each Release's source archive. Build instructions are in README.md. This software comes without warranty; upstream hardware results do not establish that this adaptation works on the same hardware.
