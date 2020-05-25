package org.ulht.aulas;

public class RandomThread {

	public static void main(String args[]) {
		String threadName = Thread.currentThread().getName();//nome da thread
		System.out.format("Eu sou %s%n", threadName);
		Thread t0 = new Thread(new RandomNumbers(1,10));//criar thread 0	
		Thread t1 = new Thread(new RandomNumbers(1,20));//criar thread 1
		System.out.println("A executar threads...");
		t0.start();//inicia thread 0
		t1.start();//inicia thread 1		
	}	

	public static class RandomNumbers implements Runnable{
		private int x1,y1;		
		public RandomNumbers( int x,int y){
			this.x1=x;
			this.y1=y;
		}
		/* codigo a executar numa thread separada */
		public void run(){
			int number=0;
			String threadName = Thread.currentThread().getName();//nome da thread
			while (number!=10){								
				number = (int) Math.floor(Math.random()*y1) + x1;//gera numero aleatorio				
				System.out.format("Eu sou %s: %s%n", threadName, number);
				try {
					Thread.sleep(1000);//Pausa de 1 segundo
				} catch (InterruptedException e) {				
					e.printStackTrace();
				}
			}
			System.out.println("Terminou "+threadName);
		}		
	}	
}
