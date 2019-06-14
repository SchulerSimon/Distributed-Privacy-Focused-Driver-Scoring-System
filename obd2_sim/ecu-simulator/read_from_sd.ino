#include <SPI.h>
#include <SD.h>

const int SD_CS_PIN = 4;

File my_file;
String file_name;


void setup_sd(void) {
  Serial.println("init sd-card");
  while(!SD.begin(SD_CS_PIN)){
    Serial.println("init sd-card failed");
    Serial.println("retry init sd-card");
    delay(100);
  }
  Serial.println("init sd-card ok");
}

bool openNewFile() {
  //there are 19 driver files, open just a random one
  file_name = "driver" + String(random(18) + 1) + ".txt";
  my_file = SD.open(file_name);
  if(my_file) {
    Serial.println("open " + file_name + " successfull");
    return true;
  } else {
    Serial.println("open " + file_name + " failed");
    return false;
  }
}

String readData() {
  String temp = readLine();
  if(temp == "") {
    while(!openNewFile()) {
      Serial.println("failed to open new file");
      Serial.println("retrying to open new file");
      delay(50);
    }
    temp = readLine();
  }
  if(temp == "\n") {
    temp = readLine();
  }
  return temp;
}

String readLine() {
  String received = "";
  char ch;
  while (my_file.available())
  {
    ch = my_file.read();
    if (ch == '\n')
    {
      return String(received);
    }
    else
    {
      received += ch;
    }
  }
  return "";
}
