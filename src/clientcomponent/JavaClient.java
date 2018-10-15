import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class JavaClient {

    public static void main(String[] args) {
        System.out.println("Run Client");


        if (args.length != 2) {
            System.out.println("usage: java client <port> <something>"); // if they dont properly do arguements
            System.exit(1);
        }

        int port = isParseInt(args[0]); // port arguement
        if (port == -1) {
            System.out.println("usage: java client <port> <something>");
            System.out.println("<port>: integer");
            System.exit(1);
        }
        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        try {
                socket = new Socket("127.0.0.1", port); // socket
                System.out.println("Connected...");

//                PassAlongThread send = new PassAlongThread(socket, 1);
//                PassAlongThread recv = new PassAlongThread(socket, 2);
//                send.start();
//                recv.start();
                PassAlongThread both = new PassAlongThread(socket, 3);
                both.start();

        } catch (IOException ex) {
            System.out.println(ex.toString());
//        } finally{
//
//            if(bufferedReader != null){
//                try {
//                    bufferedReader.close();
//                    System.out.println("bufferedReader closed");
//                } catch (IOException ex) {
//                    System.out.println(ex.toString());
//                }
//            }
//
//            if(printWriter != null){
//                printWriter.close();
//                System.out.println("printWriter closed");
//            }
//
//            if(socket != null){
//                try {
//                    socket.close();
//                    System.out.println("socket closed");
//                } catch (IOException ex) {
//                    System.out.println(ex.toString());
//                }
//            }
//        }

        }
    }
    
    private static int isParseInt(String str){
        
        int num = -1;
        try{
	    num = Integer.parseInt(str);
        } catch (NumberFormatException e) {
        }
        
        return num;
    }
    
}
