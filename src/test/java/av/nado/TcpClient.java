package av.nado;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpClient {
    public static void main(String[] args) throws IOException {
        try {
            byte a[] = { (byte) 192, (byte) 168, 2, (byte) 133 };
            InetAddress address = InetAddress.getByAddress(a);
            Socket socket = new Socket(address, 8888);
            socket.setKeepAlive(true);
            System.out.println("is connected...");
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os, true);
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(isr);
            String line;
            line = "A line sent by the client";
            out.println(line);
            line = in.readLine();
            System.out.println("client got: " + line);
            out.close();
            in.close();
            socket.close();
        } catch (UnknownHostException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
