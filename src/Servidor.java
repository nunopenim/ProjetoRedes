import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.Arrays;

public class Servidor {

    public static final String ENDCONNECTION = "Servidor.fim";

    public static class TCPServer implements Runnable {
        int serverPort;

        public TCPServer(int port) {
            this.serverPort = port;
        }



        public void run() {
            ServerSocket server = null;
            try {
                server = new ServerSocket(serverPort);
                System.out.println("Servidor TCP instânciado na porta " + serverPort);
                Socket socket = null;
                boolean loop = true;
                while (loop) {
                    socket = server.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                    String linha = br.readLine();
                    System.out.println(linha);
                    String ret = "TCP - " + linha;
                    if(linha.equals("99")) {
                        ret = ENDCONNECTION;
                        loop = false;
                    }
                    ps.println(ret);
                    socket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class UDPServer implements Runnable {
        private DatagramSocket socket;
        private boolean running;
        private byte[] buf = new byte[256];
        private int port;

        public UDPServer(int port) throws SocketException {
            socket = new DatagramSocket(port);
            this.port = port;
        }

        public void run() {
            running = true;
            System.out.println("Servidor UDP instânciado na porta " + port);
            while (running) {
                try {
                    Arrays.fill(buf, (byte) 0);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    String received = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    int counter = 0;
                    while (counter < packet.getData().length && packet.getData()[counter] != 0) {
                        counter++;
                    }
                    received = received.substring(0, counter);
                    String ret = "UDP - " + received;
                    byte[] retBuf = new byte[256];
                    retBuf = ret.getBytes();
                    packet = new DatagramPacket(retBuf, retBuf.length, address, port);
                    socket.send(packet);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                socket.close();
            }
        }
    }

    public static void main(String[] args) throws SocketException {
        Thread servidorTCP = new Thread(new TCPServer(6500));
        //Thread servidorUDP = new Thread(new UDPServer(9031));
        servidorTCP.start();
        //servidorUDP.start();
    }
}
