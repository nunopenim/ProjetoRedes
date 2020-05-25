package org.ulht.aulas;

public class HelloThread extends Thread{
	
	public static void main(String args[]) {
		String threadName = Thread.currentThread().getName();//nome da thread
		System.out.format("Eu sou %s%n", threadName);
		Thread t = new Thread(new HelloThread());//criar thread		
		t.start();//iniciar thread		
		Thread t2 = new Thread(new HelloThread());//criar thread		
		t2.start();//iniciar thread	
		System.out.println("Terminou "+threadName);
	}

	/* codigo a executar numa thread separada */
	public void run(){		
		String threadName = Thread.currentThread().getName();//nome da thread
		System.out.format("Eu sou %s: %s%n",threadName,"Hello World");
		System.out.println("Terminou "+threadName);
	}
}




