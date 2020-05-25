package org.ulht.aulas;

public class HelloNoThread {
	
	public static void main(String args[]) {
		String threadName = Thread.currentThread().getName();//nome da thread
		System.out.format("Eu sou %s: %s%n",threadName,"Hello World");	
		System.out.println("Terminou "+threadName);
	}	
}




