# AccessibilityService declaration

## Purpose: App functionality

SayBeer is a voluntary self-control app for adults. The user chooses launcher applications and a time period. The AccessibilityService receives TYPE_WINDOW_STATE_CHANGED events to determine the foreground package name, then displays a TYPE_ACCESSIBILITY_OVERLAY shield only when the foreground package is selected and a blocking session is active. Temporary work windows and emergency release are controlled explicitly by the user.

The service does not retrieve window content (canRetrieveWindowContent=false), read messages, capture text, observe keystrokes, inject gestures, or automate interactions inside third-party apps. It does not transmit any information off the device. The app has no INTERNET permission, accounts, ads or analytics SDKs.

A narrower Android API does not provide foreground-window-triggered blocking overlays for user-selected third-party apps for an ordinary, non-device-owner app. We do not request broad package visibility, Usage Access, Device Admin, or SYSTEM_ALERT_WINDOW.

## Accessibility tool

No. SayBeer does not primarily support users with disabilities; isAccessibilityTool=false.

## Prominent disclosure and consent

After onboarding and whenever permission is missing, the app displays a dialog explaining the use of the Accessibility API, the package-name data accessed, the blocking purpose, and that no content is read or transmitted. The user must tap “Đồng ý · Mở Cài đặt” to proceed to Android accessibility settings and then manually enable the service. “Để sau” dismisses the disclosure without enabling it.

Users retain access to Settings, their launcher and dialer. They can disable the service, force-stop or uninstall SayBeer at any time. No anti-uninstall functionality is implemented.

## Review video checklist

Record the app opening, onboarding/disclosure, affirmative consent, enabling the service in Android Settings, selecting an app, starting a session, opening that app and seeing the overlay, then entering SayBeer and ending the session via emergency release. Explain that only the foreground package name is observed and all state remains on-device. Provide the publicly accessible/unlisted reviewer video URL in Play Console; do not invent a URL.
