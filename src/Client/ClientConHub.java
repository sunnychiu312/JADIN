import java.io.IOException;

public class ClientConHub{
  public static void main( String[] args) throws IOException{
    if(args.length!=1) {
        System.out.println("Input only employee key");
        System.exit(1);
    }
    Client employee = new Client(args [0]);
  }
}
