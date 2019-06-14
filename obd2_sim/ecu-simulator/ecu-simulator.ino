#include <mcp_can.h>
#include <SPI.h>

// pin for the SPI_CS
const int SPI_CS_PIN = 9;

// length and buffer for can-protocol
unsigned char len = 0;
unsigned char buf[8];

//variable for the request-message
String request_message = "";

MCP_CAN CAN(SPI_CS_PIN);

//boolean values if readData() is needed
bool engine_load_used = false;
bool rpm_used = false;
bool speed_used = false;
bool throttle_used = false;

unsigned char engine_load_char;
unsigned char low_rpm_char;
unsigned char high_rpm_char;
unsigned char car_speed_char;
unsigned char throttle_pos_char;

void setup(void) {
  //fairly random seed
  randomSeed(analogRead(0) + analogRead(1) + analogRead(2));

  //init communication with computer via USART
  Serial.begin(115200);

  //init can-shield
  Serial.println("init can bus shield");
  while (CAN_OK != CAN.begin(CAN_500KBPS))              // init can bus : baudrate = 500k
  {
    Serial.println("init can bus shield failed");
    Serial.println("init can bus shield again");
    delay(100);
  }
  Serial.println("init can bus shield ok");

  //init sd-card
  setup_sd();
}

void loop(void) {
  //check if requested data
  if (CAN_MSGAVAIL == CAN.checkReceive()) {
    CAN.readMsgBuf(&len, buf);
    int canId = CAN.getCanId();
    for (int i = 0; i < len; i++)
    {
      request_message = request_message + buf[i] + ",";
    }

    Serial.println(request_message);

    if (request_message == "2,1,0,0,0,0,0,0,") {
      //supported pids: engine_load = 0x04, rpm = 0x0C = 12, speed = 0x0D = 12, throttle = 0x11 = 17
      unsigned char supportedPID[8] = {4, 65, 0, 0b00001000, 0b00011000, 0b10000000, 0b00000000};
      CAN.sendMsgBuf(0x7E8, 0, 8, supportedPID);
    }
    if (request_message == "2,1,4,0,0,0,0,0,") {
      if (engine_load_used) {
        updateData();
      }
      engine_load_used = true;
      unsigned char engineLoadSensor[8] = {4, 65, 0x04, engine_load_char, 0, 0, 0, 0};
      Serial.println(String((int)engine_load_char));
      CAN.sendMsgBuf(0x7E8, 0, 8, engineLoadSensor);
    }
    if (request_message == "2,1,12,0,0,0,0,0,") {
      if (rpm_used) {
        updateData();
      }
      rpm_used = true;
      unsigned char rpmSensor[8] = {4, 65, 0x0C, high_rpm_char, low_rpm_char, 0, 0, 0};
      Serial.println(String((int)low_rpm_char));
      CAN.sendMsgBuf(0x7E8, 0, 8, rpmSensor);
    }
    if (request_message == "2,1,13,0,0,0,0,0,") {
      if (speed_used) {
        updateData();
      }
      speed_used = true;
      unsigned char speedSensor[8] = {4, 65, 0x0D, car_speed_char, 0, 0, 0, 0};
      Serial.println(String((int)car_speed_char));
      CAN.sendMsgBuf(0x7E8, 0, 8, speedSensor);
    }
    if (request_message == "2,1,17,0,0,0,0,0,") {
      if (throttle_used) {
        updateData();
      }
      throttle_used = true;
      unsigned char throttleSensor[8] = {4, 65, 0x11, throttle_pos_char, 0, 0, 0, 0};
      Serial.println(String((int)throttle_pos_char));
      CAN.sendMsgBuf(0x7E8, 0, 8, throttleSensor);
    }
    request_message = "";
  }
  //save energy
  delay(10);
}

/**
   reads new data from the sd and updates all data
*/
void updateData() {
  String data = readData();//"s4;76,50%;2306RPM;70km/h;34,90%";
  while(data == "") {
    data = readData();
  }
  Serial.println("driver data: " + data);
  Serial.println("driver data length: " + String(data.length()));

  //get load and trim it
  String engine_load = getValue(data, ';', 1);
  engine_load.replace("%", "");
  engine_load = engine_load.substring(0, engine_load.length() - 3);
  engine_load_char = engine_load.toInt();
  engine_load_char = map(engine_load_char, 0, 100, 0, 255);

  //get rpm and trim it
  String engine_rpm = getValue(data, ';', 2);
  engine_rpm.replace("RPM", "");
  int engine_rpm_int = engine_rpm.toInt();
  low_rpm_char = engine_rpm_int;
  high_rpm_char = engine_rpm_int >> 8;

  //get speed and trim it
  String car_speed = getValue(data, ';', 3);
  car_speed.replace("km/h", "");
  car_speed_char = car_speed.toInt();

  //get throttle and trim it
  String throttle_pos = getValue(data, ';', 4);
  throttle_pos.replace("%", "");
  throttle_pos = throttle_pos.substring(0, throttle_pos.length() - 3);
  throttle_pos_char = throttle_pos.toInt();
  throttle_pos_char = map(throttle_pos_char, 0, 100, 0, 255);

  engine_load_used = false;
  rpm_used = false;
  speed_used = false;
  throttle_used = false;
}

/**
   splits string
*/
String getValue(String data, char separator, int index) {
  //how many did we find
  int found = 0;
  //where is the start of current selection
  int lower_bound = 0;
  //where is the end of current selection
  int upper_bound = -1;
  //how far can we search
  int data_length = data.length() - 1;

  //go throu all chars
  for (int i = 0; i <= data_length; i++) {
    //sometimes (who knows why) charAt(i) returns -1. I didnt know that was even possible (like chars having negative value?)
    //dosent matter, just return ""
    int char_test = data.charAt(i);
    if (char_test == -1) {
      return "";
    }
    //find the seperator
    if (data.charAt(i) == separator) {
      //we found one more separator
      found ++;
      if (found == index) {
        //set lower bound if we found the correct index
        lower_bound = i;
      }
      if (found == index + 1) {
        //set upper bound if there is one
        upper_bound = i;
      }
    }
  }
  String ret = "";
  if (upper_bound == -1) {
    //return rest of string
    ret = data.substring(lower_bound, data_length);
  } else {
    //return lower to upper
    ret = data.substring(lower_bound, upper_bound + 1);
  }
  //remove separator
  ret.replace(";", "");
  return (ret);
}
