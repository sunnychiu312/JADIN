import java.util.ArrayList;
import java.net.*;
import java.io.IOException;
import java.io.FileWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;
import java.util.Random;
import java.io.UnsupportedEncodingException;
import java.io.File;

public class WriteThread extends Thread {

    Socket client;
    String done = "DONE";

    String[] whoami;
    String my_alias;
    InetAddress myaddress;
    ArrayList<String> key_list;

    String hubsocket;
    ArrayList<String[]> whereisthedata;
    String filename;
    ConcurrentHashMap<Long, String[]> pingportipdata;
    ConcurrentHashMap<String, String> hub_status;
    ArrayList<String[]> otherhubs;
    ArrayList<String[]> reachable_servers;
    Socket remote_server;

    private String serv_address;
    private int serv_port;

    public WriteThread(Socket _client, String[] _whoami, ConcurrentHashMap<String, String> _map, ArrayList<String[]> _servers, String _alias, ArrayList<String> _list  ) throws UnknownHostException {
        client = _client;
        remote_server = new Socket();
        otherhubs = new ArrayList<>();
        this.pingportipdata = new ConcurrentHashMap<Long, String[]>();
        this.whoami = _whoami;
        this.myaddress = InetAddress.getByName(whoami[0]);
        this.hub_status = _map;
        this.reachable_servers = _servers;
        this.my_alias = _alias;
        this.key_list = _list;
    }

    public void run() {
        try {
            connect_to_remote();
            check_other_hubs();
            //parse clients input, read filename, send to servers
            read_parse();
            // read stream from servers and process the stream into a string and parse the data
            servers_return();
            //ping all the servers that the data is stored on
            ping_server_fromhere();
            //store the server data for where the data is stored
            write_file_location_txt();

            //#TODO send back RITE or FAIL
            client.close();
            remote_server.close();
        }
        catch (IOException | InterruptedException e) {

            System.out.println("IO exception from write thread");
        }

    }

    // checks reachable
    public void connect_to_remote() throws UnknownHostException, UnsupportedEncodingException{
        if (reachable_servers.size() != 0) {
            String[] serverdata = reachable_servers.get(new Random().nextInt(reachable_servers.size()));
            try {
                Socket sock = new Socket();
                InetAddress server_address = InetAddress.getByName(serverdata[0]);
                InetSocketAddress endpoint = new InetSocketAddress(server_address, Integer.valueOf(serverdata[1]));
                sock.connect(endpoint);    //if we can connect, then the hub is still reachable
                sock.getOutputStream().write("CHEK".getBytes("US-ASCII"));
                byte[] rbuf = new byte[4];
                sock.getInputStream().read(rbuf);
                String ret = new String(rbuf, "US-ASCII");
                sock.close();
                if (ret.equals("ACPT")) {
                    remote_server.connect(endpoint);
                }
                else {
                    System.out.println("wooops its broken");

                }
            } catch (IOException e) {
                connect_to_remote();
            }
        }
        else {
            System.out.println("hub is not connected to any servers");
        }

        //check thru list of reachable Servers
        // set remote_server to the first server that works
    }
    public void check_other_hubs() throws UnknownHostException, IOException, InterruptedException {
        checkhub_availability();
        System.out.println("checking total hubs: " + hub_status.size());
        for (String key : hub_status.keySet()) {
            System.out.println(hub_status.get(key));
            if (hub_status.get(key).equals("unreachable")) {
                //do nothing
            }
            else if (hub_status.get(key).equals("reachable")) {
                String[] hub = new String[]{key.split(":")[0], key.split(":")[1]};
                if ( Arrays.equals(hub, whoami) == false) {
                    otherhubs.add(hub);
                }
            }
        }

        //check thru list of reachable Servers
        // set remote_server to the first server that works
    }

    //checks unavailable and available hub lists and makes changes according to availability
    public void checkhub_availability() throws UnknownHostException, IOException, InterruptedException{
        //some sort of FileNotFoundException
        ThreadHubCheck ct = new ThreadHubCheck(hub_status, whoami);
        ct.start();
        ct.join();
    }
    //send client data to server and wait for server to return DONE and then the routing table.
    public void read_parse() throws IOException {
        byte[] rbuf = new byte[client.getInputStream().available()];   //only read in the first 4 bytes to decide what to do before bothering to read in more stuff...
        client.getInputStream().read(rbuf);
        //check clients's key on the READ request.
        String inputstring = new String(rbuf, "US-ASCII");
        int endofkey = inputstring.indexOf(":");

        //parsing for stuff we need
        String clientkey = inputstring.substring(0, endofkey);
        filename = inputstring.substring(endofkey+1, inputstring.indexOf("{") );
        String payload = inputstring.substring(inputstring.indexOf("{"), inputstring.length());
        System.out.println(clientkey);
        System.out.println(key_list.contains(clientkey));

        check_if_file_exists();

        if (!checkinputstream_key(clientkey)) {
            System.out.println("invalid client key ");
            client.getOutputStream().write("FAIL".getBytes("US-ASCII"));
            client.close();
            remote_server.close();
        }
        else {
            System.out.println("TESTING valid client key continuing...");
            System.out.println("FILENAME IS " + filename);

            String deliverable = "RITE" + filename + payload;
            remote_server.getOutputStream().write(deliverable.getBytes("US-ASCII"));

        }
    }

    public void check_if_file_exists() throws IOException{
      String filepath = "./directories/" + my_alias + "/";

      File file = new File(filepath + filename + ".txt");
      if ( file.exists()) {
        client.getOutputStream().write("FAIL".getBytes("US-ASCII"));
        client.close();
        remote_server.close();
      }
      else {
        System.out.println("File " + filename + "does not exist. continuing");
      }
    }

    public boolean checkinputstream_key(String _key) {
        System.out.println(_key);
        for (int i = 0; i < key_list.size(); i ++) {
            System.out.println(key_list.get(i));
            if (_key.equals(key_list.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void servers_return() throws IOException {
        byte[] ret = new byte[4];
        remote_server.getInputStream().read(ret, 0, 4);
        String msgtype = new String(ret, "US-ASCII");
        if (msgtype.equals("DONE")) {    //if server sends done, we will get back the routing table map for where the data is put
            byte[] retlength = new byte[remote_server.getInputStream().available()];
            remote_server.getInputStream().read(retlength);
            String routingtablestring = new String(retlength, "US-ASCII");   //string will be [ip:port, ip:port....]
            int firstBracket = routingtablestring.indexOf("[");
            filename = routingtablestring.substring(0, firstBracket);


            System.out.println("TESTING: filename is: " + filename);
            whereisthedata = string_to_array(routingtablestring);
            routingtablestring =  "RITE"+ routingtablestring.substring(firstBracket, routingtablestring.length() );;   //string should now be [ip:port, ip:port, ....]
            client.getOutputStream().write(routingtablestring.getBytes("US-ASCII"));

        }
    }


    public void send_routing_to_hubs(String _stream) throws UnknownHostException, IOException {
        String outmsg = "SAVE" + _stream;
        //no need to check hubs since it is checked before this thread starts
        for (int i = 0; i < otherhubs.size() ; i++) {   //send SAVEfilename[ipport,ipport..] to all available servers
            String[] ipport = otherhubs.get(i);
            System.out.println(ipport[0] + ipport[1]);
            Socket sock = new Socket();
            InetAddress sa = InetAddress.getByName(ipport[0]);
            InetSocketAddress endpoint = new InetSocketAddress(sa, Integer.valueOf(ipport[1]));
            sock.connect(endpoint);
            sock.getOutputStream().write(outmsg.getBytes("US-ASCII"));
            sock.close();
        }
    }

    public void ping_server_fromhere() throws InterruptedException{
        for (int i = 0 ; i<whereisthedata.size() ; i++) {
            String[] remote = whereisthedata.get(i);
            UdpPingSend pinger = new UdpPingSend(remote[0], Integer.valueOf(remote[1]), pingportipdata );
            pinger.start();
            pinger.join();
        }

    }

    public ArrayList<String[]> string_to_array(String _s) {

        System.out.println("SUB: " + _s);
        _s = _s.substring(_s.indexOf("[")+1,_s.length() - 1);
        System.out.println("SUB: " + _s);

        String[] ipports = _s.split(",");
        ArrayList<String[]> routingtable = new ArrayList<>();

        for ( int i = 0; i<ipports.length ; i++) {
            String current_val = ipports[i];
            String[] ipport = new String[]{ current_val.split(":")[0], current_val.split(":")[1] };
            routingtable.add(ipport);
            System.out.println("TESTING ip port of where file is: " + ipport[0] + ":" + ipport[1]);
        }
        return routingtable;
    }


    public void write_file_location_txt() throws IOException{
        //organize pings for where the file is
        Long [] pings = pingportipdata.keySet().toArray(new Long [pingportipdata.size()]);

        Arrays.sort(pings);
        String filepath = "./directories/" + my_alias + "/";
        check_make_directory();
        try{
            FileWriter file = new FileWriter(filepath + filename+ ".txt");
            for ( int i = 0; i < pings.length; i++) {
                long ping = pings[i];
                String[] ipport = pingportipdata.get(ping);
                String content = ipport[0] + ":" +  ipport[1] + "\n";
                file.write(content);
                file.flush();

            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException couldn't write file");
            client.getOutputStream().write("FAIL".getBytes("US-ASCII"));
            client.close();
            remote_server.close();
        }
    }

    //  checks if the directory for the hub is created, otherwise make it
    public void check_make_directory() throws IOException{
        File f = null;
        try {
            f = new File("./directories/"+ my_alias );
            Boolean dir = f.isDirectory();
            if (!dir) {
                f.mkdir();
            }
        } catch( SecurityException e) {
            System.out.println("directory error");
            client.getOutputStream().write("FAIL".getBytes("US-ASCII"));
            client.close();
            remote_server.close();
        }
    }
}
