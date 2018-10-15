import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.InterruptedException;
import java.util.HashMap;

public class Hub {

    ServerSocket ingress_srv_sock;

    ArrayList<String[]> reachable_hubs;
    ArrayList<String[]> potential_hubs;
    ArrayList<String[]> unreachable_hubs;

    ArrayList<String[]> my_reachable_servers;


    ConcurrentHashMap<String, String> hub_status;
    String[] whoami;



//#TODO i think i can get rid of unreachable_hubs
//#TODO check if READ still works
//#TODO mak the infinite hub loop stop
    public Hub(String _config, String _alias) throws IOException, InterruptedException {
        potential_hubs = new ArrayList<>();
        reachable_hubs = new ArrayList<>();
        hub_status = new ConcurrentHashMap<>();
        init_config(_config, _alias);

        unreachable_hubs = potential_hubs;
        unreachable_hubs.remove(whoami);

        my_reachable_servers = new ArrayList<>();



        connect_to_initial_server("127.0.0.1", "9090");

        //init UDP ping listen
        UdpPingListen listener = new UdpPingListen(InetAddress.getByName(whoami[0]), Integer.valueOf(whoami[1]));
        listener.start();


        //main tcp loop
        newClientConnection();
    }

    //each hub will listen as a server on a different port/ip
    //init a hub listening in on the specified alias/port
    //input file of all address the hub ports will be listening in on
    public void init_config(String _inputconfig, String _alias) throws IOException{
        File configfile = new File(_inputconfig);
        try {
            Scanner sc = new Scanner(configfile);
            while (sc.hasNextLine()) {
                String[] ip_port = new String[2];

                String i = sc.nextLine();
                String[] split_line = i.split(" ");
                if(i.isEmpty()){
                    continue;
                }
                else{
                    String config_alias = split_line[1];
                    if(split_line.length != 4){
                        System.out.println("Config file incomplete");
                        System.exit(1);
                    }
                    String ip = split_line[2];
                    String port = split_line[3];
                    check_rex(ip, "(\\d+).(\\d+).(\\d+).(\\d+)");
                    check_rex(port, "(\\d+)");
                    ip_port[0] = ip;
                    ip_port[1] = port;
                    hub_status.put( ip_port[0] + ":" + ip_port[1], "unreachable" );    //every hub will be initiailized as unreachable until checked
                    potential_hubs.add(ip_port);
                    if(!(config_alias.equals(config_alias.toLowerCase()))){
                        System.out.println("Incorrect alias information");
                        System.exit(1);
                    }
                    if(_alias.equals(config_alias)){
                        whoami = ip_port;
                        hub_status.replace(whoami[0] + ":" + whoami[1], "reachable");
                        System.out.println("Attempting to run hub on IP:port " + whoami[0] + ":" + whoami[1]);
                        create_ingress_socket(ip_port[0], Integer.valueOf(ip_port[1]));
                    }
                }
            }
            sc.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(_inputconfig + "file not found");
        }

    }

    public static void check_rex(String value, String pattern){
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(value);
        if (!m.find( )) {
            System.out.println("Incomplete config file");
            System.exit(1);
        }
    }

    /*
    sets up a server socket listening on the specified ip and port
     */
    public void create_ingress_socket(String address, int port) throws IOException{
        InetAddress server_address;
        try {
            server_address = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.err.println("Bad server address.");
            System.exit(1);
            return;
        }
        ingress_srv_sock = new ServerSocket(port, 16, server_address);
    }

    public Socket create_egress_socket(String address, int port, int timeout) throws IOException{
        InetAddress server_address;
        InetSocketAddress endpoint;

        server_address = InetAddress.getByName(address);
        endpoint = new InetSocketAddress(server_address, port);
        Socket hubsocket = new Socket();

        try {
            hubsocket.connect(endpoint, timeout);
        } catch(ConnectException e) {
            System.err.println("Cannot connect to server.");
        }
        return hubsocket;
    }

    public void newClientConnection() throws IOException, InterruptedException{
       while(true){
           System.out.println("ServerHUB listening on " + whoami[0] + ":" + whoami[1]);
           Socket ingress_sock = ingress_srv_sock.accept();

           // String client_address = ingress_sock.getRemoteSocketAddress().toString();

           byte[] rbuf = new byte[4];   //only read in the first 4 bytes to decide what to do before bothering to read in more stuff...
           ingress_sock.getInputStream().read(rbuf);
           String decoded_type = new String(rbuf, "US-ASCII");

           System.out.println("Handling a: " + decoded_type);
           handle_inputstream(decoded_type, ingress_sock);

       }
    }

    public void handle_inputstream(String _s, Socket inc_sock) throws IOException {

        if (_s.equals("RITE")) {        //client sent a write request
            WriteThread riting = new WriteThread(inc_sock,  whoami, hub_status, my_reachable_servers);
            riting.start();

        }
        else if (_s.equals("SAVE")) {   //got a message form another hub, meaning it needs to write a txt file of filename [ip:port, ip:port...]
            SaveThread st = new SaveThread(whoami, inc_sock);
            st.start();
        }
        else if (_s.equals("READ")) {    //client sent a read request
            ReadThread reading = new ReadThread(inc_sock);
            reading.start();


        }
        else if (_s.equals("LOGN")) {     //client is logging in to hub...

        }
        else if (_s.equals("HUBS")) {
            inc_sock.getOutputStream().write("ACPT".getBytes("US-ASCII"));
            System.out.println("Another hub is connecting/checking ");
        }
        else if (_s.equals("CHEK")) {    //hub is socketing into another hub
            String accept = "ACPT";
            inc_sock.getOutputStream().write(accept.getBytes("US-ASCII"));
            System.out.println("Accepted connection from: "+ inc_sock);
        }
        else {
            System.out.println("Received invalid message type");
        }
        System.out.println(7);
    }




   //will check each of the two initial servers to see if they are available, if they are. store in list. if not. dont store in list
    public void connect_to_initial_server(String ip1, String port1) throws UnsupportedEncodingException, IOException {
        // try to connect to first server and CHEK if it is available
        try {
            Socket testserv1sock = create_egress_socket(ip1, Integer.valueOf(port1), 5000);
            testserv1sock.getOutputStream().write("CHEK".getBytes("US-ASCII"), 0, 4 );   //send checking message to server
            byte[] server_return1 = new byte[4];
            testserv1sock.getInputStream().read(server_return1);    //check if this like blocks on read and waits hopefully this does wait on this line.......
            String server_return_decoded1 = new String(server_return1, "US-ASCII");   //decode servers return to String
            if (server_return_decoded1.equals("ACPT")) {
                System.out.println("Server " + ip1 + ":" + port1 + "DID send a response. ");
                String[] address = new String[]{ip1, port1};
                my_reachable_servers.add(address);
            }
            else {   //at this point the socket is closed, server connection failed
                System.out.println("Server was not reachable");
            }
        }
        catch (SocketTimeoutException | SocketException e) {    //
            System.out.println("Server " + ip1 + ":" + port1 + "did NOT send a response. ");

        }

    }

}
