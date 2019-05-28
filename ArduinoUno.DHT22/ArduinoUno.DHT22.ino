#include <SimpleDHT.h>
#define DHT22_PIN 8

char str[4];
SimpleDHT22 dht22(DHT22_PIN);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  
}

void loop() {
  // put your main code here, to run repeatedly:
  byte temperature = 0;
  byte humidity = 0;
  int err = SimpleDHTErrSuccess;
  if ((err = dht22.read(&temperature, &humidity, NULL)) != SimpleDHTErrSuccess) {
    Serial.print("Read DHT22 failed, err="); Serial.println(err);delay(1000);
    return;
  }

  Serial.print((int)temperature); 
  delay(1000); // delay 1s, DHT22 sampling rate is 0.5Hz, 1s is far enough.
}
