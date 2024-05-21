<p align="center" >
  <img src="https://raw.githubusercontent.com/wiki/blinkid/blinkid-android/images/logo-microblink.png" alt="Microblink" title="Microblink">
</p>

# _Capture_ SDK for Android

The _Capture_ Android SDK gives you the ability to auto-capture high quality images of identity documents in a user-friendly way. The SDK provides you with a rectified image of the document that ensures high success rate in extracting document text or verifying the document validity.

User is guided to avoid glare, blurred images, bad lighting conditions, fingers over the document or too much tilt. The SDK is able to recognize if a document is single sided (i.e. passport) or double sided (i.e. driving license) and prompt the user to scan the back side of the document when needed.

In the results, you can get a cropped, perspective-corrected image of the document, along with the original frame. Those can be processed by your app in any way required. The SDK is lightweight and can be easily integrated into your mobile app and bland in your design.


# Table of contents
* [Quick Start](#quick-start)
    * [Quick start with the sample app](#quick-sample)
    * [SDK integration](#sdk-integration)
* [Device requirements](#device-requirements)
    * [Android version](#android-version-req)
    * [Camera](#camera-req)
    * [Processor architecture](#processor-arch-req)
* [Customizing the look and UX](#customizing-the-look)
* [Changing default strings and localization](#changing-strings-and-localization)
    * [Defining your own string resources for UI elements](#using-own-string-resources)
* [Completely custom UX with Direct API (advanced)](#direct-api)
    * [The `AnalyzerRunner`](#analyzer-runner)
* [Troubleshooting](#troubleshoot)
* [Additional info](#additional-info)
    * [Capture SDK size](#sdk-size)
    * [API documentation](#api-documentation)
    * [Contact](#contact)


# <a name="quick-start"></a> Quick Start

## <a name="quick-sample"></a> Quick start with the sample apps

1. Open Android Studio.
2. In Quick Start dialog choose _Open project_
3. In File dialog select _CaptureSample_ folder.
4. Wait for the project to load. If Android studio asks you to reload project on startup, select `Yes`.

#### Included sample apps:

- **_app_** demonstrates quick and straightforward integration of the Capture SDK by using the provided UX and limited [customization options](#customizing-the-look) to capture document images and display the results.

- **_app-direct-api_** demonstrates custom integration using the [Direct API](#direct-api), where the integrator is responsible for preparing input image stream (or static images) for analysis and building completely custom UX from scratch, based on the image-by-image feedback from the SDK.


## <a name="sdk-integration"></a> SDK integration

### Adding _Capture_ SDK dependency

The `Capture` library is available on Microblink maven repository.

In your project root, add _Microblink_ maven repository to repositories list:

```
repositories {
    maven { url 'https://maven.microblink.com' }
}
```

Add _Capture_ as a dependency in module level build.gradle(.kts):

```
dependencies {
    implementation("com.microblink:capture-ux:1.3.0")
}
```

### Launching the capture and obtaining the results

1. A valid license key is required to initialize scanning. You can request a free trial license key, after you register, at [Microblink Developer Hub](https://account.microblink.com/signin). License is bound to [application ID](https://developer.android.com/studio/build/configure-app-module#set-application-id) of your app, so please make sure you enter the correct application ID when asked.

   Download your licence file and put it in your application's _assets_ folder. Make sure to set the license key before using any other classes from the SDK, otherwise you will get a runtime exception.

   We recommend that you extend [Android Application class](https://developer.android.com/reference/android/app/Application.html) and set the license in [onCreate callback](https://developer.android.com/reference/android/app/Application.html#onCreate()) like this:


   ```kotlin
   public class MyApplication : Application() {
       override fun onCreate() {
           CaptureSDK.setLicenseFile("path/to/license/file/within/assets/dir", this)
       }	
   }
   ```

2. In your activity, define and create `ActivityResultLauncher` object by using [`MbCapture`](https://blinkid.github.io/capture-android/capture-core/com.microblink.capture/-capture-s-d-k/index.html) contract and define the result callback.
  
   ```kotlin
   private val captureLauncher =
       registerForActivityResult(MbCapture()) { captureResult ->
           when (captureResult.status) {
               CaptureResult.Status.DOCUMENT_CAPTURED -> {
                   // do something with the result, you can use analyzerResult
                   captureResult.analyzerResult?.let { analyzerResult ->
                       // use result
                   }
               }
               CaptureResult.Status.CANCELLED -> {
                   // Capture process has been canceled by the user, or because
                   // of any other unexpected error.
               }
               CaptureResult.Status.ERROR_LICENCE_CHECK -> {
                   // Capture process has been canceled because of the licence
                   // check error. This happens if you use licence which has to
                   // be online activated, and activation fails.
               CaptureResult.Status.ERROR_ANALYZER_SETTINGS_UNSUITABLE -> {
                   // Capture process has been canceled because of the AnalyzerSettings
                   // validation error. This error means that the given analyzer settings
                   // are not suitable for capturing the document from the input image
                   // (image resolution is too small to fulfill all requirements from AnalyzerSettings).
               }
           }
        }
    ```

3. Start capture process by calling [`ActivityResultLauncher.launch`](https://developer.android.com/reference/androidx/activity/result/ActivityResultLauncher#launch(I,androidx.core.app.ActivityOptionsCompat)):

    ```kotlin
    // method within MyActivity from previous step
    public fun startScanning() {
        // Start scanning
        resultLauncher.launch()
    }
    ```

   The results are going to be available in result callback, which was defined in the previous step.

### Capture results

After capture is finished, SDK returns object of tipe [CaptureResult](https://blinkid.github.io/capture-android/capture-ux/com.microblink.capture.result/-capture-result/index.html). You first need to check the [CaptureResult.status](https://blinkid.github.io/capture-android/capture-ux/com.microblink.capture.result/-capture-result/status.html). If the status is `DOCUMENT_CAPTURED`, [CaptureResult.analyzerResult](https://blinkid.github.io/capture-android/capture-ux/com.microblink.capture.result/-capture-result/analyzer-result.html) will be available and you can use it.

You can check the API documentation to see which data fields are available in the  [AnalyzerResult](https://blinkid.github.io/capture-android/capture-core/com.microblink.capture.result/-analyzer-result/index.html).


# <a name="device-requirements"></a> Device requirements

## <a name="android-version-req"></a> Android version

_Capture_ SDK requires Android API level **21** or newer.

## <a name="camera-req"></a> Camera

Camera video preview resolution also matters. In order to perform successful scans, camera preview resolution must be at least **1080p**. Note that camera preview resolution is not the same as video recording resolution.

## <a name="processor-arch-req"></a> Processor architecture

_Capture_ SDK is distributed with **ARMv7** and **ARM64** native library binaries.

_Capture_ is a native library, written in C++ and available for multiple platforms. Because of this, _Capture_ cannot work on devices with obscure hardware architectures. We have compiled _Capture_ native code only for the most popular Android [ABIs](https://en.wikipedia.org/wiki/Application_binary_interface).

If you are combining _Capture_ library with other libraries that contain native code in your application, make sure you match the architectures of all native libraries. For example, if third party library has only ARMv7 version, you must use exactly ARMv7 version of _Capture_ with that library, but not ARM64. Using this architectures will crash your app at initialization step because JVM will try to load all its native dependencies in same preferred architecture and will fail with `UnsatisfiedLinkError`.

To avoid this issue and ensure that only architectures supported by the _Capture_ library are packaged in the final application, add the following statement to your `android/defaultConfig` block inside `build.gradle.kts`:

```
android {
    ...
    defaultConfig {
        ...
        ndk {
        // Tells Gradle to package the following ABIs into your application
        abiFilters 'armeabi-v7a', 'arm64-v8a'
        }
    }
}
```


# <a name="customizing-the-look"></a> Customizing the look and the UX

### Scan overlay theming

<p align="center" >
  <img src="https://raw.githubusercontent.com/wiki/blinkid/blinkid-android/images/capture/capture_overlay_customisation_1.png" alt="Capture SDK">
</p>
<p align="center" >
  <img src="https://raw.githubusercontent.com/wiki/blinkid/blinkid-android/images/capture/capture_overlay_customisation_2.png" alt="Capture SDK">
</p>

To customise the scanning overlay, provide your custom style resource via [`CaptureSettings`](https://blinkid.github.io/capture-android/capture-ux/com.microblink.capture.settings/-capture-settings/index.html) constructor by defining the `style` property. You can customise elements labeled on screenshots above by providing the following properties in your style:

**exit**

* `mb_capture_exitScanDrawable` - icon drawable

**torch**

* `mb_capture_torchOnDrawable` - icon drawable that is shown when the torch is enabled
* `mb_capture_torchOffDrawable` - icon drawable that is show when the torch is disabled

**instructions**

* `mb_capture_instructionsTextAppearance` - style that will be used as `android:textAppearance`
* `mb_capture_instructionsBackgroundDrawable` - drawable used for background

**flashlight warning**

* `mb_capture_flashlightWarningTextAppearance` - style that will be used as `android:textAppearance`
* `mb_capture_flashlightWarningBackgroundDrawable` - drawable used for background

**card icon**

* `mb_capture_cardFrontDrawable` - icon drawable shown during card flip animation, representing front side of the card
* `mb_capture_cardBackDrawable` - icon drawable shown during card flip animation, representing back side of the card

**reticle**

* `mb_capture_reticleDefaultDrawable` - drawable shown when reticle is in neutral state
* `mb_capture_reticleSuccessDrawable` - drawable shown when reticle is in success state (scanning was successful)
* `mb_capture_reticleErrorDrawable` - drawable shown when reticle is in error state

**inner reticle**

* `mb_capture_reticleColor` - color of the shapes displayed during reticle animations

### Introduction dialog and onboarding dialog

<p align="center" >
  <img src="https://raw.githubusercontent.com/wiki/blinkid/blinkid-android/images/capture/capture_overlay_introduction_onboarding.png" alt="Capture SDK">
</p>

**introduction dialog**

* `mb_capture_introductionTitleTextAppearance` - style that will be used as `android:textAppearance` for introduction dialog title
* `mb_capture_introductionMessageTextAppearance` - style that will be used as `android:textAppearance` for introduction dialog message
* `mb_capture_introductionBackgroundColor` - background color of the introduction dialog
* `mb_capture_introductionImage` - drawable displayed in the introduction dialog


**onboarding dialog**

* `mb_capture_onboardingImageColor` - color of the onboarding images
* `mb_capture_onboardingTitleTextAppearance` - style that will be used as `android:textAppearance` for onboarding titles
* `mb_capture_onboardingMessageTextAppearance` - style that will be used as `android:textAppearance` for onboarding messages
* `mb_capture_onboardingButtonTextAppearance` - style that will be used as `android:textAppearance` for onboarding buttons
* `mb_capture_onboardingBackgroundColor` - onboarding screens background color
* `mb_capture_onboardingPageIndicatorColor` - color of the onboarding page indicator

**help button**

* `mb_capture_helpButtonDrawable` - icon drawable of the help button

**help tooltip**

* `mb_capture_helpTooltipColor` - background color of the help tooltip
* `mb_capture_helpTooltipTextAppearance` - style that will be used as `android:textAppearance` for the help tooltip text

**alert dialog**

* `mb_capture_alertDialogButtonTextAppearance` - style that will be used as `android:textAppearance` for the alert dialog button
* `mb_capture_alertDialogTitleTextAppearance` - style that will be used as `android:textAppearance` for the alert dialog title
* `mb_capture_alertDialogMessageTextAppearance` - style that will be used as `android:textAppearance` for the alert dialog message
* `mb_capture_alertDialogBackgroundColor` - alert dialog background color

You can control the visibility of the **introduction dialog** by using [UxSettings](https://blinkid.github.io/capture-android/capture-ux/com.microblink.capture.settings/-ux-settings/index.html) property `showIntroductionDialog` and **onboarding screens** by using `showOnboardingInfo` when defining the [CaptureSettings](https://blinkid.github.io/capture-android/capture-ux/com.microblink.capture.settings/-capture-settings/index.html).


There is also an option for controlling the delay of the **"Show help?"** tooltip that is shown near the help button. The button and tooltip will be shown if the previous option for showing onboarding is `true`.

To change the default delay length of the tooltip, use [UxSettings.showHelpTooltipTimeIntervalMs](https://blinkid.github.io/capture-android/capture-ux/com.microblink.capture.settings/-ux-settings/show-help-tooltip-time-interval-ms.html). Time parameter is set in milliseconds.
The default setting of the delay is 12 seconds (12000 milliseconds).


# <a name="changing-strings-and-localization"></a> Changing default strings and localization

SDK has built-in support for several languages.

You can modify strings and add your own language. For more information on how localization works in Android, check out the [official Android documentation](https://developer.android.com/guide/topics/resources/localization).

## <a name="using-own-string-resources"></a> Defining your own string resources for UI elements

For the capture screen, you can define your own string resources that will be used instead of predefined ones by using the custom [CaptureOverlayStrings](https://blinkid.github.io/capture-android/capture-ux/com.microblink.capture.overlay.resources/-capture-overlay-strings/index.html) while creating the [CaptureSettings]((https://blinkid.github.io/capture-android/capture-ux/com.microblink.capture.settings/-capture-settings/index.html)).

```kotlin

val captureSettings = CaptureSettings(
    strings = CaptureOverlayStrings(
        helpTooltip = R.string.your_help_tooltip_text,
        flashlightWarning = R.string.your_flashlight_warning_message,
        onboardingStrings = OnboardingStrings(
            onboardingSkipButtonText = R.string.your_onboarding_skip_button_text,
            onboardingBackButtonText = R.string.your_onboarding_back_button_text,
            onboardingNextButtonText = R.string.your_onboarding_next_button_text,
            onboardingDoneButtonText = R.string.your_onboarding_done_button_text,
            onboardingTitles = intArrayOf(
                R.string.your_onboarding_title_1,
                R.string.your_onboarding_title_2,
                R.string.your_onboarding_title_3
            ),
            onboardingMessages = intArrayOf(
                R.string.your_onboarding_msg_1,
                R.string.your_onboarding_msg_2,
                R.string.your_onboarding_msg_3
            ),
            introductionDialogTitle = R.string.your_introduction_dialog_title,
            introductionDialogMessage = R.string.your_introduction_dialog_message,
            introductionDoneButtonText = R.string.your_introduction_done_button_text
        ),
        instructionsStrings = InstructionsStrings(
            scanFrontSide = R.string.your_scan_front_side_instructions,
            scanBackSide = R.string.your_scan_back_side_instructions,
            flipDocument = R.string.your_flip_document_instructions,
            rotateDocument = R.string.your_rotate_document_instructions,
            rotateDocumentShort = R.string.your_rotate_document_short_instructions,
            moveFarther = R.string.your_move_farther_instructions,
            moveCloser = R.string.your_move_closer_instructions,
            keepDocumentVisible = R.string.your_keep_document_visible_instructions,
            alignDocument = R.string.your_align_document_instructions,
            increaseLightingIntensity = R.string.your_increase_lighting_instructions,
            decreaseLightingIntensity = R.string.your_decrease_lighting_instructions,
            eliminateBlur = R.string.your_eliminate_blur_instructions,
            eliminateGlare = R.string.your_eliminate_glare_instructions,
        ),
        alertStrings = AlertStrings(
            errorDialogMessageScanningUnavailable = R.string.your_scanning_unavailable_message,
            errorDialogMessageCheckInternet = R.string.your_check_internet_message,
            errorDialogMessageNetworkCommunicationError = R.string.your_network_communication_error_message,
            errorDialogButtonText = R.string.your_error_dialog_button_text
        )
    )
)

```

# <a name="direct-api"></a> Completely custom UX with Direct API (advanced)

When using the **Direct API**, you are responsible for preparing input image stream (or static images) for analysis and building a completely custom UX from scratch based on the image-by-image feedback from the SDK. 

Direct API gives you more flexibility with the cost of a significantly larger integration effort. For example, if you need a camera, you will be responsible for camera management and displaying real-time user guidance.

### Adding _Capture_ SDK dependency for Direct API

For Direct API, you need only Capture SDK core library: **capture-core**, capture-ux is not needed.

In your project root, add _Microblink_ maven repository to the repositories list:

```
repositories {
    maven { url 'https://maven.microblink.com' }
}
```

Add _capture-core_ library as a dependency in module level build.gradle(.kts):

```
dependencies {
    implementation("com.microblink:capture-core:1.3.0")
}
```

## <a name="analyzer-runner"></a> The `AnalyzerRunner`

For the Direct API integration, use the [AnalyzerRunner](https://blinkid.github.io/capture-android/capture-core/com.microblink.capture.directapi/-analyzer-runner/index.html).  It is a singleton object, meaning it is possible to capture a single document at a time.

Like in the default UX, you can configure the `AnalyzerRunner` with desired [AnalyzerSettings](https://blinkid.github.io/capture-android/capture-core/com.microblink.capture.settings/-analyzer-settings/index.html). It is allowed to update settings at any time during analysis.

```kotlin
AnalyzerRunner.settings = AnalyzerSettings(
    // set supported options
)
```

When starting the analysis of the next document, be sure that Analyzer has been reset to the initial state:

```kotlin
AnalyzerRunner.reset()
```

During analysis and after analysis is done, the current result is available via [AnalyzerRunner.result](https://blinkid.github.io/capture-android/capture-core/com.microblink.capture.directapi/-analyzer-runner/result.html).

After analysis is done, and you don't need the `AnalyzerRunner` anymore, be sure to terminate it to release the allocated memory for processing:

```kotlin
AnalyzerRunner.terminate()
```

After terminating, the `AnalyzerRunner` could be used later again. Just start feeding the frames for the next document.



### <a name="direct-api-image-stream"></a> Analyzing the stream of images

When you have a larger number of images coming from the stream, e.g. camera stream or pre-recorded
video stream, use [AnalyzerRunner.analyzeStreamImage](https://blinkid.github.io/capture-android/capture-core/com.microblink.capture.directapi/-analyzer-runner/analyze-stream-image.html) method.

It is expected that you will call this method multiple times to analyze the single document and all analyzed images are considered for building the final `AnalyzerRunner.result`.
		
For each frame, all relevant info for the current status of the analysis and the capture process
is returned by [FrameAnalysisResultListener](https://blinkid.github.io/capture-android/capture-core/com.microblink.capture.directapi/-frame-analysis-result-listener/index.html) as [FrameAnalysisResult](https://blinkid.github.io/capture-android/capture-core/com.microblink.capture.analysis/-frame-analysis-result/index.html), which could be used to guide the user through the scanning process and give real-time feedback.

When [FrameAnalysisResult.captureState](https://blinkid.github.io/capture-android/capture-core/com.microblink.capture.analysis/-frame-analysis-result/-capture-state/index.html) becomes `CaptureState.DocumentCaptured`, this means that the document has been successfully captured and you can use the `AnalyzerRunner.result` as a final capture result. To immediately reset the Analyzer to its initial state and avoid further result changes, you can use `AnalyzerRunner.detachResult()`.

### <a name="direct-api-few-images"></a> Analyzing a few images (usually one or two)

When you have a fixed number of images to analyze, e.g. one (or few) for the front side
and another (or few) for the back side of the document, use [AnalyzerRunner.analyzeImage](https://blinkid.github.io/capture-android/capture-core/com.microblink.capture.directapi/-analyzer-runner/analyze-image.html), which is optimized for single image analysis.

Make sure that you have set appropriate settings to enable capturing of the document side from the single image:

```kotlin
AnalyzerRunner.settings = AnalyzerSettings(
    // here we have to use single frame strategy, because we have one frame per document side
    captureStrategy = CaptureStrategy.SingleFrame
)
```




# <a name="troubleshoot"></a> Troubleshooting

### Integration difficulties
In case of problems with SDK integration, first make sure that you have followed [integration instructions](#sdk-integration) and [device requirements](#device-requirements). If you're still having problems, please contact us at [help.microblink.com](http://help.microblink.com).

### Other problems
If you are having problems like undesired behaviour on specific device(s), crashes inside _Capture_ SDK or anything unmentioned, please contact us at [help.microblink.com](http://help.microblink.com) describing your problem and provide following information:

* high resolution scan/photo of the item that you are trying to read
* information about device that you are using - we need exact model name of the device. You can obtain that information with any app like [this one](https://play.google.com/store/apps/details?id=ru.andr7e.deviceinfohw)
* please stress out that you are reporting problem related to Android version of _Capture_ SDK

# <a name="additional-info"></a> Additional info

## <a name="sdk-size"></a> Capture SDK size

We recommend that you distribute your app using [App Bundle](https://developer.android.com/platform/technology/app-bundle). This will defer apk generation to Google Play, allowing it to generate minimal APK for each specific device that downloads your app, including only required processor architecture support.


Here is the SDK size, calculated for supported ABIs:

| ABI | APK file size increase | APK download size increase |
| --- |:-------------:| :----------------:|
| armeabi-v7a | 2.6 MB | 2.4 MB |
| arm64-v8a | 2.6 MB | 2.5 MB |

SDK size is calculated as application size increase when _Capture_ SDK is added, with all its dependencies included.

## <a name="api-documentation"></a> API documentation
You can find the Capture SDK **KDoc** documentation [here](https://blinkid.github.io/capture-android/index.html).

## <a name="contact"></a> Contact
For any other questions, feel free to contact us at [help.microblink.com](http://help.microblink.com).
