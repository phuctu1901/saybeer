# SayBeer Android — Stitch redesign

Source: the owner's open Chrome project [Button Redesign Project](https://stitch.withgoogle.com/projects/17773373471050443838), extracted through Computer Use on 28 October 2026. The project ZIP was exported with all canvas screens selected. Original HTML and PNG references are in `stitch/export/stitch_button_redesign_project/`. The first home PNG in the export is an empty 28-byte placeholder; the duplicate home screen ending `_2` contains the full reference image.

## Mapping to Compose

| Stitch screen | Android implementation |
| --- | --- |
| Home / redesigned buttons | `SayBeerApp.kt`: privacy badge, original beer SVG converted to vector drawable, central amber glow, gradient CTA, two compact navigation cards |
| Permission popup | Native Compose dialog with blurred background, shield badge, separate privacy callout, settings guidance, full-width consent and dismiss actions |
| Active lock | Glowing lock badge, live countdown, neutral outlined actions, emergency action |
| Work window | Centered hero and instructions, commitment card, pinned action and remaining-use counter |
| Early unlock test | Timer at top, quote and optional math cards, pinned unlock and keep-locked actions |
| Session setup | App selection card, gradient duration choices, math switch, pinned start action |
| Weekly schedule | Selectable 4×2 day grid including select-all, editable start/end rows, overnight duration and pinned save action |

Shared native components use the extracted dark/amber palette, 12–18 dp controls/cards, 28–30 dp dialog corners, subtle borders, warm text and responsive scrolling. Onboarding, statistics, app picker, emergency and privacy pages inherit the same tokens. The real Android system bars and time picker remain native; mock iOS status/home bars are not drawn.

## Functional constraints retained

No WebView or remote UI is used. All existing local storage, foreground-app blocking, work-window wait, 90-second challenge, three-failure lockout, emergency release and timezone-aware scheduling remain connected. Times, selected app counts, history and permission status come from real state. The source's sample claims about blocking calls are replaced with accurate wording: the protected dialer remains available. No hardcoded mock history or app-count examples are copied.

Build: versionName `1.1.0`, versionCode `3`, package unchanged `com.tem2t.saybeer`. The Play Store release remains the existing code 2 until a separate upload. Store screenshots and the Accessibility review video must be refreshed for code 3 before submitting that release.

## Verification

Final Gradle build, signed APK/AAB and 13 unit tests passed. Full instrumentation suite: **12/12 passed** on API 36; see `instrumentation-code3.txt`. Actual Compose captures are in `screenshots/store/`; permission dialog and functional-state captures are in `screenshots/flows/`. These are real native UI screenshots; session/history fixtures are used only by tests. No screenshots or video were automatically replaced on the live Play listing.
