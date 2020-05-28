import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.Arrays;

public class Servidor {

    public static class TCPServer implements Runnable {
        int serverPort;

        public TCPServer(int port) {
            this.serverPort = port;
        }

        public void run() {
            ServerSocket server = null;
            try {
                server = new ServerSocket(serverPort);
                System.out.println("servidor iniciado no porto 6500");
                Socket socket = null;
                //aguarda mensagens
                while (true) {
                    socket = server.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                    String linha = br.readLine();
                    String ret = "TCP";
                    ps.println(ret + " - " + linha);
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

        public UDPServer(int port) throws SocketException {
            socket = new DatagramSocket(port);
        }

        public void run() {
            running = true;

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
        Thread teste1 = new Thread(new TCPServer(6500));
        Thread teste2 = new Thread(new UDPServer(9031));
        teste1.start();
        teste2.start();
    }
}
