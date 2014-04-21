  #include <Adafruit_MAX31855.h>
  #include < avr/io.h >
  #include < avr/interrupt.h >
  
  /////////////////////////////////////////////////////////////////////// 
  // Code that automates the Fluid Actuation 
  // Thermocouple Code using adafruit serial board and K thermocouple
  // Assume that Input Voltage is 5 volts
  // nprav 04/21/2014
  ////////////////////////////////////////////////////////////////////////
  
  // Arduino Board Pin Definitions
  const int thermoDO = 3;
  const int thermoCS = 4;
  const int thermoCLK = 5;
  
  const int valveFTA = 8; //FTA Wash
  const int valveLAMP = 2; //LAMP/P5
  const int pumpPin = 10;
  const int potPin = A5;
  const int led = 13;
  
  // Heating Definitions
  int heatMax = 255;
  int heatPower = 0;
  
  // Misc. Definitions
  int potValue = 0;
  unsigned long dataPrintCounter = 0;
  char input = 0;
  
  ////////////////////////////////
  // Fluid Actuation Definitions /
  ////////////////////////////////
  
  boolean actuating = false;
  int pumpPower = 0;
  
  // Power Maximums
  const int pumpMax = 255;
  const int pumpFTA = 200;
  const int pumpLAMP = 100;
  
  // Variable defining stage of Fluid Actuation
  char actuationStage = 0;
  
  // Actuation stage timer
  unsigned long actuationTimer = 0;
  
  // Time for each actuation stage in seconds
  const int FTAValveTime = 20;
  const int FTAPumpTime = 60;
  const int LampValveTime = 20;
  const int LampPumpTime = 2;
  
  
  Adafruit_MAX31855 thermocouple(thermoCLK, thermoCS, thermoDO);
  
  void setup() {
    pinMode(led,OUTPUT);
    pinMode(valveFTA,OUTPUT);
    pinMode(valveLAMP,OUTPUT);
    pinMode(pumpPin,OUTPUT);
    Serial.begin(9600);
    Serial.println("P2D2 FluidActuatioCode");
    // wait for MAX chip to stabilize
    delay(250); // delay 500 ms
    
    while(!Serial.available()){} // Wait for serial prompt
    Serial.read();
    
    // Set up heat PWM for pin 9, Set for 8 bit Fast PWM (16Mhz/64 (prescalar)/255 = 980)
    // Set up pump PWM for pin 10, Set for 8 bit FAST PWM (16Mhz/64 (prescalar)/255 = 980)
    TCCR1B = TCCR1B | 0b00001011;
    TCCR1A = TCCR1A | 0b10100001;
    OCR1A = 0;    // Controls heater power
    OCR1B = 0;    // Controls pump power
    TIMSK1 = TIMSK1 | 0b00000001; // Enable interrupt when timer overflows
  }
  
  ISR(TIMER1_OVF_vect){
    // Overflows 16Mhz/64 = 250,000 times a second
    OCR1B = pumpPower;
  }
  
  double readTempC(){
    return (thermocouple.readCelsius());
  }
  
  void FluidActuationStart(){
    Serial.println("Starting Fluid Actuation");
    actuating = true;
  }
  
  void FluidActuationEnd(){
    Serial.println("Fluid Actuation Complete");
    actuating = false;
    actuationStage = 0;
    // Send bluetooth command
  }
    
  
  void loop() {

    if (Serial.available() > 0) {
      // read the incoming byte:
     input = Serial.read();
      switch(input){
        case 'k':
          FluidActuationStart();
          break;
        case 's':
          FluidActuationEnd();
          break;
        case 'p':
          actuating = false;
          break;
      }
        // say what you got:
      Serial.print("I received: ");
      Serial.println(input);
    }
    
    
    ////////////////////////////////////
    // Automatic Fluid Actuation Code //
    ////////////////////////////////////
    
    if(actuating){ 
      switch(actuationStage){
        case 0: 
          //Just started - switch on FTA valve, wait for some time, move to stage 1        
          digitalWrite(valveFTA,1);
          actuationTimer = millis();
          if((millis()-actuationTimer)>=(1000*FTAValveTime/2)){
            actuationStage = 1;
          }
          break;
        case 1:
          //start pump, wait for some time, switch of valve heater, wait until FTA/Ethanol has completely flushed RC (1min)
          pumpPower = pumpFTA;
          actuationTimer = millis();
          if((millis()-actuationTimer)>(1000*FTAValveTime/2)){
            digitalWrite(valveFTA,0);
          }
          if((millis()-actuationTimer)>(1000*FTAPumpTime)){
            pumpPower = 0;
            actuationStage = 2;
          }
          break;
        case 2:
          // FTA Wash/Ethanol has gone through RC completely. Now need to open LAMP channels
          digitalWrite(valveLAMP,1);
          actuationTimer = millis();
          if((millis()-actuationTimer)>(1000*LampValveTime/2)){
            pumpPower = 0;
            actuationStage = 3;
          }
          break;
        case 3:
          // pump LAMP stuff, close valve heater after some time ****NEED MORE TESTING****
          pumpPower = pumpLAMP;
          actuationTimer = millis();
          if((millis()-actuationTimer)>(1000*LampValveTime/2)){
            digitalWrite(valveLAMP,0);
          }
          if((millis()-actuationTimer)>(1000*LampPumpTime)){
            pumpPower = 0;
            FluidActuationEnd();
            // Send finished actuation command
         }
         break;
      }
    }
            
   
    if((millis() - dataPrintCounter)>=1000){
      SerialPrintData();
      dataPrintCounter = millis();
    }
 }
  
  void SerialPrintData(){
    
    Serial.print("Actuation Stage = ");
    Serial.println(actuationStage);
    
    Serial.print("pumpPower = "); 
    Serial.println(OCR1B);
    
    Serial.print("valveFTA (FTA Wash) = ");
    Serial.println(digitalRead(valveFTA));
    
    Serial.print("valveLAMP (LAMP P5) = ");
    Serial.println(digitalRead(valveLAMP));
    
    Serial.print("C = "); 
    Serial.println(readTempC());
    
  }
