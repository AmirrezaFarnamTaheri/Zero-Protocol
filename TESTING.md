# Testing Protocol Zero

This project uses standard Android testing frameworks: JUnit 4, Robolectric, and Mockito.

## Prerequisites

- JDK 17 (or compatible)
- Android SDK 34 (Command line tools installed)

## Running Tests

To run all unit tests (including Robolectric tests):

```bash
./gradlew testDebugUnitTest
```

## Test Coverage

The test suite covers:
- **Core Logic:** `DataIncinerator`, `AppManager`, `SamsungUtils`
- **Data Persistence:** `PrefsManager` (EncryptedSharedPreferences)
- **Service Logic:** `GhostAccessibilityService` (Node traversal logic)
- **Legacy Compatibility:** `GalleryManager` (Empty state handling)

## Structure

Tests are located in `app/src/test/java/com/ghostbattery/`.
- `core/manager/`: Manager logic tests.
- `data/`: Preferences and data model tests.
- `service/`: Service logic tests.
- `utils/`: Utility function tests.
