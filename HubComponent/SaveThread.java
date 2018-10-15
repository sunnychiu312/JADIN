import java.util.ArrayList;
import java.net.*;
import java.io.IOException;
import java.io.FileWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;

public class SaveThread extends Thread {

    String[] whoami;
    InetAddress myaddress;

    Socket ingress;

    String filename;
    ArrayList<String[]> dataroutingtable;
    ConcurrentHashMap<Long, String[]> routingpings;

    public SaveThread(String[] _array, Socket _ingress) throws UnknownHostException {
        this.whoami = _array;
        this.myaddress = InetAddress.getByName(whoami[0]);
        this.ingress = _ingress;
        this.routingpings = new ConcurrentHashMap<>();
    }

    public void run() {

        try {
            parse();
            ping_server_fromhere();
        } catch (InterruptedException | IOException e) {
            System.out.println("pinging interrupted");
        }

        write_file_location_txt();
    }

    public void parse() throws IOException {
        byte[] retlength = new byte[ingress.getInputStream().available()];
        ingress.getInputStream().read(retlength);
        String routingtablestring = new String(retlength, "US-ASCII");   //string will be [ip:port, ip:port....]

        int firstBracket = routingtablestring.indexOf("[");
        filename = routingtablestring.substring(0, firstBracket);
        System.out.println("TESTING incoming file to replicate is: " + filename);

        routingtablestring =  routingtablestring.substring(firstBracket+1, routingtablestring.length() - 1);;   //string should now be ip:port, ip:port, ....etc
        dataroutingtable = string_to_array(routingtablestring);
    }

    public ArrayList<String[]> string_to_array(String _s) {
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

    public void ping_server_fromhere() throws InterruptedException{

        for (int i = 0 ; i<dataroutingtable.size() ; i++) {
            String[] remote = dataroutingtable.get(i);
            UdpPingSend pinger = new UdpPingSend(remote[0], Integer.valueOf(remote[1]), routingpings );
            pinger.start();   //will attempt to ping server 4 times and write ping in concurrent hashmap
            pinger.join();
        }

    }

    public void write_file_location_txt() {
        System.out.println(routingpings.toString());
        //organize pings for where the file is
        Long [] pings = routingpings.keySet().toArray(new Long [routingpings.size()]);
        Arrays.sort(pings);

        System.out.println(Arrays.toString(pings));
        //write text file ip ports by order of lowest ping to highest

        try (FileWriter file = new FileWriter(filename+ ".txt")) {
            for ( int i = 0; i < pings.length; i++) {
                long ping = pings[i];
                String[] ipport = routingpings.get(ping);
                String content = ipport[0] + ":" +  ipport[1];
                file.write(content);
                file.flush();

            }
        } catch (IOException e) {
            System.out.println("IOException couldn't write file");
        }
    }






}
