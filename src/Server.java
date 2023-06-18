import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static List<Game> games;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        games = new ArrayList<Game>();
        System.out.println("Server started");

        while (true) {
            Socket player1Socket = serverSocket.accept();
            System.out.println("Player 1 connected");
            PrintWriter out = new PrintWriter(player1Socket.getOutputStream(), true);
            out.println("Waiting for player 2 to connect");

            Socket player2Socket = serverSocket.accept();
            System.out.println("Player 2 connected");

            Game game = new Game(player1Socket, player2Socket, games.size()+1);
            games.add(game);
            System.out.println("--> Game started number " + games.size());
            new Thread(game).start();
        }
    }
}
