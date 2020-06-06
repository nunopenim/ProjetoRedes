import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;

public class Cliente {

    public static boolean isNumber(String s) {
        try{
            int number = Integer.parseInt(s);
        }
        catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static class PrintingThread implements Runnable {
        String text;
        public PrintingThread(String text) {
            this.text = text;
        }
        public void run() {
            System.out.println(text);
        }
    }

    public static class TCPConnection implements Runnable{

        String hostname;
        int portNumber;
        Socket socket;
        String textToSend = "Connected!";
        String recieved = null;

        TCPConnection(String host, int port) {
            this.hostname = host;
            this.portNumber = port;
        }

        public void open() throws IOException {
            socket = new Socket(hostname, portNumber);
        }

        public void send(String s) throws IOException {
            PrintStream ps = new PrintStream(socket.getOutputStream());
            ps.println(s);
        }

        public String recieve() throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = null;
            String text = "";
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }
                text += line + "\n";
            }
            return text;
        }

        public void close() throws IOException {
            socket.close();
        }

        public void run() {
            try {
                this.open();
                this.send(textToSend);
                recieved = this.recieve();
                if (recieved.equals("Servidor.fim")) { //terminar ligação
                    this.close();
                }
                this.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static class UDPConnection implements Runnable {
        private String hostname;
        private DatagramSocket socket;
        private InetAddress address;
        private int port;

        private byte[] buf;

        public UDPConnection(String address, int port) throws SocketException, UnknownHostException {
            socket = new DatagramSocket();
            this.hostname = address;
            this.address = InetAddress.getByName(address);
            this.port = port;
        }

        public String sendEcho(String msg) throws IOException {
            buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, this.port);
            socket.send(packet);
            byte[] recBuf = new byte[256];
            packet = new DatagramPacket(recBuf, recBuf.length);
            socket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            return received;
        }

        public void close() {
            socket.close();
        }

        public void run() {
            boolean exit = false;
            while (!exit) {
                try {
                    UDPConnection client = new UDPConnection(hostname, port);
                    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                    String s = bufferRead.readLine();
                    Thread printing = new Thread(new PrintingThread(client.sendEcho(s)));
                    printing.start();
                    if ("tchau".equals(s)) {
                        exit = true;
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            close();
        }

    }

    public static void menu() throws IOException {
        System.out.println("MENU CLIENTE\n");
        System.out.println("0 - Menu Inicial");
        System.out.println("1 - Listar utilizadores online");
        System.out.println("2 - Enviar mensagem a um utilizador");
        System.out.println("3 - Enviar mensagem a todos os utilizadores");
        System.out.println("4 - Lista branca de utilizadores");
        System.out.println("5 - Lista negra de utilizadores");
        System.out.println("99 - Sair");
    }

    public static void main(String[] args) throws IOException {
        //Thread teste1 = new Thread(new UDPConnection("localhost", 9031));
        TCPConnection ligTCP = new TCPConnection("localhost", 6500);
        //Thread TCPThread = new Thread(ligTCP);
        boolean exit = false;
        menu();
        while(!exit){
            System.out.println();
            System.out.print("Opção? ");
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String s = bufferRead.readLine();
            if ("0".equals(s)) { //mostrar menu, continuar
                menu();
                continue;
            }
            else {
                ligTCP.textToSend = s;
                ligTCP.run();
            }
            if (ligTCP.recieved.equals("Servidor.fim")) { //server-side end connection
                System.out.println("A sair");
                System.out.println("Cliente desconectado...");
                exit = true;
            }
            else {
                System.out.print(ligTCP.recieved);
            }
        }
    }
}



