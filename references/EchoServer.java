import java.net.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

// Aguarda comunicação no porto 6500,
// recebe mensagens e devolve-as
public class EchoServer {
    static Random rand = new Random();

    static String[] arrayResp = new String[]{"Bom dia", "bem disposto", "ola", "boas"};

    public static String listar() {
        return arrayResp[0] + "," + arrayResp[1] + "," + arrayResp[2] + "," + arrayResp[3] + " - TCP";
    }

    public static String frase() {
        return arrayResp[rand.nextInt(4)] + " - TCP";
    }

    public static String horas() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now) + " - TCP";
    }

    public static void main(String args[]) throws Exception {
        ServerSocket server = new ServerSocket(6500);
        System.out.println ("servidor iniciado no porto 6500");
        Socket socket = null;
        //aguarda mensagens
        while(true) {
            socket = server.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream ps = new PrintStream(socket.getOutputStream());
            String linha = br.readLine();
            String ret = "Comando inválido";
            if ("tchau".equals(linha)) {
                ret = "Servidor.fim";
            }
            else if ("horas".equals(linha)) {
                ret = horas();
            }
            else if ("listar".equals(linha)) {
                ret = listar();
            }
            else if ("frase".equals(linha)) {
                ret = frase();
            }
            ps.println(ret);
            socket.close();
        }
    }
}
//