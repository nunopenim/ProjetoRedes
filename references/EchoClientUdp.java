package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class EchoClientUdp {
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf;

    public EchoClientUdp(String address) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        this.address = InetAddress.getByName(address);
    }

    public String sendEcho(String msg) throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
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


    public static void main(String[] args) {
        boolean exit = false;
        while (!exit) {
            try {
                EchoClientUdp client = new EchoClientUdp("localhost");
                BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                String s = bufferRead.readLine();
                System.out.println(client.sendEcho(s));
                if ("tchau".equals(s)) {
                    exit = true;
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}