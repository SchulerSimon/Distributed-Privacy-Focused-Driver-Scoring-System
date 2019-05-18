#include <mcp_can.h>
#include <SPI.h>

// pin for the SPI_CS
const int SPI_CS_PIN = 9;

// length and buffer for CAN-Protocol
unsigned char len = 0;
unsigned char buf[8];

//variable for the request-message
String BuildMessage = "";

MCP_CAN CAN(SPI_CS_PIN);                                    // Set CS pin

void setup()
{
  Serial.begin(115200);

  while (CAN_OK != CAN.begin(CAN_500KBPS))              // init can bus : baudrate = 500k
  {
    Serial.println("CAN BUS Shield init fail");
    Serial.println(" Init CAN BUS Shield again");
    delay(100);
  }
  Serial.println("CAN BUS Shield init ok!");
}

void loop()
{
  unsigned char throttle = random(0, 255);
  unsigned char rpm = random(1, 55);
  unsigned char maf = random(0, 255);
  unsigned char speed_ = random(0,255);

  //GENERAL
 unsigned char SupportedPID[8] = {4, 65, 0, 0b00000000, 0b00011001, 0b10000000, 0b00000000};
 unsigned char MilCleared[7] =   {4, 65, 63, 34, 224, 185, 147};

  //SENSORS
  unsigned char throttleSensor[8] = {4, 65, 0x11, throttle, 0, 0, 0, 0};
  unsigned char rpmSensor[8] =      {4, 65, 0x0C, rpm, 0, 0, 0, 0};
  unsigned char mafSensor[8] =      {4, 65, 0x10, maf, 0, 0, 0, 0};
  unsigned char speedSensor[8] =    {4, 65, 0x0D, speed_, 0, 0, 0, 0};

  if (CAN_MSGAVAIL == CAN.checkReceive()) {
    CAN.readMsgBuf(&len, buf);
    int canId = CAN.getCanId();
    Serial.print("<"); Serial.print(canId, HEX); Serial.print(",");
    for (int i = 0; i < len; i++)
    {
      BuildMessage = BuildMessage + buf[i] + ",";
    }
    Serial.println(BuildMessage);

    //Check wich message was received.
    if (BuildMessage == "2,1,0,0,0,0,0,0,") {
      CAN.sendMsgBuf(0x7E8, 0, 8, SupportedPID);
      Serial.println("Send SupportedPID");
    }
    if (BuildMessage == "2,1,1,0,0,0,0,0,") {
      CAN.sendMsgBuf(0x7E8, 0, 7, MilCleared);
    }

    //SEND SENSOR STATUSES
    if (BuildMessage == "2,1,17,0,0,0,0,0,") {
      CAN.sendMsgBuf(0x7E8, 0, 8, throttleSensor);
    }
    if (BuildMessage == "2,1,16,0,0,0,0,0,") {
      CAN.sendMsgBuf(0x7E8, 0, 8, mafSensor);
    }
    if (BuildMessage == "2,1,12,0,0,0,0,0,") {
      CAN.sendMsgBuf(0x7E8, 0, 8, rpmSensor);
    }
    if (BuildMessage == "2,1,13,0,0,0,0,0,") {
      CAN.sendMsgBuf(0x7E8, 0, 8, speedSensor);
      Serial.println("Send speed");
    }


    BuildMessage = "";
  }
}
