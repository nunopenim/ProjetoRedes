import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Servidor {

    public static TCPServer[] TCPThreads = new TCPServer[10];
    public static UDPServer[] UDPThreads = new UDPServer[10];
    public static Thread[] threadsTCP = new Thread[TCPThreads.length];
    public static Thread[] threadsUDP = new Thread[UDPThreads.length];

    public static final String ENDCONNECTION = "Servidor.fim";
    public static final String UDPSTART = "Servidor.udp\n";
    public static final String BLOCKED = "Servidor.ilegal";

    public static String readWhiteList() {
        String ret = "";
        try {
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
        return ret;
    }

    public static String readBlackList() {
        String ret = "";
        try {
            File myObj = new File("files/blacklist.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                ret += data + "\n";
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            ret = "The blacklist file doesn't exist on this server!";
        }
        return ret;
    }

    public static class TCPServer implements Runnable {
        //int serverPort;
        Socket socket = null;
        int index;

        boolean legal;

        public TCPServer(int index, Socket sock, boolean legal) {
            //this.serverPort = port;
            this.index = index;
            this.socket = sock;
            this.legal = legal;
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
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream ps = new PrintStream(socket.getOutputStream());
                if (legal) {
                    ps.println(UDPThreads[index].port);
                }
                else {
                    ps.println(BLOCKED);
                    System.out.println("An illegal client was detected!");
                }
                //br.readLine();
                //ps.flush();
                //UDPThreads[index] = new UDPServer(9031);
                while (legal) {
                    //ps.flush();
                    ps = new PrintStream(socket.getOutputStream());
                    String linha = br.readLine();
                    System.out.println("Diagnostics: " + linha + " was recieved");
                    //UDPThreads[index].recievedStr();
                    String ret = "Ping!";
                    if (linha == null) {
                        break;
                    }
                    switch (linha) {
                        case "99":
                            ret = ENDCONNECTION;
                            ps.println(ret);
                            //ps.flush();
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
                            //ps.flush();
                            //UDPThreads[index].recieving = true;
                            //UDPThreads[index].exec();
                            //String msgRec = UDPThreads[index].recieved;
                            String msgRec = br.readLine() + "|" + socket.getInetAddress().toString().split("/")[1] + "|" + socket.getPort();
                            System.out.println("Diagnostics: Message '" + msgRec + "' was recieved");
                            String[] args = msgRec.split("\\|");
                            String mensagem = args[0];
                            String origem = args[2];
                            String destino = args[1];
                            //System.out.println(args[1]);
                            //System.out.println(destino);
                            //System.out.println(destino.equals("all "));
                            if (destino.equals("all")) {
                                for (UDPServer t : UDPThreads) {
                                    if (t == null) {
                                        continue;
                                    }
                                    t.toSend = "Mensagem de " + origem + ": " + mensagem;
                                    //UDPThreads[index].destiny = s.split(" - ")[1];
                                    t.sending = true;
                                    t.run();
                                }
                            } else {
                                int person = 0;
                                boolean invalid = false;
                                boolean changed = false;
                                for (String s : getUsers()) {
                                    if (s.startsWith(args[1] + " ")) {
                                        try {
                                            person = Integer.parseInt(s.split(" - ")[0]);
                                            changed = true;
                                        } catch (Exception e) {
                                            invalid = true;
                                        }
                                    }
                                }
                                if (invalid || !changed) {
                                    System.out.println("Diagnostics: destination is not valid!!");
                                    UDPThreads[index].toSend = "O utilizador de destino é inválido!";
                                    UDPThreads[index].sending = true;
                                    UDPThreads[index].run();
                                } else {
                                    //UDPThreads[0].destinyPort = UDPThreads[0].port;
                                    UDPThreads[person].toSend = "Mensagem de " + origem + ": " + mensagem;
                                    UDPThreads[person].sending = true;
                                    UDPThreads[person].run();
                                }
                            }
                            break;
                        case "4":
                            ret = readWhiteList();
                            ps.println(ret);
                            break;
                        case "5":
                            ret = readBlackList();
                            ps.println(ret);
                            break;
                        default:
                            ret = "Opção inválida!";
                            ps.println(ret);
                            break;
                    }
                    //ps.flush(); //IMPORTANTE
                }
                socket.close();
                System.out.println("TCP Server Disconnected!");
                threadsTCP[index] = null;
                threadsUDP[index] = null;
                UDPThreads[index] = null;
                TCPThreads[index] = null;
            } catch (SocketException e) {
                System.out.println("The client has disconnected unexpectedly!");
                threadsTCP[index] = null;
                threadsUDP[index] = null;
                UDPThreads[index] = null;
                TCPThreads[index] = null;
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
        public int destinyPort;
        private byte[] buf = new byte[256];
        private int port;

        public UDPServer(int port) throws SocketException {
            socket = new DatagramSocket();
            this.port = port;
            this.destinyPort = port;
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
            //System.out.println("UDP server started on: " + port);
            exec();
        }

        public void exec() {
            if (false && recieving) { //temporarily disabled
                System.out.println("Entered");
                this.recieved = null;
                this.recieved = recievedStr();
                recieving = false;
            }
            if (sending) {
                this.send(toSend, destiny);
                toSend = null;
                //destiny = null;
                //destinyPort = port;
                sending = false;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(6500);
        while (true) {
            for (int i = 0; i < threadsTCP.length; i++) {
                if (TCPThreads[i] == null) {
                    boolean legal = true;
                    boolean listaBrancaValida = true;
                    boolean listaNegraValida = true;
                    Socket socket = server.accept();
                    String listaBrancaCorrida = readWhiteList();
                    String listaNegraCorrida = readBlackList();
                    String[] listaBranca = null;
                    String[] listaNegra = null;
                    if (listaBrancaCorrida.equals("The whitelist file doesn't exist on this server!")) {
                        listaBrancaValida = false;
                    }
                    else{
                        legal = false;
                        listaBranca = listaBrancaCorrida.split("\n");
                    }
                    if (listaNegraCorrida.equals("The blacklist file doesn't exist on this server!")) {
                        listaNegraValida = false;
                    }
                    else {
                         listaNegra = listaNegraCorrida.split("\n");
                    }
                    String ip = (((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
                    if (listaBrancaValida) {
                        for (String s : listaBranca) {
                            if (s != null && s.equals(ip)) {
                                legal = true;
                                break;
                            }
                        }
                    }
                    if (listaNegraValida) {
                        for (String s : listaNegra) {
                            if (s != null && s.equals(ip)) {
                                legal = false;
                                break;
                            }
                        }
                    }
                    TCPThreads[i] = new TCPServer(i, socket, legal);
                    UDPThreads[i] = new UDPServer(9031+i);
                    UDPThreads[i].destiny = (((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
                    //UDPThreads[i].recieving = true;
                    //UDPThreads[i].exec();
                    threadsTCP[i] = new Thread(TCPThreads[i]);
                    threadsUDP[i] = new Thread(UDPThreads[i]);
                    threadsTCP[i].start();
                    threadsUDP[i].start();
                }
            }
        }
    }
}
