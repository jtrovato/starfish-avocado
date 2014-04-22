#include <Arduino.h>
#include <SoftwareSerial.h> 

/*******************************************************************************
 * Definitions 
 ******************************************************************************/
//Header bytes
#define HEADER_IN_1 0xB8
#define HEADER_IN_2 0xD3
#define HEADER_OUT_1 0xFA
#define HEADER_OUT_2 0xCE

//Led status bytes
#define LED_STATE 0x3C
#define LED_ON 0xFF
#define LED_OFF 0x00

// Heating status bytes
#define HEATING_STATE 0x57
#define HEAT_TEMP_1 0x33
#define HEAT_TEMP_2 0x55
#define HEAT_TEMP_3 0x65
#define HEAT_TEMP_4 0xFF
#define STOP_HEATING 0x00
#define HEATING_TEMP_1 0x31
#define HEATING_TEMP_2 0x51
#define HEATING_TEMP_3 0x62
#define HEATING_TEMP_4 0xF7

// Fluid status bytes
#define FLUID_STATE 0x8F
#define FLUIDS_ACTUATE 0xFF
#define FLUIDS_STOP 0x00
#define FLUIDS_NOT_ACTUATED 0x00
#define FLUIDS_ACTUATING 0x44
#define FLUIDS_ACTUATED 0xFF

// Temp data byte
#define TEMP_DATA 0x6A

// Full status request bytes
#define STATUS_REQUEST 0xFF
#define FULL_STATUS 0xA5

//Error bytes 
#define ERROR_MESSAGE 0xEE
#define ERROR_OVERHEAT 0x11
#define ERROR_LOW_VOLTAGE 0xF0


