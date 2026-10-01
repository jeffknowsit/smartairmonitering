/*
 * SmartAir Arduino Sketch v1.4
 * Arduino UNO Environmental Monitoring System
 * 
 * Sensors:
 *   - DHT11: Temperature & Humidity (Digital Pin 2)
 *   - MQ-5: Gas sensor (Analog Pin A0)
 *   - GP2Y1010AU0F: Dust sensor (Analog Pin A1, LED Pin 7)
 * 
 * Outputs:
 *   - 5V Fan (Pin 9, via relay/transistor)
 *   - Buzzer (Pin 8)
 *   - 16x2 I2C LCD (Address 0x27)
 * 
 * Protocol:
 *   Sends JSON per line at 9600 baud to USB serial.
 *   Example: {"dust":245,"gas":310,"temperature":28.4,"humidity":61,"fan":1,"buzzer":0,"status":"NORMAL"}
 */

#include <DHT.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// Pin Definitions
#define DHT_PIN        2
#define DHT_TYPE       DHT11
#define MQ5_PIN        A0
#define DUST_MEASURE   A1
#define DUST_LED       7
#define FAN_PIN        9
#define BUZZER_PIN     8

// Threshold Configuration (raw sensor values)
#define DUST_WARNING    270
#define DUST_CRITICAL   500
#define GAS_WARNING     400
#define GAS_CRITICAL    600
#define TEMP_WARNING    35.0
#define TEMP_CRITICAL   40.0
#define HUMIDITY_WARNING 75.0

// Timing
#define SEND_INTERVAL   2000  // Send data every 2 seconds
#define DUST_SAMPLE_TIME 280  // Dust sensor sampling time (µs)
#define DUST_DELTA_TIME  40
#define DUST_SLEEP_TIME  9680

// Objects
DHT dht(DHT_PIN, DHT_TYPE);
LiquidCrystal_I2C lcd(0x27, 16, 2);

// State
unsigned long lastSendTime = 0;
bool fanState = false;
bool buzzerState = false;
String currentStatus = "NORMAL";

void setup() {
  Serial.begin(9600);
  
  // Initialize sensors
  dht.begin();
  
  // Initialize pins
  pinMode(DUST_LED, OUTPUT);
  pinMode(FAN_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(MQ5_PIN, INPUT);
  
  // Initialize LCD
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("SmartAir v1.4");
  lcd.setCursor(0, 1);
  lcd.print("Initializing...");
  
  // Initial state
  digitalWrite(FAN_PIN, LOW);
  digitalWrite(BUZZER_PIN, LOW);
  
  delay(2000); // Sensor warm-up
  lcd.clear();
}

void loop() {
  unsigned long now = millis();
  
  if (now - lastSendTime >= SEND_INTERVAL) {
    lastSendTime = now;
    
    // Read sensors
    float temperature = dht.readTemperature();
    float humidity = dht.readHumidity();
    int gasValue = analogRead(MQ5_PIN);
    int dustValue = readDustSensor();
    
    // Validate DHT readings
    if (isnan(temperature)) temperature = -1;
    if (isnan(humidity)) humidity = -1;
    
    // Determine status and control outputs
    evaluateStatus(dustValue, gasValue, temperature, humidity);
    
    // Control fan (automatic threshold-based)
    controlFan(dustValue, gasValue, temperature, humidity);
    
    // Control buzzer (critical only)
    controlBuzzer();
    
    // Send JSON to Android via USB serial
    sendJsonData(dustValue, gasValue, temperature, humidity);
    
    // Update LCD
    updateLCD(temperature, humidity, dustValue, gasValue);
  }
  
  // Listen for commands from Android (future use)
  if (Serial.available() > 0) {
    String command = Serial.readStringUntil('\n');
    command.trim();
    handleCommand(command);
  }
}

int readDustSensor() {
  digitalWrite(DUST_LED, LOW); // Turn on IR LED
  delayMicroseconds(DUST_SAMPLE_TIME);
  
  int rawValue = analogRead(DUST_MEASURE);
  
  delayMicroseconds(DUST_DELTA_TIME);
  digitalWrite(DUST_LED, HIGH); // Turn off IR LED
  delayMicroseconds(DUST_SLEEP_TIME);
  
  return rawValue;
}

void evaluateStatus(int dust, int gas, float temp, float hum) {
  bool isCritical = false;
  bool isWarning = false;
  
  if (dust >= DUST_CRITICAL || gas >= GAS_CRITICAL) isCritical = true;
  if (temp >= TEMP_CRITICAL) isCritical = true;
  
  if (dust >= DUST_WARNING || gas >= GAS_WARNING) isWarning = true;
  if (temp >= TEMP_WARNING || hum >= HUMIDITY_WARNING) isWarning = true;
  
  if (isCritical) {
    currentStatus = "CRITICAL";
  } else if (isWarning) {
    currentStatus = "WARNING";
  } else {
    currentStatus = "NORMAL";
  }
}

void controlFan(int dust, int gas, float temp, float hum) {
  // Fan ON if any reading exceeds warning threshold
  if (dust >= DUST_WARNING || gas >= GAS_WARNING || 
      temp >= TEMP_WARNING || hum >= HUMIDITY_WARNING) {
    fanState = true;
    digitalWrite(FAN_PIN, HIGH);
  } else {
    // Hysteresis: turn off only when well below thresholds
    if (dust < (DUST_WARNING - 30) && gas < (GAS_WARNING - 30) &&
        temp < (TEMP_WARNING - 2) && hum < (HUMIDITY_WARNING - 5)) {
      fanState = false;
      digitalWrite(FAN_PIN, LOW);
    }
  }
}

void controlBuzzer() {
  if (currentStatus == "CRITICAL") {
    buzzerState = true;
    // Short beeps for critical
    tone(BUZZER_PIN, 2000, 200);
  } else {
    buzzerState = false;
    noTone(BUZZER_PIN);
  }
}

void sendJsonData(int dust, int gas, float temp, float hum) {
  Serial.print("{\"dust\":");
  Serial.print(dust);
  Serial.print(",\"gas\":");
  Serial.print(gas);
  Serial.print(",\"temperature\":");
  if (temp >= 0) {
    Serial.print(temp, 1);
  } else {
    Serial.print("null");
  }
  Serial.print(",\"humidity\":");
  if (hum >= 0) {
    Serial.print(hum, 0);
  } else {
    Serial.print("null");
  }
  Serial.print(",\"fan\":");
  Serial.print(fanState ? 1 : 0);
  Serial.print(",\"buzzer\":");
  Serial.print(buzzerState ? 1 : 0);
  Serial.print(",\"status\":\"");
  Serial.print(currentStatus);
  Serial.println("\"}");
}

void updateLCD(float temp, float hum, int dust, int gas) {
  lcd.setCursor(0, 0);
  
  if (currentStatus == "CRITICAL") {
    lcd.print("!! CRITICAL !!  ");
  } else if (currentStatus == "WARNING") {
    lcd.print("* WARNING *     ");
  } else {
    lcd.print("SmartAir  OK    ");
  }
  
  lcd.setCursor(0, 1);
  if (temp >= 0) {
    lcd.print(temp, 1);
    lcd.print("C ");
  } else {
    lcd.print("--C ");
  }
  if (hum >= 0) {
    lcd.print(hum, 0);
    lcd.print("% ");
  } else {
    lcd.print("--% ");
  }
  lcd.print("D:");
  lcd.print(dust);
  lcd.print("  ");
}

void handleCommand(String command) {
  // Future command channel from Android
  if (command == "STATUS") {
    sendJsonData(
      analogRead(DUST_MEASURE),
      analogRead(MQ5_PIN),
      dht.readTemperature(),
      dht.readHumidity()
    );
  }
  // Add more commands here as needed
  // Commands are explicit actions, NEVER AI-generated
}
