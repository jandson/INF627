#include <SoftwareSerial.h>   
   
SoftwareSerial bluetooth(10, 11); // RX, TX  
String command = ""; // Stores response of bluetooth device  
            // which simply allows \n between each  
            // response.  
            
int incomingByte = 0;
char buf[100];
int num = 0;
String dados;
   
void setup()   
{   
  // SoftwareSerial "com port" data rate. JY-MCU v1.03 defaults to 9600.  
  bluetooth.begin(9600);  
  pinMode(9, OUTPUT);     // Pino para acionar o modo de configuracao (pino KEY do módulo)
  Serial.begin(9600);
  digitalWrite(9, HIGH);
  limpaBufferEntrada();
}  

void executeAction(String command){  
  if(command.equalsIgnoreCase("W")){    
    walk();
  }else if(command.equalsIgnoreCase("S")){
    stop();
  }    
}

String confirmAction(String command){     
 
  Serial.println("Recebido:"+command); 
  String confirm = "0";  
   
  if(command.equalsIgnoreCase("W") || command.equalsIgnoreCase("S")){        
    confirm = "1";
  }   
  
  bluetooth.print(confirm);
  
  Serial.println("Retornado:"+confirm);
  
  return confirm;
}


void stop(){
  Serial.print("STOPPING");
}

void walk(){
  Serial.print("WALKING");
}  
   
void loop()  
{   
  char teste[100] = "";
 
  if (bluetooth.available()) 
  { 
    
    while(bluetooth.available() > 0)
    { 
      buf[num] = bluetooth.read();
      
      if (buf[num] == '\n')
      {
        
        String dados = buf;
        dados.trim();        
        if(confirmAction(dados) == "1"){
           executeAction(dados);
        }  
        
        
        // apaga o buffer
        for (int ca =0; ca<20; ca++)
        {
          buf[ca]=0;
        }
        num=0;
        break;
       }
      num++;
    }
  }       
   
}  

void limpaBufferEntrada() {

  while (bluetooth.available()) {

    bluetooth.read();

  }

}



