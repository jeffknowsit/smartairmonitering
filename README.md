# SmartAir Monitoring System

SmartAir is a fully functional Android-based smart environmental monitoring dashboard that interfaces with an Arduino Uno via USB OTG. It provides real-time sensor analytics, history tracking, intelligent alerts, and AI-driven insights.

## Features

- **Real-Time Telemetry:** Connects to an Arduino Uno over USB OTG to stream temperature, humidity, dust, and gas readings.
- **Dynamic Dashboard:** A beautiful Jetpack Compose interface with sparklines, status badges, and an intuitive Command Center.
- **Local Alert Engine:** Evaluates thresholds instantly and updates the system status (Normal, Warning, Critical).
- **Critical Alerts (SMS):** When conditions become critical, SmartAir bypasses standard notifications and sends an emergency SMS directly to a specified contact.
- **Historical Data Logging:** Stores sensor readings using Room Database for offline analytics and trend graphing.
- **AI Assistant:** Local AI service integration for contextual environmental insights and chatting about your air quality.
- **Hardware Integration:** Includes a robust custom USB prober fallback to detect a wide variety of Arduino clones (including CH340 chips).

## Tech Stack

- **Platform:** Android (Kotlin)
- **UI Framework:** Jetpack Compose
- **Architecture:** MVI (Model-View-Intent) using ViewModel & StateFlow
- **Local Storage:** Room Database
- **Hardware Communication:** `usb-serial-for-android` via USB OTG
- **Background Work:** Kotlin Coroutines
- **JSON Parsing:** Gson

## Setup Instructions

1. **Hardware Preparation:**
   - Flash the `SmartAir_v1.4.ino` sketch (located in the `arduino/` folder) to your Arduino Uno.
   - Connect the Arduino Uno to your Android device using a USB OTG adapter.

2. **Android App:**
   - Clone this repository.
   - Open the project in Android Studio.
   - Build and run the app on a physical Android device (emulators do not support USB OTG serial out-of-the-box).

3. **Usage:**
   - Upon opening the app, it will automatically prompt you for USB permissions if the Arduino is connected.
   - Navigate to the **Settings** tab to configure your Room Name, SMS recipient number, and storage intervals.
   - View real-time graphs on the **History** tab.
   - Ask the **AI Assistant** for insights on the current air quality trends.

## Screenshots

*(Add screenshots here)*

## License

This project is licensed under the MIT License.
