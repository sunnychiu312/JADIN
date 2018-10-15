import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

// Client class
public class client2hub
{
    public static void main(String[] args) throws IOException
    {
        try
        {
            Scanner scn = new Scanner(System.in);

//            getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection with server port 5056
            Socket s = new Socket(ip, 5056); // connects to server here

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            Thread t = new ClientHandler(s, dis, dos);

            //Invoking the start() method
            t.run();



            //exchange information between client and client handler(handler uses threads)
//            while (true)
//            {
////                System.out.println(dis.readUTF());
//                String tosend = scn.nextLine();
//                dos.writeUTF(tosend);
//
//                // If client sends exit,close this connection
//                // and then break from the while loop
//                if(tosend.equals("Exit"))
//                {
//                    System.out.println("Closing this connection : " + s);
//                    s.close();
//                    System.out.println("Connection closed");
//                    break;
//                }
//
//                // printing date or time as requested by client
//                String received = dis.readUTF();
//                System.out.println(received);
//            }

            // closing resources
//            scn.close();
//            dis.close();
//            dos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    // ClientHandler class
    static class ClientHandler extends Thread
    {
        DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
        DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
        final DataInputStream dis;
        final DataOutputStream dos;
        final Socket s;


        // Constructor
        public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
        {
            this.s = s;
            this.dis = dis;
            this.dos = dos;
        }

        @Override
        public void run()
        {
            String received;
            String toreturn;
            while (true)
            {
                try {

                    // Ask user what he wants
                    dos.writeUTF("What do you want?[Date | Time]");

                    // receive the answer from client
                    received = dis.readUTF();

                    if(received.equals("Exit"))
                    {
                        System.out.println("Client " + this.s + " sends exit...");
                        System.out.println("Closing this connection.");
                        this.s.close();
                        System.out.println("Connection closed");
                        break;
                    }

                    // creating Date object
                    Date date = new Date();

                    // write on output stream based on the
                    // answer from the client
                    switch (received) {

                        case "Date" :
                            toreturn = fordate.format(date);
                            dos.writeUTF(toreturn);
                            break;

                        case "Time" :
                            toreturn = "1234"; //fortime.format(time)
                            dos.writeUTF(toreturn);
                            break;

                        default:
                            dos.writeUTF("Invalid input");
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try
            {
                // closing resources
                this.dis.close();
                this.dos.close();

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

}
