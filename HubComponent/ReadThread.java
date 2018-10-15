import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ReadThread extends Thread {

    Socket ingress;

    String filename;
    ArrayList<String[]> filelocations;

    String payload_from_server;

    public ReadThread(Socket _i) {
        this.ingress = _i;
        filelocations = new ArrayList<>();
    }

    public void run() {
        try {
            getfilename();
            read_file_for_address();
            //connect to the server with the data and retrieve it
            connect_to_servers();
            //send the data back to the client
            return_data_client();

        } catch (IOException e) {
            System.out.println("error in read threading");
        }

    }

    public void getfilename() throws UnsupportedEncodingException, IOException {
        byte[] rbuf = new byte[ingress.getInputStream().available()];   //only read in the first 4 bytes to decide what to do before bothering to read in more stuff...
        ingress.getInputStream().read(rbuf);
        filename = new String(rbuf, "US-ASCII");
    }

    public void read_file_for_address() throws IOException{
        try {
            File file = new File(filename+ ".txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            System.out.println("Searching for location file for: " + filename);
            while ((st = br.readLine()) != null) {
                System.out.println("TESTING: each line of text file: " + st);
                String[] ipport = new String[2];
                ipport[0] = st.split(":")[0];
                ipport[1] = st.split(":")[1];
                filelocations.add(ipport);
            }
            if (filelocations.size() == 0) {
                System.out.println("something went wrong and we dont know the servers for the file");
            }
        }   catch (FileNotFoundException e) {
            System.out.println("Could not find file " + filename);
        }

    }

    public void connect_to_servers() throws IOException{
        String content;
        int failcounter = 0;
        for (int i = 0 ; i < filelocations.size() ; i++ ) {
            String[] serveraddr = filelocations.get(i);
            try {
                Socket egress = create_egress(serveraddr[0], Integer.valueOf(serveraddr[1]));
                content = "READ" + filename;
                egress.getOutputStream().write(content.getBytes("US-ASCII"));
                listen_server_confirmation(egress);
                if (failcounter > 0) {
                    String payload = "RITE" + filename + "_" + failcounter;
                    egress.getOutputStream().write(payload.getBytes("US-ASCII"));
                }
                break;

            } catch (IOException e) {
                failcounter++;
                System.out.println("uh oh could NOT connect to the server");
            }
        }
    }

    public void listen_server_confirmation(Socket _s) throws IOException {
        byte[] ret = new byte[4];
        _s.getInputStream().read(ret,0,4);
        String msgtype = new String(ret, "US-ASCII");
        if (msgtype.equals("DONE")) {
            byte[] retlen = new byte[_s.getInputStream().available()];
            _s.getInputStream().read(retlen);
            System.out.println(retlen);
            payload_from_server = new String(retlen, "US-ASCII");
            System.out.println("TESTING received from the server: " + payload_from_server);
        }
        else if (msgtype.equals("FAIL")) {

        }
    }

    public void return_data_client() throws IOException{
        ingress.getOutputStream().write(payload_from_server.getBytes("US-ASCII"));
    }

    // returns a socket connected to the ip and port
    public Socket create_egress(String ip, int port) throws IOException {
        Socket sock = new Socket();
        InetAddress server_address;
        InetSocketAddress endpoint;
        server_address = InetAddress.getByName(ip);
        endpoint = new InetSocketAddress(server_address, Integer.valueOf(port));

        try {
            sock.connect(endpoint);

        } catch (ConnectException e) {
            System.out.println("couldnt egress");
        }

        return sock;
    }


}
