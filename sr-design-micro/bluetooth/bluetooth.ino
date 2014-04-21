/*
 * Bluetooth Connectivity with Blue SMiRF
 * Author: Andrew Botelho
 * Initial code based on sampe code by Jim Lindblom of SparkFun Electronics
 * last updated: 2014-01-28
 */

#include <SoftwareSerial.h>

int bluetoothTx = 6;  // TX-O pin of bluetooth mate, Arduino D2
int bluetoothRx = 5;  // RX-I pin of bluetooth mate, Arduino D3
SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup()
{
  Serial.begin(9600);  // Begin the serial monitor at 9600bps

  bluetooth.begin(9600);  // The Bluetooth Mate defaults to 115200bps
  //bluetooth.print("$");  // Print three times individually
  //bluetooth.print("$");
  //bluetooth.print("$");  // Enter command mode
  //delay(100);  // Short delay, wait for the Mate to send back CMD
  //bluetooth.println("SU,96");  // Temporarily Change the baudrate to 9600, no parity
  // 115200 can be too fast at times for NewSoftSerial to relay the data reliably
  //bluetooth.begin(9600);  // Start bluetooth serial at 9600
}

void loop()
{
  if(bluetooth.available())  // If the bluetooth sent any characters
  {
    // Send any characters the bluetooth prints to the serial monitor
    Serial.print((char)bluetooth.read());  
  }
  if(Serial.available())  // If stuff was typed in the serial monitor
  {
    char in = Serial.read();
    // Send any characters the Serial monitor prints to the bluetooth
    if(in == 'a'){
      // Header
      bluetooth.print((char)0xFA);
      bluetooth.print((char)0xCE);
      bluetooth.print((char)0x05);

      // Temp Data
      bluetooth.print((char)0x6A);
      bluetooth.print((char)0x04);
      bluetooth.print((char)0x25);

      // Heating State
      bluetooth.print((char)0x57);
      bluetooth.print((char)0xFF);

      // LED State
      bluetooth.print((char)0x3C);
      bluetooth.print((char)0xFF);

      // Fluid Actuation State
      bluetooth.print((char)0x8F);
      bluetooth.print((char)0x00);

      // Error
      bluetooth.print((char)0xEE);
      bluetooth.print((char)0x11);
    }
    else if (in == 'b'){
      Serial.println("got b");
      bluetooth.print((char)0xFA);
      bluetooth.print((char)0xCE);
      bluetooth.print((char)0x01);
      bluetooth.print((char)0x8F);
      bluetooth.print((char)0xFF);
    }
    Serial.flush();
    //bluetooth.print((char) Serial.read());
  }
  // and loop forever and ever!
}


