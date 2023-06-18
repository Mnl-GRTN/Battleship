import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class Client{

    private static BufferedReader in;
    private static PrintWriter out;
    private static char[][] BoatBoard = new char[10][10];
    private static char[][] AttackBoard = new char[10][10];

    public static void main(String[] args) throws IOException {
        try {
            Socket socket = new Socket("localhost", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                try {
                    String response = in.readLine();
                    System.out.println(response);
                    if ((response.startsWith("Place your")) || (response.startsWith("Randomly place"))){
                        String shipcoord = input.readLine();
                        out.println(shipcoord);
                    }
                    else if ((response.startsWith("Insert the direction of the ship")) || (response.startsWith("Invalid direction"))){
                        String direction = input.readLine();
                        out.println(direction);
                    }
                    else if (response.startsWith("Your turn") || response.startsWith("Invalid input")) {
                        String attack = input.readLine();
                        out.println(attack);
                    }
                    else if(response.startsWith("Opponent's turn")){
                        convertBoards();
                    }
                    else if (response.startsWith("You win") || response.startsWith("You lose") || response.startsWith("Time out")) {
                        break;
                    }
                } catch (SocketException e) {
                    System.out.println("Time out, you lose, you run out of time to play.");
                    break;
                }
            }
            socket.close();
        } catch (ConnectException e) {
            System.out.println("Server is not running, please try again later.");
        };
    }
    // Méthode qui enregistre les boards envoyés par le serveur sous forme de String (Evite les problèmes de synchronisation avec ObjectInputStream)
    //Permet de stocker l'état de la partie
    public static void convertBoards() throws IOException{
        String response = in.readLine();
        int i = 0;
        for(int row = 0; row < 10; row++){
            for(int col = 0; col < 10; col++){
                BoatBoard[row][col] = response.charAt(i);
                i++;
            }
        }
        response = in.readLine();
        i = 0;
        for(int row = 0; row < 10; row++){
            for(int col = 0; col < 10; col++){
                AttackBoard[row][col] = response.charAt(i);
                i++;
            }
        }
    }
}
