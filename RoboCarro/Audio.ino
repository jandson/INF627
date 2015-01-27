/*
Projeto Arduino beep com buzzer.
Por Jota
----------------------------------------
--=<| www.ComoFazerAsCoisas.com.br |>=--
----------------------------------------
*/

//Método loop, executado enquanto o Arduino estiver ligado.
void audio() {  
  //Ligando o buzzer com uma frequencia de 1500 hz.
  tone(Audio_buzzer,2500);   
  delay(300);
  //Desligando o buzzer.
  noTone(Audio_buzzer); 
}

void audio_on(){
  tone(Audio_buzzer,2000);
}

void audio_off(){
  noTone(Audio_buzzer);
}

void audio2() {  
  //Ligando o buzzer com uma frequencia de 1500 hz.
  tone(Audio_buzzer,1500);   
  delay(30);
  //Desligando o buzzer.
  noTone(Audio_buzzer); 
}

void audio3() {  
  //Ligando o buzzer com uma frequencia de 1500 hz.
  tone(Audio_buzzer,1000);   
  delay(500);
  //Desligando o buzzer.
  noTone(Audio_buzzer); 
}

void audio5() {  
  
  for (int nota = 0; nota < 25; nota++) {    // Até 156, pois são 156 notas
    tone(Audio_buzzer, melodia[nota]+30,duracaodasnotas[nota]);
    delay(pausadepoisdasnotas[nota]);
    noTone(Audio_buzzer);
  }
}
