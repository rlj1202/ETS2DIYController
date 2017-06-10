const int pinSteering = A0;
const int pinAcceleration = A1;
const int pinBraking = A2;

long prevMillis;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  while (!Serial);

  prevMillis = millis();

  pinMode(2, INPUT);
}

void loop() {
  long curMillis = millis();
  if (curMillis - prevMillis > 50) {
    prevMillis = curMillis;
    sendMessage(
      analogRead(pinSteering),
      analogRead(pinAcceleration),
      analogRead(pinBraking),
      digitalRead(2), 
      digitalRead(3), 
      digitalRead(4), 
      digitalRead(5), 
      digitalRead(6), 
      digitalRead(7)
      );
  }

//  String str = "";
//  while (Serial.available()) {
//    str.concat(Serial.read());
//  }
//
//  if (str != "") {
//    digitalWrite(13, HIGH);
//  }
}

void sendMessage(int steering, int acceleration, int braking, int btn1, int btn2, int btn3, int btn4, int btn5, int btn6) {
  Serial.print("{\"data\":[");
    Serial.print(steering); Serial.print(",");
    Serial.print(acceleration); Serial.print(",");
    Serial.print(braking); Serial.print(",");
    Serial.print(btn1); Serial.print(",");
    Serial.print(btn2); Serial.print(",");
    Serial.print(btn3); Serial.print(",");
    Serial.print(btn4); Serial.print(",");
    Serial.print(btn5); Serial.print(",");
    Serial.print(btn6);
  Serial.print("]}\n");
}

