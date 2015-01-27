#include <SoftwareSerial.h>

#define LIMITE_FRENTE   10
#define LIMITE_ESQUERDA 5
#define LIMITE_DIREITA  5
#define LIMITE_TRASEIRA 10

//Pinos do Sensor Ultrasom Frontal
#define UltraF_echoPin  2
#define UltraF_trigPin  3

//Pinos do Sensor Ultrasom Esquerdo
#define UltraE_echoPin  4
#define UltraE_trigPin  5

//Pinos do Sensor Ultrasom Direito
#define UltraD_echoPin  6
#define UltraD_trigPin  7

//Pinos do Sensor Ultrasom Traseiro
//#define UltraT_echoPin  8
//#define UltraT_trigPin  9

#define BlueTooth_ATPin   9    // Pino para habilitar o modo AT do módulo BlueTooth - cabo laranja

//Pinos da Ponte H com Chip L298N
#define Motor_IN1 14  //Pino A0
#define Motor_IN2 15  //Pino A1
#define Motor_IN3 16  //Pino A2
#define Motor_IN4 17  //Pino A3

//Pino do Buzzer
#define Audio_buzzer 8

//Pino do LED
#define LED      13

//Constantes do Comando Bluetooth
#define WALK    1
#define STOP    0

//Constantes do Movimento
#define PARAR    -1
#define PARADO   0
#define FRENTE   1
#define ESQUERDA 3
#define DIREITA  4
#define TRASEIRA 5

//Constantes da Direção
#define BLOQUEADA 0
#define LIVRE     1

//Constante da Atualização do LOG
#define TAXA_LOG 100

//Estrutura para armazenar todo o estado do Carro
struct estado_carro{
  int comando;
  int movimento;
  long distanciaF;
  long distanciaE;
  long distanciaD;
  long distanciaT;
  int frente;
  int esquerda;
  int direita;
  int traseira;
}
carro;

// Declaracao das estrutura de dados contendo a música do carro
int melodia[] = {660,660,660,510,660,770,380,510,380,320,440,480,450,430,380,660,760,860,700,760,660,520,580,480,510,380,320,440,480,450,430,380,660,760,860,700,760,660,520,580,480,500,760,720,680,620,650,380,430,500,430,500,570,500,760,720,680,620,650,1020,1020,1020,380,500,760,720,680,620,650,380,430,500,430,500,570,585,550,500,380,500,500,500,500,760,720,680,620,650,380,430,500,430,500,570,500,760,720,680,620,650,1020,1020,1020,380,500,760,720,680,620,650,380,430,500,430,500,570,585,550,500,380,500,500,500,500,500,500,500,580,660,500,430,380,500,500,500,500,580,660,870,760,500,500,500,500,580,660,500,430,380,660,660,660,510,660,770,380};
int duracaodasnotas[] = {100,100,100,100,100,100,100,100,100,100,100,80,100,100,100,80,50,100,80,50,80,80,80,80,100,100,100,100,80,100,100,100,80,50,100,80,50,80,80,80,80,100,100,100,100,150,150,100,100,100,100,100,100,100,100,100,100,150,200,80,80,80,100,100,100,100,100,150,150,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,150,150,100,100,100,100,100,100,100,100,100,100,150,200,80,80,80,100,100,100,100,100,150,150,100,100,100,100,100,100,100,100,100,100,100,100,100,60,80,60,80,80,80,80,80,80,60,80,60,80,80,80,80,80,60,80,60,80,80,80,80,80,80,100,100,100,100,100,100,100};
int pausadepoisdasnotas[] ={150,300,300,100,300,550,575,450,400,500,300,330,150,300,200,200,150,300,150,350,300,150,150,500,450,400,500,300,330,150,300,200,200,150,300,150,350,300,150,150,500,300,100,150,150,300,300,150,150,300,150,100,220,300,100,150,150,300,300,300,150,300,300,300,100,150,150,300,300,150,150,300,150,100,420,450,420,360,300,300,150,300,300,100,150,150,300,300,150,150,300,150,100,220,300,100,150,150,300,300,300,150,300,300,300,100,150,150,300,300,150,150,300,150,100,420,450,420,360,300,300,150,300,150,300,350,150,350,150,300,150,600,150,300,350,150,150,550,325,600,150,300,350,150,350,150,300,150,600,150,300,300,100,300,550,575};

long cont = 0;
long cont_logconsole = 0;
int aleatorio = 0;
int log_ativo = 0;

SoftwareSerial BlueTooth(10, 11); // TX | RX  - Cabo TX (Verde) - Cabo RX (Amarelo)

void setup() {
  Serial.begin(38400);                              // Inicializa a comunicação Serial
  
  pinMode(BlueTooth_ATPin, OUTPUT);                // this pin will pull the HC-05 pin 34 (key pin) HIGH to switch module to AT mode
  digitalWrite(BlueTooth_ATPin, HIGH);
  BlueTooth.begin(38400);                          // HC-05 default speed in AT command more
  
  pinMode(UltraF_echoPin,INPUT);                    // Inicializa Pinos do Ultrassom
  pinMode(UltraF_trigPin,OUTPUT);
  pinMode(UltraE_echoPin,INPUT);
  pinMode(UltraE_trigPin,OUTPUT);
  pinMode(UltraD_echoPin,INPUT);
  pinMode(UltraD_trigPin,OUTPUT);

  pinMode(Audio_buzzer,OUTPUT);                    //Definindo o pino buzzer como de saída.
  pinMode(LED,OUTPUT);                             //Definindo o pino LED como de saída.

  audio();
  
  carro.comando    = WALK;
  carro.movimento  = FRENTE;
  carro.frente     = LIVRE;
  carro.esquerda   = LIVRE;
  carro.direita    = LIVRE;
  carro.traseira   = LIVRE;
  carro.distanciaF = 100;
  carro.distanciaE = 100;
  carro.distanciaD = 100;
  carro.distanciaT = 100;
}

void loop(){
  comando_bluetooth();
  atualiza_distancia();
  comando_autonomo();
  log_console();
  log_BlueTooth();
  motor();
}

