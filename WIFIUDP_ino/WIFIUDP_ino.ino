
/*
  WiFi UDP Send and Receive String
 
 This sketch wait an UDP packet on localPort using a WiFi shield.
 When a packet is received an Acknowledge packet is sent to the client on port remotePort
 
 Circuit:
 * WiFi shield attached
 
 created 30 December 2012
 by dlf (Metodo2 srl)

 */


#include <SPI.h>
#include <WiFi.h>
#include <WiFiUdp.h>
#include <Wire.h>
#include <stdio.h>

#define LED 13

int status = WL_IDLE_STATUS;
char ssid[] = "Romesco wifi"; //  your network SSID (name) 
char pass[] = "marinasilvia";    // your network password (use for WPA, or use as key for WEP)
int keyIndex = 0;            // your network key Index number (needed only for WEP)

unsigned int localPort = 8888;      // local port to listen on

char packetBuffer[255]; //buffer to hold incoming packet
char  ReplyBuffer[] = "alive\n";       // a string to send back

WiFiUDP Udp;



void setup() {
  Wire.begin();
  Serial.begin(9600); 
  pinMode(LED, OUTPUT);
  digitalWrite(LED, LOW);
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
    status = WiFi.begin(ssid, pass);
  
    // wait 10 seconds for connection:
    delay(10000);
  } 
  Serial.println("Connected to wifi");
  printWifiStatus();
  
  Serial.println("\nStarting connection to server...");
  // if you get a connection, report back via serial:
  Udp.begin(localPort);  
}

void loop() {
   digitalWrite(LED, HIGH);
    
  // if there's data available, read a packet
  int packetSize = Udp.parsePacket();
  if(packetSize)
  {   
    Serial.print("Received packet of size ");
    Serial.println(packetSize);
    Serial.print("From ");
    IPAddress remoteIp = Udp.remoteIP();
    Serial.print(remoteIp);
    Serial.print(", port ");
    Serial.println(Udp.remotePort());

    // read the packet into packetBufffer
    int len = Udp.read(packetBuffer,255);
    if (len >0) packetBuffer[len]=0;
    Serial.println("Contents:");
    Serial.println(packetBuffer);
    
    if (packetBuffer[0]=='c' && packetBuffer[1]=='o' && packetBuffer[2]=='n' && packetBuffer[3]=='n' && packetBuffer[4]=='e' && packetBuffer[5]=='c' && packetBuffer[6]=='t'){
      // send a reply, to the IP address and port that sent us the packet we received
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      Udp.write(ReplyBuffer);
      Udp.endPacket();
      Serial.println("CONNECTED");      
    }
    else if (packetBuffer[0]=='S' && packetBuffer[1]=='t' && packetBuffer[2]=='o' && packetBuffer[3]=='p'){
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      Udp.write("Stop\n");
      Udp.endPacket();
      Serial.println("STOP");      
    }
    else if (packetBuffer[0]=='G' && packetBuffer[1]=='P' && packetBuffer[2]=='S'){
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      Udp.write("50.1-23.4-");
      Udp.endPacket();
      Serial.println("GPS"); 
    }
    else if (packetBuffer[0]=='R'){
      Serial.println("RIGHT");      
    }
    else if (packetBuffer[0]=='L' && packetBuffer[1]!='V'){
      Serial.println("LEFT");      
    }
    else if (packetBuffer[0]=='B'){
      Serial.println("BACKWARD");      
    }
    else if (packetBuffer[0]=='F'){
      Serial.println("FORWARD");      
    }
    else if (packetBuffer[0]=='U'){
      Serial.println("UP");      
    }
    else if (packetBuffer[0]=='D'){
      Serial.println("DOWN");      
    }
    else if (packetBuffer[0]=='L' && packetBuffer[1] == 'V'){
      Serial.println("Less Velocity");      
    }
    else if (packetBuffer[0]=='M' && packetBuffer[1]=='V'){
      Serial.println("More Velocity");      
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




