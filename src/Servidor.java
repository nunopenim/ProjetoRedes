import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class Servidor {

    public static TCPServer[] TCPThreads = new TCPServer[10];

    public static final String ENDCONNECTION = "Servidor.fim";

    public static class TCPServer implements Runnable {
        int serverPort;
        Socket socket = null;

        public TCPServer(int port) {
            this.serverPort = port;
        }

        public void run() {
            ServerSocket server = null;
            try {
                server = new ServerSocket(serverPort);
                System.out.println("Servidor TCP instânciado na porta " + serverPort);
                while (true) {
                    socket = server.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                    String linha = br.readLine();
                    System.out.println("Diagnostics: " + linha + " was recieved");
                    String ret = "Ping!";
                    switch (linha) {
                        case "99":
                            ret = ENDCONNECTION;
                            break;
                        case "1":
                            int count = 0;
                            ret = "";
                            for (TCPServer t : TCPThreads) {
                                if (t != null && t.socket != null) {
                                    String ip = (((InetSocketAddress) t.socket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
                                    ret += count + " - " + ip;
                                    count++;
                                }
                            }
                            break;
                        case "2":
                            //UDPThread aqui
                            break;
                        case "3":
                            //UDPThread aqui
                            break;
                        case "4":
                            try {
                                ret = "";
                                File myObj = new File("files/whitelist.txt");
                                Scanner myReader = new Scanner(myObj);
                                while (myReader.hasNextLine()) {
                                    String data = myReader.nextLine();
                                    ret += data + "\n";
                                }
                                myReader.close();
                            } catch (FileNotFoundException e) {
                                ret = "The whitelist file doesn't exist on this server!";
                            }
                            break;
                        case "5":
                            try {
                                ret = "";
                                File myObj = new File("files/blacklist.txt");
                                Scanner myReader = new Scanner(myObj);
                                while (myReader.hasNextLine()) {
                                    String data = myReader.nextLine();
                                    ret += data + System.getProperty("line.separator");
                                }
                                myReader.close();
                            } catch (FileNotFoundException e) {
                                ret = "The blacklist file doesn't exist on this server!";
                            }
                            break;
                        default:
                            ret = "Opção inválida!";
                            break;
                    }
                    ps.print(ret);
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

        public void close() {
            socket.close();
        }

        public String recievedStr() {
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
                received = received.substring(0, counter) + "|" + address.toString() + "|" + port; //recieved tem de ter mensagem + destino
                return received;
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void send(String data, String address, int porta) {
            try{
                byte[] retBuf = new byte[256];
                retBuf = data.getBytes();
                DatagramPacket packet = new DatagramPacket(retBuf, retBuf.length, InetAddress.getByName(address), porta);
                socket.send(packet);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            System.out.println("Servidor UDP instânciado na porta " + port);
            String recieved = recievedStr();
            if (recieved == null) {
                return;
            }
            String[] split = recieved.split("\\|");
            String message = split[0];
            String destiny = split[1];
            String address = split[2];
            String port = split[3];
            int porta = Integer.parseInt(port);
            String toSend = "Mensagem de " + address + ":" + message;
            send(toSend, destiny, porta);
        }
    }

    public static void main(String[] args) throws SocketException {
        TCPThreads[0] = new TCPServer(6500);
        Thread[] threadsTCP = new Thread[TCPThreads.length];
        for (int i = 0; i < TCPThreads.length; i++) {
            if (TCPThreads[i] != null) {
                threadsTCP[i] = new Thread(TCPThreads[i]);
            }
            else {
                threadsTCP[i] = null;
            }
        }
        for(Thread t : threadsTCP) {
            if (t != null) {
                t.start();
            }
        }
    }
}
