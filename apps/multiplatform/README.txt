UITP Connect — Android System Icon Resource Pack
Generated from user-provided UITP logo (304 x 305 JPEG).

Archive root: android/src/main/res/...
Unpack into apps/multiplatform/ (do NOT unpack into repo root).

Contains:
- standard (legacy) icon and round icon in 5 pixel densities
- adaptive foreground icons in 5 pixel densities, safe-area scaled
- both default and former dark-blue icon alternatives replaced with UITP image
- Android 13+ themed icon asset and adaptive-icon XMLs
- small notification and foreground service icons in 5 densities (white segmented ring)

Does NOT modify:
- UI/internal logos (drawable/logo.png, drawable/logo_light.png, SplashView.kt)
- AndroidManifest.xml and Kotlin code
- unrelated drawable-hdpi/icon.png (its use should be inspected)
- existing original theme color assets

After extracting, inspect git diff --stat, git status -sb, and the APK on device.
Test both launcher aliases, Android settings, Recents, status bar and call notifications.
Replace extracted files on your system only after verifying current git branch is stage2/branding.

Developer note: only source logo was available as a low-resolution JPEG;
for final store publication use the official vector SVG when possible.
