#include <Adafruit_MAX31855.h>
#include < avr/io.h >
#include < avr/interrupt.h >
 
// Code with heater and VacPump functions
// Thermocouple Code using adafruit serial board and K thermocouple
// Assume that Input Voltage is 8 volts
// nprav 03/18/2014

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
double dataPrintCounter = 0;

double kp = 8; //Proportional gain
double ki = 0.05; //Integral gain
double kd = 30; //derivative gain
short total_error = 0;
double current_error = 0;
double diff = 1;
double prev_diff = 1;
int FINAL_TEMP=70; // Final set temperature
int boostBuffer = 7; // initial power boost until Final Temp - boostBuffer
double output = 0;
double prev_temp = 0;
double temp = 0;
bool run = true;
int k;

int pumpPower = 0;

Adafruit_MAX31855 thermocouple(thermoCLK, thermoCS, thermoDO);

void setup() {
  pinMode(led,OUTPUT);
  pinMode(heatPin,OUTPUT);
  pinMode(pumpPin,OUTPUT);
  Serial.begin(9600);
  Serial.println("P2D2");
  // wait for MAX chip to stabilize
  delay(250); // delay 500 ms
  
  while(!Serial.available()){} // Wait for serial prompt
  
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
  
  //Heater Code
  if(heatTest){
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
      prev_temp = readTempC();
      temp = prev_temp;
    }
    
    diff = 10*(temp-prev_temp);
    prev_temp = temp;
    output = kp*current_error+ki*(total_error)-kd*(diff+prev_diff)/20;
    prev_diff = diff;
    
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
    }
    
  }
  
  // Pump Code
  if(pumpTest){
    
    potValue = analogRead(potPin);
    potValue = map(potValue,0,1023,0,255);
    pumpPower = potValue;
    
  }
  
  dataPrintCounter++;
  if(dataPrintCounter >=10){
    SerialPrintData();
    dataPrintCounter = 0;
  }
  //Serial.println(dataPrintCounter);
}

void SerialPrintData(){
  
  if(heatTest){
    Serial.print("C = "); 
    Serial.println(readTempC());
    Serial.print("total_error = ");
    Serial.println(total_error);
    Serial.print("outputkp = ");
    Serial.println(kp*current_error);
    Serial.print("outputki = ");
    Serial.println(ki*(total_error));
    Serial.print("outputkd = ");
    Serial.println(-kd*(diff+prev_diff)/20);
    Serial.print("Output = ");
    Serial.println(output);
    Serial.print("Heat Power = ");
    Serial.println(OCR1A);
  }
  
  if(pumpTest){
    Serial.print("pumpPower = "); 
    Serial.println(OCR1B);
  }
}
