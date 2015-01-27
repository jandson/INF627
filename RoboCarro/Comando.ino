void comando_bluetooth(){
  int comando = 0;
  // Keep reading from HC-05 and send to Arduino Serial Monitor
  if (BlueTooth.available()){
    comando = BlueTooth.read();
    Serial.print("Comando: ");
    Serial.println(comando);
    if(comando == 119 || comando == 87 ){
      carro.comando = WALK;
    }else{
      if(comando == 83 || comando == 115) carro.comando = STOP;
    }
    if(comando == 76 || comando == 108){
      if(log_ativo == 0){ 
        log_ativo = 1;
        audio();
        return;
      }
      if(log_ativo == 1){
        log_ativo = 0;
        audio();
        return;
      }
    }
  }
  // Keep reading from Arduino Serial Monitor and send to HC-05
  if (Serial.available()){
    BlueTooth.write(Serial.read());
  }
}

void comando_autonomo(){
  if(carro.comando == WALK){
    if(carro.distanciaF < LIMITE_FRENTE)   carro.frente   = BLOQUEADA; else carro.frente = LIVRE;
    if(carro.distanciaE < LIMITE_ESQUERDA) carro.esquerda = BLOQUEADA; else carro.esquerda = LIVRE;
    if(carro.distanciaD < LIMITE_DIREITA)  carro.direita  = BLOQUEADA; else carro.direita = LIVRE;
    if(carro.distanciaT < LIMITE_TRASEIRA) carro.traseira = BLOQUEADA; else carro.traseira = LIVRE;
    
    if(carro.esquerda == LIVRE){
      if(carro.direita == LIVRE){
        if(carro.frente == LIVRE){
          carro.movimento = FRENTE;
        }else{  //Se não estiver com a frente Livre
          if(aleatorio < 300){
            carro.movimento = ESQUERDA;
            aleatorio++;
          }
          if(aleatorio >= 300 && aleatorio < 600){
            carro.movimento = DIREITA;
            aleatorio++;
          }
          if(aleatorio >= 600){
            carro.movimento = TRASEIRA;
            cont = TAXA_LOG + 1;            //Estas duas linhas a seguir forçam o registro no LOG
            cont_logconsole = TAXA_LOG + 1;
            if(aleatorio > 602) aleatorio = 0;
            aleatorio++;
          }
        }
      }else{
        carro.movimento = ESQUERDA;  
      }
    }else{ //Se esquerda Bloquada
      if(carro.direita == LIVRE){
        carro.movimento = DIREITA;
      }else{
        carro.movimento = TRASEIRA;
        cont = TAXA_LOG + 1;             //Estas duas linhas a seguir forçam o registro no LOG
        cont_logconsole = TAXA_LOG + 1;
      }
    }
  }else{
    if(carro.movimento == PARADO) return;
    carro.movimento = PARAR;
  } 
}
