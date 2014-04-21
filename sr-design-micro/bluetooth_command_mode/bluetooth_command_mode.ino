/*
  Example Bluetooth Serial Passthrough Sketch
 by: Jim Lindblom
 SparkFun Electronics
 date: February 26, 2013
 license: Public domain
 
 This example sketch converts an RN-42 bluetooth module to
 communicate at 9600 bps (from 115200), and passes any serial
 data between Serial Monitor and bluetooth module.
 */
#include <SoftwareSerial.h>  

int bluetoothTx = 2;  // TX-O pin of bluetooth mate, Arduino D2
int bluetoothRx = 3;  // RX-I pin of bluetooth mate, Arduino D3
int length; //length of packet
int cmd; //command selection from bluetooth
int srcmd; //status requests
int heatingcmd; //heating commands
int ledcmd; //led commands
int fluidscmd; //fluid commands


SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup()
{
  Serial.begin(9600);  // Begin the serial monitor at 9600bps

  bluetooth.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  bluetooth.print("$");  // Print three times individually
  bluetooth.print("$");
  bluetooth.print("$");  // Enter command mode
  delay(100);  // Short delay, wait for the Mate to send back CMD
  bluetooth.println("U,9600,N");  // Temporarily Change the baudrate to 9600, no parity
  // 115200 can be too fast at times for NewSoftSerial to relay the data reliably
  bluetooth.begin(9600);  // Start bluetooth serial at 9600
}

void loop(){
  if(bluetooth.available())  // If the bluetooth sent any characters
  {
    // Send any characters the bluetooth prints to the serial monitor
    //Serial.print((char)bluetooth.read());  
    if(bluetooth.read() == 0xB8 ){
      Serial.print("first byte received\r\n");
      if(bluetooth.read() == 0xD3){
        Serial.print("second byte received\r\n");
        length = bluetooth.read();
        Serial.print(length);
        for( int i = 1; i <= length; i++){
          cmd = bluetooth.read();
          if (cmd == 0xFF){
            srcmd = bluetooth.read();
            status_requests(srcmd);
          }
          else if (cmd ==0x57){
            heatingcmd = bluetooth.read();
            heating_commands(heatingcmd);
          }
          else if (cmd == 0x3C){
            ledcmd = bluetooth.read();
            led_command(ledcmd);
          }
          else if (cmd == 0x8F){
            fluidscmd = bluetooth.read();
            fluids_command(fluidscmd);
          }
        }

      }
      else{
        bluetooth.flush();
      }

    }
    else{
      bluetooth.flush();
    } 
    if(Serial.available())  // If stuff was typed in the serial monitor
    {
      // Send any characters the Serial monitor prints to the bluetooth
      bluetooth.print((char)Serial.read());
    }
    // and loop forever and ever!
  }
}

void status_requests(int cmd){
  if (cmd == 0x3C){
    Serial.print("//led state request");
  }
  else if(cmd == 0x57){
   Serial.print(" //heating state request");
  }
  else if (cmd == 0x8F){
   Serial.print("//fluid acutation state request");
  }
  else if (cmd == 0x6A){
   Serial.print("//temp data request");
  }
  else if (cmd == 0xA5){
    Serial.print("//full status (all state values and data)");
  }else{
    Serial.print("invalid input received");
  }
} 

void heating_commands(int cmd){
  if (cmd == 0x33){
    //Start/switch heating-- temp 1
    Serial.print("Start/switch heating-- temp 1\r\n");  
  }
  else if(cmd == 0x55){
    //Start/switch heating-- temp 2
     Serial.print("Start/switch heating-- temp 2\r\n");
  }
  else if (cmd == 0x66){
    //Start/switch heating-- temp 3
    Serial.print("Start/switch heating-- temp 3\r\n");
  }
  else if (cmd == 0xFF){
    //Start/switch heating-- temp 4
    Serial.print("Start/switch heating-- temp 4\r\n");
  }
  else if (cmd == 0x00){
    //Stop heating
    Serial.print("Stop heating \r\n");
  }
}

void led_command(int cmd){
  if (cmd == 0xFF){
    Serial.print("//Leds on\r\n");
  }else if(cmd == 0x00){
     Serial.print("//Leds off\r\n");
  }
}  

void fluids_command(int cmd){
  if (cmd == 0xFF){
    //Start fluids actuation
   Serial.print("Start fluids actuation");
  }else if(cmd == 0x00){
    //Stop fluids actuation
     Serial.print("Stop fluids actuation");
  }
}  


