
/*
SOURCE:
https://www.arduino.cc/en/Tutorial/WiFiSendReceiveUDPString


 */


#include <SPI.h>
#include <WiFi.h>
#include <WiFiUdp.h>
#include <Wire.h>
#include <stdio.h>


int status = WL_IDLE_STATUS;
char ssid[] = "AndroidAP"; //  your network SSID (name) 
char pass[] = "holahola";    // your network password (use for WPA, or use as key for WEP)
int keyIndex = 0;            // your network key Index number (needed only for WEP)

unsigned int localPort = 8888;      // local port to listen on
static String gps_positions [] = {"50.1-23.4-","50.1-23.41-","50.1-23.4-","50.1-23.412-","50.1-23.413-","50.1-23.412-","50.1-23.41-","50.1-23.42-","50.1-23.41-","50.1-23.42-","50.1-23.41-","50.11-23.42-","50.12-23.41-"};

char packetBuffer[255]; //buffer to hold incoming packet
char  ReplyBuffer[] = "alive\n";       // a string to send back

WiFiUDP Udp;

boolean flagUp = false;
boolean flagDown = false;
boolean flagNormal = false;

char* buff;

void setup() {
  Wire.begin();
  Serial.begin(9600);
  //Initialize serial and wait for port to open:
  
  // check for the presence of the shield:
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present"); 
    // don't continue:
    while(true);
  } 
  
  // attempt to connect to Wifi network:
  while ( status != WL_CONNECTED) { 
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:    
    status = WiFi.begin(ssid,pass);
  
    // wait 10 seconds for connection:
    delay(10000);
  } 
  Serial.println("Connected to wifi");
  printWifiStatus();
  Serial.println("\nStarting connection to server...");
  // if you get a connection, report back via serial:
  Udp.begin(localPort);  
}

String get_current_position(void){

  static int position = 0;
  if (position == 13) position = 0;
  return gps_positions[position++];
}

void loop() {
  while ( WiFi.status() != WL_CONNECTED ) {
    Serial.println("Trying to reconnect");
    WiFi.begin(ssid, pass);
    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("Reconnected");
    }
    delay(5000);
  }
 
  // if there's data available, read a packet
  int packetSize = Udp.parsePacket();
  String current_pos;
  if(packetSize)
  {   
    //Serial.print("Received packet of size ");
    //Serial.println(packetSize);
    //Serial.print("From ");
    //IPAddress remoteIp = Udp.remoteIP();
    //Serial.print(remoteIp);
    //Serial.print(", port ");
    //Serial.println(Udp.remotePort());

    // read the packet into packetBufffer
    int len = Udp.read(packetBuffer,255);
    if (len >0) packetBuffer[len]=0;
    //Serial.println("Contents:");
    //Serial.println(packetBuffer);
    
    if (packetBuffer[0]=='N'){
      if (!flagNormal)
        Serial.println("Normal");
        flagNormal=true;
    }
    
    else if (packetBuffer[0]=='c' && packetBuffer[1]=='o' && packetBuffer[2]=='n' && packetBuffer[3]=='n' && packetBuffer[4]=='e' && packetBuffer[5]=='c' && packetBuffer[6]=='t'){
      // send a reply, to the IP address and port that sent us the packet we received
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      Udp.write("alive\n");
      Udp.endPacket();
      Serial.println("CONNECTED");
      //start the motors      
    }
    else if (packetBuffer[0]=='R' && packetBuffer[1]=='e' && packetBuffer[2]=='s' && packetBuffer[3]=='t' && packetBuffer[4]=='o' && packetBuffer[5]=='r' && packetBuffer[6]=='e'){
      // send a reply, to the IP address and port that sent us the packet we received
      Serial.println("INTERNET RESTORED");
      //start the motors      
    }
    else if (packetBuffer[0]=='S' && packetBuffer[1]=='t' && packetBuffer[2]=='o' && packetBuffer[3]=='p'){
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      Udp.write("Stop\n");
      Udp.endPacket();
      Serial.println("STOP");
      //stop the motors       
    }
    else if (packetBuffer[0]=='G' && packetBuffer[1]=='P' && packetBuffer[2]=='S'){
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      current_pos = get_current_position();
      buff = (char*) malloc(sizeof(char)*current_pos.length()+1);
      current_pos.toCharArray(buff,current_pos.length()+1);
      Serial.println(buff);
      
      Udp.write(buff);
      Udp.endPacket();
      free(buff);
      Serial.println("GPS");
      //receive GPS location 
    }
    else if (packetBuffer[0]=='R' && packetBuffer[1]!='R' && packetBuffer[1]!='L'){
      Serial.println("RIGHT");
      //send PWM to go right      
    }
    else if (packetBuffer[0]=='L' && packetBuffer[1]!='V'){
      Serial.println("LEFT");    
      //send PWM to go left  
    }
    else if (packetBuffer[0]=='B'){
      Serial.println("BACKWARD");
      //send PWM to move backward      
    }
    else if (packetBuffer[0]=='F'){
      Serial.println("FORWARD"); 
      //send PWM to move forward     
    }
    else if (packetBuffer[0]=='R' && packetBuffer[1]=='L'){
      Serial.println("Rotate Left"); 
      //send PWM to rotate left     
    }
    else if (packetBuffer[0]=='R' && packetBuffer[1]=='R'){
      Serial.println("Rotate Right");
      //send PWM to rotate right   
    }
    else if (packetBuffer[0]=='L' && packetBuffer[1] == 'V'){
      Serial.println("Less Velocity"); 
      //decrease velocity     
    }
    else if (packetBuffer[0]=='M' && packetBuffer[1]=='V'){
      Serial.println("More Velocity");    
      //increase velocity  
    }
    else if (packetBuffer[0]=='W' && packetBuffer[1]=='e' && packetBuffer[2]=='a' && packetBuffer[3]=='t' && packetBuffer[4]=='h' && packetBuffer[5]=='e' && packetBuffer[6]=='r'){
      // send a reply, to the IP address and port that sent us the packet we received
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      Udp.write("GPS-Humidity-Speed-Temp-\n");
      Udp.endPacket();
      Serial.println("WEATHER");
    }
    if (!flagUp && flagNormal){
      if (packetBuffer[0]=='U'){
        Serial.println("UP");
        flagUp=true;
        flagDown=false;
        flagNormal=false;
      }
    }
    if (!flagDown && flagNormal){
      if (packetBuffer[0]=='D'){
        Serial.println("DOWN");  
        flagUp=false;
        flagDown=true;
        flagNormal=false;
      } 
    }
    if (flagUp && !flagNormal){
      //send PWM up
    }
    else if (flagDown && !flagNormal){
      //send PWM down
    }
    
   }
}




void printWifiStatus() {
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your WiFi shield's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}




