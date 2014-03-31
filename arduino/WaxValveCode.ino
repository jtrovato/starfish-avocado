#include <Adafruit_MAX31855.h>
#include < avr/io.h >
#include < avr/interrupt.h >
 
// Code that can control Wax Valve Peltier heater via potentiometer, 
// and control a pump with an inputted pumpPower value.
// Thermocouple Code using adafruit serial board and K thermocouple
// Assume that Input Voltage is 8 volts
// nprav 03/25/2014

boolean heatTest = true;
boolean pumpTest = true;

const int thermoDO = 3;
const int thermoCS = 4;
const int thermoCLK = 5;
const int heatPin = 9;
const int pumpPin = 10;
const int potPin = A5;
const int led = 13;

int heatMax = 100;
int heatPower = 0;
int potValue = 0;
int boostDelay = 5; //boost time in seconds
long dataPrintCounter = 0;

int pumpPower = 0;

Adafruit_MAX31855 thermocouple(thermoCLK, thermoCS, thermoDO);

void setup() {
  pinMode(led,OUTPUT);
  pinMode(heatPin,OUTPUT);
  pinMode(pumpPin,OUTPUT);
  Serial.begin(9600);
  Serial.println("P2D2 WaxValveCode");
  // wait for MAX chip to stabilize
  delay(250); // delay 500 ms
  
  while(!Serial.available()){} // Wait for serial prompt
  Serial.read();
  
  // Set up heat PWM for pin 9, Set for 8 bit Fast PWM (approx 1000Hz)
  TCCR1B = TCCR1B | 0b00001011;
  TCCR1A = TCCR1A | 0b10100001;
  OCR1A = 0;    // Controls heater power
  OCR1B = 0;    // Controls pump power
  TIMSK1 = TIMSK1 | 0b00000001; // Enable interrupt when timer overflows
  
}

ISR(TIMER1_OVF_vect){
  OCR1A = heatPower;
  OCR1B = pumpPower;
}


double readTempC(){
  //noInterrupts();
  return (thermocouple.readCelsius());
}
  

void loop() {
  
  if (Serial.available() > 0) {
                // read the incoming byte:
                pumpPower = Serial.parseInt();

                // say what you got:
                Serial.print("I received: ");
                Serial.println(pumpPower,DEC);
        }
        
  potValue = analogRead(potPin);
  potValue = map(potValue,0,1023,0,75);
  heatPower = potValue;
  
  dataPrintCounter++;
  if(dataPrintCounter >=1000){
    SerialPrintData();
    dataPrintCounter = 0;
  }
  //Serial.println(dataPrintCounter);
}

void SerialPrintData(){
  
  if(heatTest){
    Serial.print("C = "); 
    Serial.println(readTempC());
    Serial.print("Heat Power = ");
    Serial.println(OCR1A);
  }
  
  if(pumpTest){
    Serial.print("pumpPower = "); 
    Serial.println(OCR1B);
  }
}
