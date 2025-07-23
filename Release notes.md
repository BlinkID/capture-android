# Release notes

## v1.4.1

### Improvements

- The SDK now supports 16KB page sizes

### Other changes

- Native code is now built using NDK r28c

### Bugfixes

- Resolved an issue where UI elements could be obscured by system bars when using edge-to-edge mode. The SDK now properly applies insets to prevent overlap, ensuring content remains fully visible.

## v1.4.0

### Improvements

- Introduced **CaptureFilter**:
    - If you need additional checks on Capture result images, you can use [CaptureFilter](https://blinkid.github.io/capture-android/capture-ux/com.microblink.capture.analysis/-capture-filter/index.html). This feature is optional.
    - Capture filter filters capture results after each successful side capture (accepts or drops captured side). If the captured image is filtered out, the capture process is restarted for the current side and the same side is captured again in the same camera session.
    - We are providing one specific implementation of the `CaptureFilter` in `capture-filter-blinkid` library which uses the BlinkID SDK and accepts document images that are extractable by the BlinkID SDK.
- Updated introduction dialog with new image and "Turn your phone to landscape mode" message
- Introduced `AnalyzerRunner.resetSide` function used to retake the current document side.
- Upgraded CameraX dependency to the latest stable version v1.3.4.


### Other changes

- Native code is now built using NDK r27

### Bugfixes

- Fix for "Rotate card or turn phone to landscape" animation being displayed too often and even when it shouldn't be:
    - improved `DocumentFramingStatus.CameraOrientationUnsuitable` (if you are using  the Direct API)
    - fix for case when document met all requirements but it was too close to the camera
    - fix for case when passport is scanned in landscape

## v1.3.0

### Improvements

- Added translations for the following languages: `Arabic`, `Chinese in China`, `Chinese in Taiwan`, `Croatian`, `Czech`, `Dutch`, `Filipino`, `French`, `German`, `Hebrew`, `Hungarian`, `Indonesian`, `Italian`, `Malay`, `Portuguese`, `Romanian`, `Serbian`, `Slovak`, `Slovenian`, `Spanish`, `Thai` and `Vietnamese`.
- Accessibility improvements in the UI

### Minor API changes

- Added option `AnalyzerSettings.keepDpiOnTransformedDocumentImage`:
    - Determines whether to preserve the captured document DPI on transformed document image. If disabled, the document DPI is downscaled to 400 DPI.
- Added option `AnalyzerSettings.enforcedDocumentGroup`:
    - Enforces a specific document group, overriding the analyzer's document classification. This setting impacts the number of sides scanned to match the enforced group, and the way document image is transformed.

### Other changes

- Native code is now built using NDK r26c and uses dynamic c++ runtime (`libc++_shared.so`)
    - In case when multiple different dependencies use the same runtime in the same app, the latest available version of the library should be packaged into the app.
- Added `android.permission.INTERNET` permission to the manifest of `capture-core` library
    - This permission is needed in order to correctly perform license key validation for licenses that require online validation.

## v1.2.0

### Minor API changes

- Introduced `BlurPolicy`, `GlarePolicy` and `TiltPolicy` which are used to enable `strict`, `normal`, `relaxed` or `disabled` policies.
- `AnalyzerSettings` changes:
    - Replaced `tiltThreshold` with `tiltPolicy`.
    - Replaced `ignoreGlare` with `glarePolicy`.
    - Replaced `ignoreBlur` with `blurPolicy`.

## v1.1.0

### Improvements

- SDK is available on Microblink maven repository for easier integration. You no longer need to worry about additional dependencies required by the Capture SDK - they are included as transitive dependencies.
- Introduced support for the **Direct API** integration: 
    - When using the Direct API, you are responsible for preparing input image stream (or static images) for analysis and building a completely custom UX from scratch based on the image-by-image feedback from the SDK.
    - Direct API gives you more flexibility with the cost of a significantly larger integration effort. For example, if you need a camera, you will be responsible for camera management and displaying real-time user guidance.
- Introduced validation of the DPI requirements from the `AnalyzerSettings`:
    - If the user should position the document aligned with the device screen to occupy a larger area on the input image (enables higher capture resolution), we display the animated instructions to rotate the document or phone (to landscape or portrait).
    - If the required `minimumDocumentDpi` cannot be satisfied because of the selected camera resolution on a specific device and automatic DPI adjustment is not enabled by the `adjustMinimumDocumentDpi` flag - we display the error dialog and terminate the Capture screen immediately after the dialog dismissal.
- Added a new option `adjustMinimumDocumentDpi` to the `AnalyzerSettings`. It is used to enable minimum document DPI adjustment. If it is enabled, the minimum DPI is adjusted to the optimal value for the provided input resolution to facilitate the capture of all document groups.
- Added `dpiAdjusted` member to `SideCaptureResult`. If the document is captured at a lower DPI than requesteed by `AnalyzerSettings.minimumDocumentDpi`, a flag is set to `true`.
- Added support for online checked licenses.

### Minor API changes

- Expanded `CaptureResult.Status` with two new statuses:
    - `ERROR_LICENCE_CHECK` - Capture process has been canceled because of the licence check error. This happens if you use a licence that should be online activated and activation fails.
    - `ERROR_ANALYZER_SETTINGS_UNSUITABLE` - Capture process has been canceled because of the AnalyzerSettings validation error.
- SDK is distributed as two separate libraries:
    - `capture-core` provides image analysis and capture without UX, which is enough for Direct API integration
    - `capture-ux` depends on *capture-core* and contains default UX implementation with limited customization possibilities. It enables straightforward integration with minimal effort.
- Renamed all occurrences of `analyse` to `analyze` in code.
- Changed default value of `AnalyzerSettings.minimumDocumentDpi` to `230`

### Bugfixes

 - Fix for turn-side animation being shown on timeout for single-sided documents

## v1.0.2

### Bugfixes

- Fixed camera crashes

## v1.0.1

### Bugfixes

- Fixed **camera permission issue** when starting the SDK for the first time in case when the camera permission has not been granted yet


## v1.0.0

- Initial release of the Capture SDK for Android