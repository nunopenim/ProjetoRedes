package org.ulht.aulas;

public class RandomNoThread {

	public static void main(String args[]) {
		/* codigo a executar na thread main */
		String threadName = Thread.currentThread().getName();//nome da thread
		System.out.format("Eu sou %s%n", threadName);
		RandomNumbers(1,10);		
		RandomNumbers(1,20);
		System.out.println("Terminou "+threadName);
	}

	public static void RandomNumbers(int x, int y){
		int x1=x;
		int y1=y;
		int number=0;
		while (number!=10){								
			number = (int) Math.floor(Math.random()*y1) + x1;//gera numero aleatorio
			String threadName = Thread.currentThread().getName();//nome da thread
			System.out.format("Eu sou %s: %s%n", threadName, number);
			try {
				Thread.sleep(1000);//Pausa de 1 segundo
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
		}		
	}		
}




