package src;

import java.net.*;
import java.io.*;
// Connectar ao porto 6500 de um servidor especifico,
// envia uma mensagem e imprime resultado,

public class EchoClient {
    // usage: java EchoClient <servidor> <mensagem>
    public static void main(String args[]) throws Exception {
        boolean openConnection = true;
        String endConnection = "Servidor.fim";
        while(openConnection) {
            Socket socket = new Socket("localhost", 6500);
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String s = bufferRead.readLine();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream ps = new PrintStream(socket.getOutputStream());
            ps.println(s); // escreve mensagem na socket
            // imprime resposta do servidor
            String received = br.readLine();
            if(endConnection.equals(received)) {
                openConnection = false;
            }
            else {
                System.out.println(received);
            }
            socket.close();
        }
    }
}
