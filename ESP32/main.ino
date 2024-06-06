/*
Arduino-MAX30100 oximetry / heart rate integrated sensor library
Copyright (C) 2016  OXullo Intersecans <x@brainrapers.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

// The example shows how to retrieve raw values from the sensor
// experimenting with the most relevant configuration parameters.
// Use the "Serial Plotter" app from arduino IDE 1.6.7+ to plot the output

#include <Wire.h>
#include "MAX30100.h"

// Sampling is tightly related to the dynamic range of the ADC.
// refer to the datasheet for further info
#define SAMPLING_RATE                       MAX30100_SAMPRATE_100HZ

// The LEDs currents must be set to a level that avoids clipping and maximises the
// dynamic range
#define IR_LED_CURRENT                      MAX30100_LED_CURR_50MA
#define RED_LED_CURRENT                     MAX30100_LED_CURR_27_1MA

// The pulse width of the LEDs driving determines the resolution of
// the ADC (which is a Sigma-Delta).
// set HIGHRES_MODE to true only when setting PULSE_WIDTH to MAX30100_SPC_PW_1600US_16BITS
#define PULSE_WIDTH                         MAX30100_SPC_PW_1600US_16BITS
#define HIGHRES_MODE                        true


// Instantiate a MAX30100 sensor class
MAX30100 sensor(21, 22);

void setup()
{
    
    Serial.begin(115200);

    Serial.print("Initializing MAX30100..");

    // Initialize the sensor
    // Failures are generally due to an improper I2C wiring, missing power supply
    // or wrong target chip
    if (!sensor.begin()) {
        Serial.println("FAILED");
        for(;;);
    } else {
        Serial.println("SUCCESS");
    }

    // Set up the wanted parameters
    sensor.setMode(MAX30100_MODE_SPO2_HR);
    sensor.setLedsCurrent(IR_LED_CURRENT, RED_LED_CURRENT);
    sensor.setLedsPulseWidth(PULSE_WIDTH);
    sensor.setSamplingRate(SAMPLING_RATE);
    sensor.setHighresModeEnabled(HIGHRES_MODE);
}

void loop()
{
    static unsigned long startTime = millis(); // Startzeit für die Aggregation
    static unsigned int count = 0; // Anzahl der aggregierten Werte
    static uint32_t irTotal = 0; // Summe der IR-Werte
    static uint32_t redTotal = 0; // Summe der roten Werte
    
    uint16_t ir, red;
    
    sensor.update();

    // Zeitpunkt seit Start der Aggregation
    unsigned long elapsedTime = millis() - startTime;
    
    // Prüfen, ob 100ms abgelaufen sind
    if (elapsedTime >= 100) {
        // Werte abrufen
        if (sensor.getRawValues(&ir, &red)) {
            irTotal += ir;
            redTotal += red;
            count++;
        }
        
        // Wenn 100ms abgelaufen sind
        if (elapsedTime >= 100) {
            // Durchschnitt berechnen
            if (count > 0) {
                uint16_t irAvg = irTotal / count;
                uint16_t redAvg = redTotal / count;

                // Durchschnitt ausgeben
                Serial.print(irAvg);
                Serial.print('\t'); // Tabulator einfügen
                Serial.println(redAvg);
            } else {
                Serial.println("Keine Werte erhalten.");
            }
            
            // Zurücksetzen für die nächste Aggregation
            startTime = millis();
            count = 0;
            irTotal = 0;
            redTotal = 0;
        }
    }
}
