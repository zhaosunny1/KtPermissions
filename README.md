# RxPermissions

[![](https://jitpack.io/v/zhaosunny1/KtPermissions.svg)](https://jitpack.io/#zhaosunny1/KtPermissions)

This library allows the usage of RxJava with the new Android M permission model.

## Setup

To use this library your `minSdkVersion` must be >= 14.

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
	        implementation 'com.github.zhaosunny1:KtPermissions:1.0.0'
}
```

## Usage

Create a `RxPermissions` instance :

```kotlin
RxPermissions(this) // where this is an Activity or Fragment instance and must implementation CoroutineScope by MainScope()
```

Example : request the CAMERA permission (with Retrolambda for brevity, but not required)

```kotlin
// Must be done during an initialization phase like onCreate
rxPermissions
    .request(Manifest.permission.CAMERA){granted -> {
        if (granted) { // Always true pre-M
           // I can control the camera now
        } else {
           // Oups permission denied
        }
    }}
```


Look at the `sample` app for more.

## Important read

**As mentioned above, because your app may be restarted during the permission request, the request
must be done during an initialization phase**. This may be `Activity.onCreate`, or
`View.onFinishInflate`, but not *pausing* methods like `onResume`, because you'll potentially create an infinite request loop, as your requesting activity is paused by the framework during the permission request.

If not, and if your app is restarted during the permission request (because of a configuration
change for instance), the user's answer will never be emitted to the subscriber.

You can find more details about that [here](https://github.com/tbruyelle/RxPermissions/issues/69).

## Status

This library is still beta, so contributions are welcome.
I'm currently using it in production since months without issue.

## Benefits

- Avoid worrying about the framework version. If the sdk is pre-M, the observer will automatically
receive a granted result.

- Prevents you to split your code between the permission request and the result handling.
Currently without this library you have to request the permission in one place and handle the result
in `Activity.onRequestPermissionsResult()`.

- All what RX provides about transformation, filter, chaining...

# License

```
Copyright (C) 2015 Thomas Bruyelle

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
