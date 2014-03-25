#include <Adafruit_MAX31855.h>
#include < avr/io.h >
#include < avr/interrupt.h >
 
// Thermocouple Code using adafruit serial board and K thermocouple
// Assume that heater recieves 8 volts
// nprav 03/18/2014

const int thermoDO = 3;
const int thermoCS = 4;
const int thermoCLK = 5;
const int heatPin = 9;
const int potPin = A5;
const int led = 13;

int heatMax = 100;
int heatPower = 0;
int potValue = 0;
int boostDelay = 5; //boost time in seconds
double dataPrintCounter = 0;

double kp = 8; //Proportional gain
double ki = 0.05; //Integral gain
double kd = 15; //derivative gain
short total_error = 0;
double current_error = 0;
double diff = 20;
int FINAL_TEMP=64; // Final set temperature
int boostBuffer = 5; // initial power boost until Final Temp - boostBuffer
double output = 0;
double prev_temp = 0;
double temp = 0;
bool run = true;
int k;

Adafruit_MAX31855 thermocouple(thermoCLK, thermoCS, thermoDO);

void setup() {
  pinMode(led,OUTPUT);
  pinMode(heatPin,OUTPUT);
  Serial.begin(9600);
  Serial.println("MAX31855 test");
  // wait for MAX chip to stabilize
  delay(250); // delay 500 ms
  
  while(!Serial.available()){} // Wait for serial prompt
  
  // Set up heat PWM for pin 9, Set for 8 bit Fast PWM (approx 1000Hz)
  TCCR1B = TCCR1B | 0b00001011;
  TCCR1A = TCCR1A | 0b10000001;
  OCR1A = 0;
  //interrupts();
  TIMSK1 = TIMSK1 | 0b00000001; // Enable interrupt when timer overflows
  
}

ISR(TIMER1_OVF_vect){
  OCR1A = heatPower;
  //temp = readTempC();
}


double readTempC(){
  //potValue = analogRead(potPin);
  //potValue = map(potValue,0,1023,25,60);
  //return(potValue);
  //noInterrupts();
  double s = thermocouple.readCelsius();
  //interrupts();
  return(s);
}
  

void loop() {
  // Get temperature readings
  double temp = readTempC();
  if(current_error>0){
    total_error = total_error+current_error;
  }
  else{
    total_error = total_error+(current_error);
  }
  current_error = FINAL_TEMP-temp;
  
  if(k==0 && temp<45){
    k = 1; // this allows a one time initial power boost
    while(readTempC()<(FINAL_TEMP-boostBuffer) && readTempC()>0){
     heatPower = heatMax;
     SerialPrintData();
    }
    digitalWrite(led,HIGH);
    //delay(boostDelay*100);
    digitalWrite(led,LOW);
    prev_temp = readTempC();
    temp = prev_temp;
  }
  
  diff = temp-prev_temp;
  prev_temp = temp;
  output = kp*current_error+ki*(total_error)-kd*diff;
  
  if (total_error>3500)  //avoids integral wind-up
  {
    total_error=2000;
  }
  if(output<0){
    output=0;
  }
  
  if(run){
    heatPower = output;
    if(heatPower>heatMax){
      heatPower = heatMax;
    }
  }
  else{
      heatPower = 0;
      digitalWrite(led,HIGH);
  }
  dataPrintCounter++;
  if(dataPrintCounter >=10){
    SerialPrintData();
    dataPrintCounter = 0;
  }
  //Serial.println(dataPrintCounter);
}

void SerialPrintData(){
  Serial.print("C = "); 
  Serial.println(readTempC());
  Serial.print("total_error = ");
  Serial.println(total_error);
  Serial.print("outputkp = ");
  Serial.println(kp*current_error);
  Serial.print("outputki = ");
  Serial.println(ki*(total_error));
  Serial.print("outputkd = ");
  Serial.println(-kd*diff);
  Serial.print("Output = ");
  Serial.println(output);
  Serial.print("Heat Power = ");
  Serial.println(OCR1A);
}
