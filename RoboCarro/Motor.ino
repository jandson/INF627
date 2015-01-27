void motor(){
  if(carro.movimento == PARAR){              //Esta condição ocorre antes do estado PARADO. Serve para freiar o movimento do carro.
    //Para o motor A
    digitalWrite(Motor_IN1, LOW);
    digitalWrite(Motor_IN2, LOW);
    //Para o motor B
    digitalWrite(Motor_IN3, LOW);
    digitalWrite(Motor_IN4, LOW);    
    digitalWrite(LED, LOW);
    audio5();
    carro.movimento = PARADO;    
  }
  if(carro.movimento == PARADO){
    return;
  }
  if(carro.movimento == FRENTE){
    //Gira o Motor A para frente
    digitalWrite(Motor_IN1, HIGH);
    digitalWrite(Motor_IN2, LOW);
    //Gira o Motor B para frente
    digitalWrite(Motor_IN3, HIGH);
    digitalWrite(Motor_IN4, LOW);
    digitalWrite(LED, HIGH);
    return;
  }
  if(carro.movimento == ESQUERDA){
    //Gira para Esquerda
    //Gira o Motor A para trás
    digitalWrite(Motor_IN1, HIGH);
    digitalWrite(Motor_IN2, LOW);
    //Gira o Motor B para frente
    digitalWrite(Motor_IN3, LOW);
    digitalWrite(Motor_IN4, HIGH);
    digitalWrite(LED, LOW);
    return;
  }
  if(carro.movimento == DIREITA){
    //Gira para Direita
    //Gira o Motor A para frente
    digitalWrite(Motor_IN1, LOW);
    digitalWrite(Motor_IN2, HIGH);
    //Gira o Motor B para trás
    digitalWrite(Motor_IN3, HIGH);
    digitalWrite(Motor_IN4, LOW);
    digitalWrite(LED, LOW);
    return;
  }
  if(carro.movimento == TRASEIRA){
  //Gira para Trás
    //Gira o Motor A para trás
    digitalWrite(Motor_IN1, LOW);
    digitalWrite(Motor_IN2, HIGH);
    //Gira o Motor B para trás
    digitalWrite(Motor_IN3, LOW);
    digitalWrite(Motor_IN4, HIGH);
    digitalWrite(LED, LOW);
    audio_on();
    delay(1000);
    audio_off();
    return;
  }
}


 
