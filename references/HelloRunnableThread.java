package org.ulht.aulas;

public class HelloRunnableThread {//inicio classe HelloRunnableThread

	public static void main(String args[]) {//inicio metodo main
		String threadName = Thread.currentThread().getName();//nome da thread
		System.out.format("Eu sou %s%n", threadName);
		Thread t = new Thread(new MsgThread("hello world"));//criar thread		
		t.start();//executar thread				
		System.out.println("Terminou "+threadName);
	}//fim metodo main
	
	public static class MsgThread implements Runnable{//inicio classe MsgThread
		private String msg1; 
		public MsgThread(String msg){ 
			this.msg1=msg;
		}
		/* codigo a executar numa thread separada */
		public void run(){//inicio metodo run 
			String threadName = Thread.currentThread().getName();//nome da thread
			System.out.format("Eu sou %s: %s%n",threadName,msg1);
			System.out.println("Terminou "+threadName);
		}//fim run		
	}//fim MsgThread
}//fim HelloRunnableThread
