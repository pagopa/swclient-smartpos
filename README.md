# swc-smartpos

This project is a Proof of concept written to pay a PagoPa advice with Pos.
Project has 5 main modules.

- [app](#app) -> main application
- [network](#network) -> module to manage network requests
- [sharedUtils](#sharedutils) -> module where to put code shared by all kind of pos
- [ui_kit](#uikit) -> module where to put the base of all front-end application


## App

It has two main buildTypes: Debug and release. Debug will be debuggable and will not shrink your resources and will not minify your code, else Release will not
be debuggable and will shrink resources and minify your code. About minifying, please check proguard-rules.pro.
App will have a specified flavour for each pos which will be selected by business.\
So, in resume, every pos selected by business will have:

- One dedicated module where to put logic.
- One flavour to implement its module.
- One dedicated class SdkUtility where to implement module logic respect Pos.

App has MVVM architecture.\
minSDK is set to 19 because some poses present in the market have SDK 19 installed.

## Network

Network module will give you back a Resource<LiveData> for your data class.\
Resource:

```kotlin
data class Resource<out T>(val code: Int?, val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(code: Int?, data: T?): Resource<T> = Resource(code, Status.SUCCESS, data, null)
        fun <T> error(code: Int?, msg: String): Resource<T> = Resource(code, Status.ERROR, null, msg)
        fun <T> loading(): Resource<T> = Resource(null, Status.LOADING, null, null)
    }
}
```

Status:

```kotlin
enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}
```

To make a request you can pass an annotation class to your method putted in it.pagopa.swc_smartpos.network.HttpServiceInterface\
Example get:

```kotlin
@Get("$your_base_url/rest_of_path")
fun getCall(
    context: Context
): LiveData<Resource<ExampleDataClassResponse>> {
    val network = BaseNetwork<ExampleDataClassResponse>(viewModelScope, this.javaClass.name)
    network.call(
        context = context,
        typeToken = ExampleDataClassResponse::class.java
    )
    return network.data
}
```
Example post:
```kotlin
@Post("$your_base_url/rest_of_path")
fun getCall(
    context: Context,
    payload: ExamplePayloadDataClass
): LiveData<Resource<ExampleDataClassResponse>> {
    val network = BaseNetwork<ExampleDataClassResponse>(viewModelScope, this.javaClass.name)
    network.call(
        context = context,
        ExampleDataClassPayload(exampleParam="test"),
        ExamplePayloadDataClass::class.java,
        typeToken = ExampleDataClassResponse::class.java
    )
    return network.data
}
```

BaseNetwork class uses reflection to understand which kind of request you're trying to do. In order to do this, in proguard-rules, HttpServiceInterface class and methods
will not be obfuscated.

```proguard
-keepnames class it.pagopa.swc_smartpos.view_model.** {*;}
```

Annotations available are:

```kotlin
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Get(val url: String)

@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Post(val url: String)

@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Patch(val url: String)

@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Put(val url: String)

@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Delete(val url: String)
```

You can change runtime your url if needed using data class runtime url:

```kotlin
data class RuntimeUrl(val oldValue: String, val newValue: String) : java.io.Serializable
```

Example:

```kotlin
 @Get("$your_base_url/to_change")
fun getCall(
    context: Context
): LiveData<Resource<ExampleDataClassResponse>> {
    val network = BaseNetwork<ExampleDataClassResponse>(viewModelScope, this.javaClass.name)
    network.call(
        context = context,
        typeToken = ExampleDataClassResponse::class.java,
        runtimeUrl = arrayOf(RuntimeUrl("to_change", "new_value"))
    )
    return network.data
}
```
Note that runtimeUrl param is an array, so you can change multiple values.

## SharedUtils

Here you have:
- encryption util class
- camera utils class with logic to scan QrCodes and BarCodes.
- vibration utils class
- Event class.

Event is used to observe a single LiveData event:
````kotlin
/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
class Event<out T>(private val content: T) {
    /** Allow external read but not write*/
    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}
````
## Ui_kit
Here you have:
- buttons to use in app, styled and Custom with lottie for lazy loading.
- dialogs
- a divider, both horizontal and vertical basing on the style passed

Buttons styles:
````xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="MainButton" parent="Widget.AppCompat.Button">
        <item name="android:layout_width">wrap_content</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:fontFamily">@font/readex_pro</item>
        <item name="android:gravity">center</item>
        <item name="android:paddingTop">@dimen/button_padding_vertical</item>
        <item name="android:paddingStart">@dimen/button_padding_horizontal</item>
        <item name="android:paddingBottom">@dimen/button_padding_vertical</item>
        <item name="android:paddingEnd">@dimen/button_padding_horizontal</item>
        <item name="textAllCaps">false</item>
        <item name="android:textSize">@dimen/cta</item>
    </style>

    <style name="MainDrawableButton">
        <item name="android:layout_width">wrap_content</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:elevation">3dp</item>
        <item name="android:textSize">@dimen/cta</item>
    </style>

    <style name="PrimaryButton" parent="MainButton">
        <item name="android:textColor">@color/white</item>
        <item name="android:background">@drawable/rounded_primary_filled_8dp</item>
    </style>

    <style name="PrimaryButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_primary_filled_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="PrimaryButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_primary_filled_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="PrimaryOutLinedButton" parent="MainButton">
        <item name="android:textColor">@color/primary</item>
        <item name="android:background">@drawable/rounded_primary_empty_8dp</item>
    </style>

    <style name="PrimaryOutLinedButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_primary_empty_8dp</item>
        <item name="button_icon">@drawable/primary_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/primary</item>
    </style>

    <style name="PrimaryOutLinedButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_primary_empty_8dp</item>
        <item name="button_icon">@drawable/primary_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/primary</item>
    </style>

    <style name="WhiteOutLinedButton" parent="MainButton">
        <item name="android:textColor">@color/white</item>
        <item name="android:background">@drawable/rounded_white_primary_border_8dp</item>
    </style>

    <style name="WhiteOutLinedButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_white_primary_border_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="WhiteOutLinedButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_white_primary_border_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="WhiteButton" parent="MainButton">
        <item name="android:textColor">@color/primary</item>
        <item name="android:background">@drawable/rounded_white_filled_8dp</item>
    </style>

    <style name="WhiteButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_white_filled_8dp</item>
        <item name="button_icon">@drawable/primary_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/primary</item>
    </style>

    <style name="WhiteButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_white_filled_8dp</item>
        <item name="button_icon">@drawable/primary_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/primary</item>
    </style>

    <style name="SuccessLightButton" parent="MainButton">
        <item name="android:textColor">@color/success_dark</item>
        <item name="android:background">@drawable/rounded_success_light_filled_8dp</item>
    </style>

    <style name="SuccessLightButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_success_light_filled_8dp</item>
        <item name="button_icon">@drawable/green_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/success_dark</item>
    </style>

    <style name="SuccessLightButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_success_light_filled_8dp</item>
        <item name="button_icon">@drawable/green_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/success_dark</item>
    </style>

    <style name="SuccessDarkButton" parent="MainButton">
        <item name="android:textColor">@color/white</item>
        <item name="android:background">@drawable/rounded_success_dark_filled_8dp</item>
    </style>

    <style name="SuccessDarkButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_success_dark_filled_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="SuccessDarkButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_success_dark_filled_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="InfoLightButton" parent="MainButton">
        <item name="android:textColor">@color/info_dark</item>
        <item name="android:background">@drawable/rounded_info_light_filled_8dp</item>
    </style>

    <style name="InfoLightButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_info_light_filled_8dp</item>
        <item name="button_icon">@drawable/primary_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/info_dark</item>
    </style>

    <style name="InfoLightButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_info_light_filled_8dp</item>
        <item name="button_icon">@drawable/primary_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/info_dark</item>
    </style>

    <style name="InfoDarkButton" parent="MainButton">
        <item name="android:textColor">@color/white</item>
        <item name="android:background">@drawable/rounded_info_dark_filled_8dp</item>
    </style>

    <style name="InfoDarkButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_info_dark_filled_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="InfoDarkButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_info_dark_filled_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="ErrorLightButton" parent="MainButton">
        <item name="android:textColor">@color/error_dark</item>
        <item name="android:background">@drawable/rounded_error_light_filled_8dp</item>
    </style>

    <style name="ErrorLightButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_error_light_filled_8dp</item>
        <item name="button_icon">@drawable/red_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/error_dark</item>
    </style>

    <style name="ErrorLightButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_error_light_filled_8dp</item>
        <item name="button_icon">@drawable/red_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/error_dark</item>
    </style>

    <style name="ErrorDarkButton" parent="MainButton">
        <item name="android:textColor">@color/white</item>
        <item name="android:background">@drawable/rounded_error_dark_filled_8dp</item>
    </style>

    <style name="ErrorDarkButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_error_dark_filled_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="ErrorDarkButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_error_dark_filled_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="WarningDarkButton" parent="MainButton">
        <item name="android:textColor">@color/white</item>
        <item name="android:background">@drawable/rounded_warning_dark_filled_8dp</item>
    </style>

    <style name="WarningDarkButtonEndStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_warning_dark_filled_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textEnd</item>
        <item name="text_color">@color/white</item>
    </style>

    <style name="WarningDarkButtonStartStar" parent="MainDrawableButton">
        <item name="android:background">@drawable/rounded_warning_dark_filled_8dp</item>
        <item name="button_icon">@drawable/white_star</item>
        <item name="drawableGravity">textStart</item>
        <item name="text_color">@color/white</item>
    </style>
</resources>
````

Divider styles:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
   <style name="Divider">
        <item name="android:layout_margin">@dimen/divider_padding</item>
        <item name="android:importantForAccessibility">no</item>
        <item name="android:background">@color/grey_ultra_light</item>
    </style>

    <style name="HorizontalDivider" parent="Divider">
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">1dp</item>
    </style>

    <style name="VerticalDivider" parent="Divider">
        <item name="android:layout_width">1dp</item>
        <item name="android:layout_height">match_parent</item>
    </style>
</resources>
```

