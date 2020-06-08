import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class Cliente {

    public static final String ENDCONNECTION = "Servidor.fim\n";

    public static final String UDPSTART = "Servidor.udp\n";

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

        public void runRec() throws IOException {
            this.open();
            recieved = this.recieve();
            this.close();
        }

        public void run() {
            try {
                this.open();
                this.send(textToSend);

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

        public void sendEcho(String msg) throws IOException {
            buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, this.port);
            socket.send(packet);
        }

        public String recieveEcho() throws IOException {
            byte[] recBuf = new byte[256];
            DatagramPacket packet = new DatagramPacket(recBuf, recBuf.length);
            socket.setSoTimeout(250);
            try{
                socket.receive(packet);
                return new String(packet.getData(), 0, packet.getLength());
            }
            catch (SocketTimeoutException e) {
                return null;
            }
        }

        public void close() {
            socket.close();
        }

        public void run() {
            try {
                while(true) {
                    String messageRec = this.recieveEcho();
                    if (messageRec != null) {
                        System.out.println("Recieved message: " + messageRec);
                    }
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
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
        UDPConnection ligUDP = new UDPConnection("localhost", 9031);
        TCPConnection ligTCP = new TCPConnection("localhost", 6500);
        Thread udpThread = new Thread(ligUDP);
        udpThread.start();
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
                if ("2".equals(s)) {
                    System.out.println();
                    System.out.print("Utilizador? ");
                    String destinatario = bufferRead.readLine();
                    System.out.println();
                    System.out.print("Mensagem? ");
                    String mensagem = bufferRead.readLine();
                    String toSend = mensagem + "|" + destinatario;
                    ligUDP.sendEcho(toSend);
                }
                else if("3".equals(s)){
                    System.out.println();
                    System.out.print("Mensagem? ");
                    String mensagem = bufferRead.readLine();
                    String toSend = mensagem + "|" + "all";
                    ligUDP.sendEcho(toSend);
                }
                else {
                    ligTCP.runRec();
                }
            }
            if (ENDCONNECTION.equals(ligTCP.recieved)) { //server-side end connection
                ligTCP.close();
                ligUDP.close();
                System.out.println("A sair");
                System.out.println("Cliente desconectado...");
                exit = true;
            }
            else if (UDPSTART.equals(ligTCP.recieved)) {
                continue;
            }
            else {
                if (ligTCP.recieved != null) {
                    System.out.print(ligTCP.recieved);
                }

            }
        }
    }
}



