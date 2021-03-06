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
  boolean actuating = false;
  
  const int thermoDO = 3;
  const int thermoCS = 4;
  const int thermoCLK = 5;
  
  const int valveFTA = 8; //FTA Wash
  const int valveLAMP = 2; //LAMP/P5
  const int pumpPin = 10;
  const int potPin = A5;
  const int led = 13;
  
  int heatMax = 255;
  int heatPower = 0;
  int potValue = 0;
  unsigned long dataPrintCounter = 0;
  char input = 0;
  
  // Fluid Actuation Variables
  int pumpPower = 0;
  int pumpMax = 255;
  int pumpFTA = 200;
  int pumpLAMP = 100;
  char actuationStage = 0;
  unsigned long actuationTimer = 0;
  
  
  Adafruit_MAX31855 thermocouple(thermoCLK, thermoCS, thermoDO);
  
  void setup() {
    pinMode(led,OUTPUT);
    pinMode(valveFTA,OUTPUT);
    pinMode(valveLAMP,OUTPUT);
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
    // Overflows 16Mhz/64 = 250,000 times a second
    OCR1B = pumpPower;
  }
  
  
  double readTempC(){
    //noInterrupts();
    return (thermocouple.readCelsius());
  }
  
  void FluidActuationStart(){
    Serial.println("Starting Fluid Actuation");
    actuating = true;
  }
  
  void FluidActuationEnd(){
    Serial.println("Fluid Actuation Complete");
    // Send bluetooh command
  }
    
  
  void loop() {
    
    if (Serial.available() > 0) {
      // read the incoming byte:
     input = Serial.read();
      switch(input){
        case 'vF':
          digitalWrite(valveFTA,!digitalRead(valveFTA));
          break;
        case 'vL':
          digitalWrite(valveLAMP,!digitalRead(valveLAMP));
          break;
      }
        // say what you got:
      Serial.print("I received: ");
      Serial.println(input);
    }
    
    // **********************************
    // * Automatic Fluid Actuation Code *
    // **********************************
    
    if(actuating){ 
      switch(actuationStage){
        case 0: 
          //Just started - switch on FTA valve, wait 20s, move to stage 1        
          digitalWrite(valveFTA,1);
          actuationTimer = millis();
          if((millis()-actuationTimer)>=(1000*20)){
            digitalWrite(valveFTA,0);
            actuationStage = 1;
          }
          break;
        case 1:
          //start pump, wait until FTA/Ethanol has completely flushed RC (1min)
          pumpPower = pumpFTA;
          actuationTimer = millis();
          if((millis()-fluidTimer)>(1000*60)){
            pumpPower = 0;
            actuationStage = 2;
          }
          break;
        case 2:
          // FTA Wash/Ethanol has gone through RC completely. Now need to open LAMP channels
          digitalWrite(valveLAMP,1);
          actuationTimer = millis();
          if((millis()-actuationTimer)>(1000*60)){
            pumpPower = 0;
            actuationStage = 3;
          }
          break;
        case 3:
          // pump LAMP stuff ****NEED MORE TESTING****
          pumpPower = pumpLAMP;
          actuationTimer = millis();
          if((millis()-actuationTimer)>(1000*2)){
            pumpPower = 0;
            actuating = false;
            FluidActuationEnd();
            // Send finished actuation command
         }
         break;
      }
            
                   
//    potValue = analogRead(potPin);
//    potValue = map(potValue,0,1023,0,pumpMax);
//    pumpPower = potValue;
    
    if((millis() - dataPrintCounter)>=1000){
      SerialPrintData();
      dataPrintCounter = millis();
    }
    //Serial.println(dataPrintCounter);
  }
  
  void SerialPrintData(){
  
    Serial.println(pumpPower);
    
    if(heatTest){
      Serial.print("C = "); 
      Serial.println(readTempC());
    }
    
    if(pumpTest){
      Serial.print("pumpPower = "); 
      Serial.println(OCR1B);
    }
    Serial.print("valveFTA (FTA Wash) = ");
    Serial.println(digitalRead(valveFTA));
    Serial.print("valveLAMP (LAMP P5) = ");
    Serial.println(digitalRead(valveLAMP));
  }
