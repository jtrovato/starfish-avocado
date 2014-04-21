/*******************************************************************************
 * p2d2.ino
 *  
 * Authors: Andrew Botelho, Praveer Nidamaluri, Sylvester Tate, Wenbin Zhao
 * Bluetooth communications based off "Example Bluetooth Serial Passthrough 
 * Sketch" by Jim Lindblom of SparkFun Electronics
 * Last update: 2014-04-17
 * 
 * This sketch is run on the Arduino Duemilanove inside the P2D2 diagnostic 
 * device. It controls the device's heating, lighting, fluid actuation, and 
 * bluetooth communication with a smartphone. 
 * Code with heater and VacPump functions
 * Thermocouple Code using adafruit serial board and K thermocouple
 * // Assume that Input Voltage is 8 volts
 ******************************************************************************/

#include "bluetooth.h"
#include "Tag.h"
#include <SoftwareSerial.h>  
#include <Adafruit_MAX31855.h>
#include <avr/io.h>
#include <avr/interrupt.h>

/******************************************************************************
 * Variable initializations
 ******************************************************************************/
// Bluetooth pins
const int bluetoothTx = 6 ;  // TX-O pin of bluetooth mate, Arduino D2
const int bluetoothRx = 5;  // RX-I pin of bluetooth mate, Arduino D3


// Other Bluetooth Variables
int length; //length of packet
int cmd; //command selection from bluetooth
int srcmd; //status requests
int heatingcmd; //heating commands
int ledcmd; //led commands
int fluidscmd; //fluid commands
int minPacketSize = 5;
SoftwareSerial bluetooth(bluetoothTx, bluetoothRx); // connection to Blue SmiRF

// Heating/pump/lighting variables 
boolean heatingOn = false;
boolean stable = false;
boolean pumpTest = true;

const int thermoDO = 2;
const int thermoCS = 2;
const int thermoCLK = 4;
const int heatPin = 11;
const int pumpPin = 9;
//const int potPin = A5;
const int led = 8;

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
byte fluid_state= FLUIDS_NOT_ACTUATED;

int pumpPower = 0;

Adafruit_MAX31855 thermocouple(thermoCLK, thermoCS, thermoDO);


// Runs before loop()
void setup()
{
  // Pump/heater/light setup
  pinMode(led,OUTPUT);
  pinMode(heatPin,OUTPUT);
  pinMode(pumpPin,OUTPUT);
  Serial.begin(9600);
  Serial.println("P2D2 Process Monitoring:");
  // wait for MAX chip to stabilize
  delay(250); // delay 500 ms

  // Wait for serial prompt
  //while(!Serial.available()){
  //} 

  // Set up heat PWM for pin 9, Set for 8 bit Fast PWM (approx 1000Hz)
  TCCR1B = TCCR1B | 0b00001011;
  TCCR1A = TCCR1A | 0b10100001;
  OCR1A = 0;    // Controls heater power
  OCR1B = 0;    // Controls pump power
  TIMSK1 = TIMSK1 | 0b00000001; // Enable interrupt when timer overflows

  // Bluetooth setup
  Serial.begin(9600);  // Begin the serial monitor at 9600bps
  bluetooth.begin(9600);  // Start bluetooth serial at 9600
}

// Timer interrupt which regularly sets heater, pump PWM signals
ISR(TIMER1_OVF_vect){
  OCR1A = heatPower;
  OCR1B = pumpPower;
}

// reads in temperature from thermocouple
double readTempC(){
  //noInterrupts();
  return (thermocouple.readCelsius());
}

// Runs continuously after setup
void loop(){
  //Heater Code
  if(heatingOn){
    // Get temperature readings
    double temp = readTempC();
    if(current_error>0){
      total_error = total_error+current_error;
    }
    else{
      total_error = total_error+(current_error);
    }
    current_error = FINAL_TEMP-temp;

    if(k==0 && temp<FINAL_TEMP-boostBuffer){
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

    //potValue = analogRead(potPin);
    //potValue = map(potValue,0,1023,0,255);
    //pumpPower = potValue;

  }

  dataPrintCounter++;
  if(dataPrintCounter >=10){
    //SerialPrintData();
    dataPrintCounter = 0;
  }
  //Serial.println(dataPrintCounter);

  // If bluetooth receiverd at least minPacketSize characters
  if(bluetooth.available() >= minPacketSize)  
  {
    // Serial.print("Checking for bluetooth\r\n");
    processPacket();
  }
}

// Processes status requests received over bluetooth
void status_requests(int cmd){
  switch(cmd){
  case LED_STATE: 
    {
      Tag ledTag;
      if(bitRead(PORTB, 0) == HIGH){
        ledTag.setValues(LED_STATE, LED_ON);
      } 
      else {
        ledTag.setValues(LED_STATE, LED_OFF);
      }
      //Serial.println((int) ledTag.getType());
      Tag tagArray[] = {
        ledTag      };      
      writeToBT(tagArray, 0x01);
      //Serial.println("//led state request");
      break;
    }
  case HEATING_STATE: 
    {
      Tag heatStateTag;
      if(heatingOn){
        if(stable){
          heatStateTag.setValues(HEATING_STATE, HEAT_TEMP_1);
        }
        else{
          heatStateTag.setValues(HEATING_STATE, HEATING_TEMP_1);
        }
      } 
      else {
        heatStateTag.setValues(HEATING_STATE, STOP_HEATING);
      }
      //Serial.println((int) heatStateTag.getType());
      Tag tagArray[] = {        
        heatStateTag                                        };      
      writeToBT(tagArray, 0x01);
      //Serial.println(" //heating state request");
      break;
    }
  case FLUID_STATE:
    {
      Tag fluidsTag;
      fluidsTag.setValues(FLUID_STATE, fluid_state);
      Tag tagArray[] = {
        fluidsTag                                          };
      writeToBT(tagArray, 0x01);
      Serial.println("//fluid acutation state request");
      break;
    }
  case TEMP_DATA:
    Serial.println("//temp data request");
    break;
  case FULL_STATUS:
    Serial.println("//full status (all state values and data)");
    break;
  default:
    Serial.println("invalid input received");
  }
} 

// Processes heating commands received over bluetooth
void heating_commands(int cmd){
  Serial.println("Heating command");
  switch(cmd){
  case HEAT_TEMP_1: //this is our nominal testing value
    {
      //Start/switch heating-- temp 1
      Serial.println("Start/switch heating-- temp 1 - LAMP temp");
      break;  
    }
  case HEAT_TEMP_2:
    {
      //Start/switch heating-- temp 2
      Serial.println("Start/switch heating-- temp 2");
      break;  
    }
  case HEAT_TEMP_3:
    {
      //Start/switch heating-- temp 3
      Serial.println("Start/switch heating-- temp 3");
      break;  
    }
  case HEAT_TEMP_4:
    {
      //Start/switch heating-- temp 4
      Serial.println("Start/switch heating-- temp 4");
      break;  
    }
  case STOP_HEATING:
    {
      //Stop heating
      Serial.println("Stop heating");
      break;  
    }
  default:
    {
      //Serial.println("default Case"); 
    }
  }
}

// Processes LED commands received over bluetooth
void led_command(int cmd){
  if (cmd == LED_ON){
    Serial.print("//Leds on\r\n");
    digitalWrite(led,HIGH);
  }
  else if(cmd == LED_OFF){
    Serial.print("//Leds off\r\n");
    digitalWrite(led,LOW);
  }
}  

// Processes fluid actuation commands received over bluetooth
void fluids_command(int cmd){
  if (cmd == FLUIDS_ACTUATE){
    //Start fluids actuation
    Serial.println("Start fluid actuation");
    delay(5000); //replace with actuall function later
    Serial.println("fluid actuation complete");
    fluid_state = FLUIDS_ACTUATED;
    
    
  }
  else if(cmd == FLUIDS_STOP){
    //probably not going to do anything here because the fluids automatically stop when the FA function returns.
    //Stop fluids actuation
    //Serial.print("Stop fluid actuation");
  }
}  

// Processes packets received over bluetooth
void processPacket(){
  //Serial.print("Debug 2\r\n");
  if(bluetooth.read() == HEADER_IN_1){
    //Serial.print("first byte received\r\n");
    if(bluetooth.read() == HEADER_IN_2){
      //Serial.print("second byte received\r\n");
      length = bluetooth.read();
      //Serial.println(length);
      for( int i = 1; i <= length; i++){
        cmd = bluetooth.read();
        switch(cmd){
        case STATUS_REQUEST:
          {
            srcmd = bluetooth.read();
            status_requests(srcmd);
            break;
          }
        case HEATING_STATE:
          {
            heatingcmd = bluetooth.read();
            heating_commands(heatingcmd);
            break;
          }
        case LED_STATE:
          {
            ledcmd = bluetooth.read();
            led_command(ledcmd);
            break;
          }
        case FLUID_STATE:
          {
            fluidscmd = bluetooth.read();
            fluids_command(fluidscmd);
            break;
          }
        }
      }

    }
    else{
      //bluetooth.flush();
    }
  }
  else{
    //bluetooth.flush();
  }
}

void writeToBT(Tag tagArray[], byte size){
  bluetooth.write(HEADER_OUT_1);
  bluetooth.write(HEADER_OUT_2);
  bluetooth.write(size);

  for(int i = 0; i< size; i++){
    byte type = tagArray[i].getType();
    bluetooth.write(type);
    bluetooth.write(tagArray[i].getData1());
    if(type == TEMP_DATA){
      bluetooth.write(tagArray[i].getData2());
    }
  }
}

void SerialPrintData(){
  if(heatingOn){
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
  else{
    Serial.print("C = "); 
    Serial.println(readTempC());
  }

  if(pumpTest){
    Serial.print("pumpPower = "); 
    Serial.println(OCR1B);
  }
}





















