# Protocol Zero: System Battery Health
### Internal Code: GhostBattery

**System Battery Health** is a sophisticated Android security application disguised as a generic battery utility. Its primary purpose is to provide a "panic button" capability for users in high-risk environments (e.g., journalists, activists, or individuals in hostile territories) who need to rapidly secure their device data and uninstall sensitive applications.

---

## ⚠️ Critical Disclaimer

**This software allows for the permanent, unrecoverable destruction of data.**
By using this application, you acknowledge that:
1.  **Deletion is Permanent:** Files targeted by the Incinerator are corrupted (header overwriting) before deletion. Recovery is statistically impossible.
2.  **Use at Your Own Risk:** The developers assume no liability for data loss, device instability, or legal consequences arising from the use of this software.
3.  **Not Malware:** This tool is designed for **self-defense** and personal privacy. It does not propagate, steal data, or act without explicit user configuration and interaction.

---

## 🏗️ Architecture

The application is built on a modular architecture separating the **Decoy** layer from the **Secure** layer.

### 1. The Decoy Layer (Camouflage)
*   **Appearance:** Looks and behaves like a standard "Battery Health" monitor.
*   **Functionality:** Displays real battery voltage, level, and health status.
*   **Stealth:** No "Security" or "Panic" keywords in the app name or icon.
*   **Entry Point:** The secure dashboard is hidden. To access it, the user must tap the "VOLTAGE" text (e.g., "4200 mV") exactly **5 times** in rapid succession.

### 2. The Secure Layer (Protocol Zero)
Once triggered, the app exposes the **Panic Dashboard**, which orchestrates three parallel defensive actions:

#### A. SOS Beacon (Network Layer)
*   **Mechanism:** Fetches the last known high-accuracy GPS location.
*   **Action:** Constructs a deep link (Google Maps) and sends it via WhatsApp or SMS to a pre-configured trusted contact.
*   **Automation:** Uses the Accessibility Service to automatically click "Send" if user interaction is blocked.

#### B. Data Incinerator (Storage Layer)
*   **Mechanism:** A custom file destruction engine.
*   **Process:**
    1.  **Open:** Accesses the file stream.
    2.  **Corrupt:** Overwrites the first 4KB (header) with random noise.
    3.  **Delete:** unlink() the file from the filesystem.
*   **Scope:**
    *   Internal Storage (`/sdcard/DCIM`, `/sdcard/Pictures`, `/sdcard/Download`)
    *   Physical SD Cards (Auto-detected)
    *   WhatsApp Media & Databases
    *   Samsung Gallery Trash (`.trash`)
    *   **Custom Targets:** User-defined folders via the Settings menu.

#### C. App Purge (Package Layer)
*   **Mechanism:** Loops through a list of sensitive apps (e.g., Telegram, Signal).
*   **Action:** Launches system uninstall intents for each package sequentially.
*   **Automation:** The Accessibility Service (`GhostAccessibilityService`) detects the "Uninstall?" dialog and auto-clicks "OK" to speed up the process.
*   **Final Act:** The app uninstalls itself (`SelfDestruct`) as the last step.

---

## 🛠️ Setup Guide

### Prerequisites
*   **Android 11+** recommended for full feature support.
*   **Physical Device** (Emulators may fail GPS/Camera tests).

### Installation
1.  **Build:** Clone the repo and build the APK using Android Studio or the provided GitHub Action.
2.  **Install:** Sideload the `app-debug.apk` onto your device.
3.  **Hide:** (Optional) Use your launcher's "Hide App" feature to further conceal "System Battery Health".

### Configuration (The Onboarding)
Upon first launch, you must grant:
1.  **Manage All Files:** Essential for the Incinerator to bypass the Recycle Bin and wipe external storage.
2.  **Accessibility Service:** Essential for the auto-clicker to confirm uninstalls and send messages without delay.
3.  **Location:** Essential for the SOS Beacon.

### Customization
Go to the **Settings** (Gear Icon) in the Panic Dashboard:
*   **SOS Contact:** Enter the phone number (international format) of your trusted contact.
*   **Target Apps:** Select which apps to purge. You can pick from a list of installed apps.
*   **Custom Folders:** Browse and select specific secret folders to be incinerated.

---

## 🚦 Usage Manual

### Drill Mode (Simulation)
**Do not test the app by running a real panic sequence.** You will lose data.
Instead, use **Drill Mode** (accessible via Settings):
*   Mimics the UI and timing of the Panic Dashboard.
*   Simulates SOS transmission (Toasts only).
*   Simulates File Deletion (No actual IO).
*   Simulates Uninstalls (Fake Dialogs).
*   **Goal:** Build muscle memory for the 5-tap trigger and "Execute" button.

### Real Execution (The Chase)
1.  **Open App:** Launch "System Battery Health".
2.  **Trigger:** Tap "Voltage" 5 times.
3.  **Authenticate:** (Optional) Enter PIN if configured.
4.  **EXECUTE:** Tap the big red button.
5.  **Action:** Keep the screen ON.
    *   The app will handle the rest.
    *   If a dialog appears and the auto-clicker misses it, tap "OK" manually.

---

## 🔒 Security & Privacy

*   **Encrypted Storage:** All configuration data (SOS number, target apps) is stored using `EncryptedSharedPreferences` (AES-256). It cannot be read by forensic tools dumping the app's data.
*   **No Internet Permission:** The app does not have standard internet access (only implicit intents for WhatsApp/Maps), preventing it from leaking data to third-party servers.
*   **Source Code:** Open source and auditable.

---

## ⚠️ Operational Security (OPSEC) Tips

1.  **Notifications:** Go to *Android Settings -> Apps -> System Battery Health -> Notifications* and turn them **OFF**. You do not want a "Permission Granted" or "Service Running" notification to give you away.
2.  **Battery Optimization:** Set the app to **Unrestricted** battery usage so the OS doesn't kill the background Incinerator process.
3.  **Secure Folder:** For Samsung users, move this app (and your sensitive apps) into the **Secure Folder**. This adds a layer of encryption and makes the "Decoy" effectively double-blind.

---

## 📄 License
MIT License. See `LICENSE` file for details.
