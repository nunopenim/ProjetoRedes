package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;

public class EchoServerUdp extends Thread {
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
 
    public EchoServerUdp() throws SocketException {
        socket = new DatagramSocket(4445);
    }

    static Random rand = new Random();

    static String[] arrayResp = new String[]{"Bom dia", "bem disposto", "ola", "boas", "asdfghjkhgfhdjcbdnkjjsjks"};

    public static String listar() {
        return arrayResp[0] + "," + arrayResp[1] + "," + arrayResp[2] + "," + arrayResp[3] + "," + arrayResp[4];
    }

    public static String frase() {
        return arrayResp[rand.nextInt(4)];
    }

    public static String horas() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
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
                //System.out.println(received);
                String ret = "Comando inválido";
                if("tchau".equals(received)) {
                    ret = "fim";
                    running = false;
                }
                else if ("horas".equals(received)) {
                    ret = horas();
                }
                else if ("listar".equals(received)) {
                    ret = listar();
                }
                else if ("frase".equals(received)) {
                    ret = frase();
                }
                byte[] retBuf = new byte[256];
                retBuf = ret.getBytes();
                packet = new DatagramPacket(retBuf, retBuf.length, address, port);
                //System.out.println(ret);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket.close();
    }

    public static void main(String[] args) throws SocketException {
        EchoServerUdp serverUdp = new EchoServerUdp();
        serverUdp.run();
    }
}