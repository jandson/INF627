// Programa : Scrool horizontal com matriz de leds 8x8  
// Baseado no livro Arduino Basico, de Michael McRoberts  
// Alterações e comentários : Arduino e Cia  
   

int atualiza_distancia(){      //Função que atualiza variáveis de distâncias 
  long duracao = 0;
  long distancia = 0;

  // Atualiza distancia da Frente
  digitalWrite(UltraF_trigPin,LOW);
  delayMicroseconds(2); 
  digitalWrite(UltraF_trigPin,HIGH);
  delayMicroseconds(10); 
  digitalWrite(UltraF_trigPin,LOW);
  duracao = pulseIn(UltraF_echoPin,HIGH);
  distancia = (duracao/2)/29.1;
  carro.distanciaF = distancia;

  // Atualiza distancia da Esquerda
  digitalWrite(UltraE_trigPin,LOW);
  delayMicroseconds(2); 
  digitalWrite(UltraE_trigPin,HIGH);
  delayMicroseconds(10); 
  digitalWrite(UltraE_trigPin,LOW);
  duracao = pulseIn(UltraE_echoPin,HIGH);
  distancia = (duracao/2)/29.1;
  carro.distanciaE = distancia;

  // Atualiza distancia da Direita  
  digitalWrite(UltraD_trigPin,LOW);
  delayMicroseconds(2); 
  digitalWrite(UltraD_trigPin,HIGH);
  delayMicroseconds(10); 
  digitalWrite(UltraD_trigPin,LOW);
  duracao = pulseIn(UltraD_echoPin,HIGH);
  distancia = (duracao/2)/29.1;
  carro.distanciaD = distancia;
  
  // Atualiza distancia da Traseira
  /*
  digitalWrite(UltraT_trigPin,LOW);
  delayMicroseconds(2); 
  digitalWrite(UltraT_trigPin,HIGH);
  delayMicroseconds(10); 
  digitalWrite(UltraT_trigPin,LOW);
  duracao = pulseIn(UltraT_echoPin,HIGH);
  distancia = (duracao/2)/29.1;
  carro.distanciaT = distancia;
  */
  carro.distanciaT = 100;
  return(1);
}  

int log_console(){
cont_logconsole++;
  if(cont_logconsole > TAXA_LOG){
    Serial.println("Parametros do Carro");
    Serial.print("Comando: ");
    if(carro.comando == WALK) Serial.println("WALK");
    if(carro.comando == STOP) Serial.println("STOP");
    Serial.print("Direcao: ");
    if(carro.movimento == PARADO) Serial.println("Parado");
    if(carro.movimento == FRENTE) Serial.println("Frente");
    if(carro.movimento == ESQUERDA) Serial.println("Esquerda");
    if(carro.movimento == DIREITA) Serial.println("Direita");
    if(carro.movimento == TRASEIRA) Serial.println("Traseira");
    Serial.print("Aleatorio: ");
    Serial.println(aleatorio);
    Serial.print("Frente: ");
    Serial.print(carro.distanciaF);
    Serial.print(" Esquerda: ");
    Serial.print(carro.distanciaE);
    Serial.print(" Direita: ");
    Serial.print(carro.distanciaD);
    Serial.print(" Traseira: ");
    Serial.println(carro.distanciaT);
    Serial.println();
    cont_logconsole = 0;
    return(1);
  }
}

int log_BlueTooth(){
cont++;
  if(cont > TAXA_LOG && log_ativo == 1){
    BlueTooth.println("Parametros do Carro");
    BlueTooth.print("Comando: ");
    if(carro.comando == WALK) BlueTooth.println("WALK");
    if(carro.comando == STOP) BlueTooth.println("STOP");
    BlueTooth.print("Direcao: ");
    if(carro.movimento == PARADO) BlueTooth.println("Parado");
    if(carro.movimento == FRENTE) BlueTooth.println("Frente");
    if(carro.movimento == ESQUERDA) BlueTooth.println("Esquerda");
    if(carro.movimento == DIREITA) BlueTooth.println("Direita");
    if(carro.movimento == TRASEIRA) BlueTooth.println("Traseira");
    BlueTooth.print("Aleatorio: ");
    BlueTooth.println(aleatorio);
    BlueTooth.print("Frente: ");
    BlueTooth.print(carro.distanciaF);
    BlueTooth.print(" Esquerda: ");
    BlueTooth.print(carro.distanciaE);
    BlueTooth.print(" Direita: ");
    BlueTooth.print(carro.distanciaD);
    BlueTooth.print(" Traseira: ");
    BlueTooth.println(carro.distanciaT);
    BlueTooth.println();
    cont = 0;
    return(1);
  }
}
