import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Servidor {

    public static TCPServer[] TCPThreads = new TCPServer[10];
    public static UDPServer[] UDPThreads = new UDPServer[10];
    public static Thread[] threadsTCP = new Thread[TCPThreads.length];
    public static Thread[] threadsUDP = new Thread[UDPThreads.length];

    public static final String ENDCONNECTION = "Servidor.fim";
    public static final String UDPSTART = "Servidor.udp\n";

    public static class TCPServer implements Runnable {
        //int serverPort;
        Socket socket = null;
        int index;

        public TCPServer(int index, Socket sock) {
            //this.serverPort = port;
            this.index = index;
            this.socket = sock;
        }

        public static ArrayList<String> getUsers() {
            ArrayList<String> ret = new ArrayList<>();
            int count = 0;
            for (TCPServer t : TCPThreads) {
                if (t != null && t.socket != null) {
                    String ip = (((InetSocketAddress) t.socket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
                    ret.add(count + " - " + ip);
                    count++;
                }
            }
            return ret;
        }

        public void run() {
            ServerSocket server = null;
            try {
                System.out.println("TCP Server connected");
                //UDPThreads[index] = new UDPServer(9031);
                while (true) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                    ps.flush();
                    String linha = br.readLine();
                    System.out.println("Diagnostics: " + linha + " was recieved");
                    String ret = "Ping!";
                    switch (linha) {
                        case "99":
                            ret = ENDCONNECTION;
                            ps.println(ret);
                            break;
                        case "1":
                            ret = "";
                            ArrayList<String> users = getUsers();
                            for (String s : users) {
                                ret += s + "\n";
                            }
                            ps.println(ret);
                            //ps.flush();
                            break;
                        case "2":
                        case "3":
                            ps.print(UDPSTART);
                            //UDPThreads[index].recieving = true;
                            //UDPThreads[index].exec();
                            //String msgRec = UDPThreads[index].recieved;
                            String msgRec = br.readLine() + "|" + socket.getInetAddress().toString().split("/")[1] + "|" + socket.getPort();
                            System.out.println("Diagnostics: Message '" + msgRec+"' was recieved");
                            String[] args = msgRec.split("\\|");
                            String mensagem = args[0];
                            String origem = args[2];
                            String destino = args[1];
                            UDPThreads[index].recievedStr();
                            //System.out.println(args[1]);
                            //System.out.println(destino);
                            //System.out.println(destino.equals("all "));
                            if (destino.equals("all")) {
                                for (String s : getUsers()) {
                                    UDPThreads[index].toSend = "Mensagem de " + origem + ": " + mensagem;
                                    UDPThreads[index].destiny = s.split(" - ")[1];
                                    UDPThreads[index].sending = true;
                                    UDPThreads[index].exec();
                                }
                            }
                            else {
                                for (String s : getUsers()) {
                                    if (s.startsWith(args[1] + " ")) {
                                        destino = s.split(" - ")[1];
                                    }
                                }
                                if (destino == null) {
                                    System.out.println("Diagnostics: destination is null!!");
                                }
                                else {
                                    //UDPThreads[0].destinyPort = UDPThreads[0].port;
                                    UDPThreads[index].toSend = "Mensagem de " + origem + ": " + mensagem;
                                    UDPThreads[index].destiny = destino;
                                    UDPThreads[index].sending = true;
                                    UDPThreads[index].exec();
                                }
                            }
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
                                ps.println(ret);
                            } catch (FileNotFoundException e) {
                                ret = "The whitelist file doesn't exist on this server!";
                                ps.println(ret);
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
                                ps.println(ret);
                            } catch (FileNotFoundException e) {
                                ret = "The blacklist file doesn't exist on this server!";
                                ps.println(ret);
                            }
                            break;
                        default:
                            ret = "Opção inválida!";
                            ps.println(ret);
                            break;
                    }
                    ps.flush(); //IMPORTANTE
                }
                //socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class UDPServer implements Runnable {
        private DatagramSocket socket;
        public boolean recieving = false;
        public boolean sending = false;
        public String recieved = null;
        public String toSend = null;
        public String destiny = null;
        public int destinyPort = 9031;
        private byte[] buf = new byte[256];
        private int port;

        public UDPServer(int port, DatagramSocket sock) throws SocketException {
            socket = sock;
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
                destinyPort = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                String received = new String(packet.getData(), packet.getOffset(), packet.getLength());
                int counter = 0;
                while (counter < packet.getData().length && packet.getData()[counter] != 0) {
                    counter++;
                }
                received = received.substring(0, counter) + "|" + address.toString().split("/")[1] + "|" + port; //recieved tem de ter mensagem + destino
                return received;
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void send(String data, String address) {
            try{
                byte[] retBuf = new byte[256];
                retBuf = data.getBytes();
                DatagramPacket packet = new DatagramPacket(retBuf, retBuf.length, InetAddress.getByName(address), destinyPort);
                socket.send(packet);
                System.out.println("UDP: SENT!");
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            System.out.println("UDP server started on: " + port);
            exec();
        }

        public void exec() {
            if (recieving) {
                this.recieved = null;
                this.recieved = recievedStr();
                recieving = false;
            }
            if (sending) {
                this.send(toSend, destiny);
                toSend = null;
                destiny = null;
                destinyPort = 9031;
                sending = false;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(6500);
        for (int i = 0; i < threadsTCP.length; i++) {
            Socket socket = server.accept();
            TCPThreads[i] = new TCPServer(i, socket);
            DatagramSocket udpServer = new DatagramSocket(9031 + i);
            UDPThreads[i] = new UDPServer(9031 + i, udpServer);
            threadsTCP[i] = new Thread(TCPThreads[i]);
            threadsTCP[i].start();
            threadsUDP[i] = new Thread(UDPThreads[i]);
            threadsUDP[i].start();
        }
    }
}
