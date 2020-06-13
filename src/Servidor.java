import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Servidor {

    public static TCPServer[] TCPThreads = new TCPServer[100];
    public static UDPServer[] UDPThreads = new UDPServer[100];
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
        Socket socket;
        int index;

        boolean legal;

        public TCPServer(int index, Socket sock, boolean legal) {
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
                }
                count++;
            }
            return ret;
        }

        public void run() {
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
                while (legal) {
                    ps = new PrintStream(socket.getOutputStream());
                    String linha = br.readLine();
                    System.out.println("Diagnostics: " + linha + " was recieved");
                    String ret;
                    if (linha == null) {
                        break;
                    }
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
                            break;
                        case "2":
                        case "3":
                            ps.print(UDPSTART);
                            String msgRec = br.readLine() + "|" + socket.getInetAddress().toString().split("/")[1] + "|" + socket.getPort();
                            System.out.println("Diagnostics: Message '" + msgRec + "' was recieved");
                            String[] args = msgRec.split("\\|");
                            String mensagem = args[0];
                            String origem = args[2];
                            String destino = args[1];
                            if (destino.equals("all")) {
                                for (UDPServer t : UDPThreads) {
                                    if (t == null) {
                                        continue;
                                    }
                                    t.toSend = "Mensagem de " + origem + ": " + mensagem;
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
        public boolean sending = false;
        public String toSend = null;
        public String destiny = null;
        public int destinyPort;
        private int port;

        public UDPServer(int port) throws SocketException {
            socket = new DatagramSocket();
            this.port = port;
            this.destinyPort = port;
        }


        public void send(String data, String address) {
            try{
                byte[] retBuf;
                retBuf = data.getBytes();
                DatagramPacket packet = new DatagramPacket(retBuf, retBuf.length, InetAddress.getByName(address), destinyPort);
                socket.send(packet);
                System.out.println("UDP: SENT!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            exec();
        }

        public void exec() {
            if (sending) {
                this.send(toSend, destiny);
                toSend = null;
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
                    threadsTCP[i] = new Thread(TCPThreads[i]);
                    threadsUDP[i] = new Thread(UDPThreads[i]);
                    threadsTCP[i].start();
                    threadsUDP[i].start();
                }
            }
        }
    }
}
