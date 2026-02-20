# APK Signing (Debug + Sample Release)

This repository is configured for:
- Debug APK: default Android debug signing
- Release APK: local sample keystore signing for test/dev usage

## 1) Generate sample keystore

Run from project root:

```bash
keytool -genkeypair \
  -v \
  -keystore <PROJECT_ROOT>/openkala-release.jks \
  -alias openkala-release \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass <STORE_PASSWORD> \
  -keypass <KEY_PASSWORD> \
  -dname "CN=OpenKala Dev, OU=Mobile, O=OpenKala, L=Tehran, ST=Tehran, C=IR"
```

Use strong unique passwords. Never use sample/default passwords in real signing workflows.

## 2) Configure local.properties

Add this line to `local.properties` at project root:

```properties
RELEASE_STORE_FILE=<PROJECT_ROOT>/openkala-release.jks
```

## 3) Export signing environment variables

```bash
export OPENKALA_KEY_ALIAS=openkala-release
export OPENKALA_STORE_PASSWORD=<STORE_PASSWORD>
export OPENKALA_KEY_PASSWORD=<KEY_PASSWORD>
```

## 4) Build APKs

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

## 5) Output artifacts

- Debug: `<PROJECT_ROOT>/app/build/outputs/apk/debug/app-debug.apk`
- Release: `<PROJECT_ROOT>/app/build/outputs/apk/release/app-release.apk`

## 6) Verification

Check signing configuration:

```bash
./gradlew :app:signingReport
```

Optional ADB install:

```bash
adb install -r <PROJECT_ROOT>/app/build/outputs/apk/debug/app-debug.apk
adb install -r <PROJECT_ROOT>/app/build/outputs/apk/release/app-release.apk
```

## Failure messages

Release/signing tasks fail with explicit messages if:
- `RELEASE_STORE_FILE` is missing in `local.properties`
- `OPENKALA_STORE_PASSWORD` is missing
- `OPENKALA_KEY_ALIAS` is missing
- `OPENKALA_KEY_PASSWORD` is missing
- keystore file path does not exist
