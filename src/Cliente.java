import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;

public class Cliente {

    public static class PrintingThread implements Runnable {
        String text;
        public PrintingThread(String text) {
            this.text = text;
        }
        public void run() {
            System.out.println(text);
        }
    }

    public static class TCPConnection implements Runnable {

        String hostname;
        int portNumber;

        TCPConnection(String host, int port) {
            this.hostname = host;
            this.portNumber = port;
        }

        public void run() {
            boolean openConnection = true;
            String endConnection = "Servidor.fim";
            while(openConnection) {
                try {
                    Socket socket = new Socket(hostname, portNumber);
                    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                    String s = null;
                    s = bufferRead.readLine();
                    BufferedReader br = null;
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream ps = null;
                    ps = new PrintStream(socket.getOutputStream());
                    ps.println(s);
                    String received = null;
                    received = br.readLine();
                    if(endConnection.equals(received)) {
                        openConnection = false;
                    }
                    else {
                        Thread printing = new Thread(new PrintingThread(received));
                        printing.start();
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    public static void main(String[] args) throws SocketException, UnknownHostException {
        Thread teste1 = new Thread(new UDPConnection("localhost", 9031));
        Thread teste2 = new Thread(new TCPConnection("localhost", 6500));
        teste1.start();
        teste2.start();
    }


}



