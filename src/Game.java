import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game implements Runnable {

    private Socket player1Socket;
    private Socket player2Socket;
    private int gameNumber;

    private BufferedReader player1In;
    private PrintWriter player1Out;

    private BufferedReader player2In;
    private PrintWriter player2Out;

    private char[][] player1Board;
    private char[][] player1OppBoard;
    private char[][] player2Board;
    private char[][] player2OppBoard;

    List<Ship> player1Ships;
    List<Ship> player2Ships;

    public boolean isRunning;

    private boolean player1Turn;

    public Game(Socket player1Socket, Socket player2Socket, int gameNumber) {
        this.player1Socket = player1Socket;
        this.player2Socket = player2Socket;
        this.gameNumber = gameNumber;

        try {
            player1In = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
            player1Out = new PrintWriter(player1Socket.getOutputStream(), true);

            player2In = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));
            player2Out = new PrintWriter(player2Socket.getOutputStream(), true);
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        player1Board = new char[10][10];
        player2Board = new char[10][10];
        player1OppBoard = new char[10][10];
        player2OppBoard = new char[10][10];
        player1Ships = new ArrayList<Ship>();
        player2Ships = new ArrayList<Ship>();

        player1Ships.add(new Ship("Carrier", 5));
        player1Ships.add(new Ship("Battleship", 4));
        player1Ships.add(new Ship("Cruiser", 3));
        player1Ships.add(new Ship("Submarine", 3));
        player1Ships.add(new Ship("Destroyer", 2));

        player2Ships.add(new Ship("Carrier", 5));
        player2Ships.add(new Ship("Battleship", 4));
        player2Ships.add(new Ship("Cruiser", 3));
        player2Ships.add(new Ship("Submarine", 3));
        player2Ships.add(new Ship("Destroyer", 2));
        
        player1Turn = true;
        isRunning = true;
    }

    @Override
    public void run() {
        //Initialisation des plateaux et envoi des plateaux aux clients
        initializeBoards();
        // Placement des bateaux des joueurs
        try {
            placeShips();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Début de la partie
        while (isRunning) {
            String input = null;
            try {
                if (player1Turn) {
                    // Attaque du joueur 1 et attente du joueur 2
                    player2Out.println("Opponent's turn. Waiting for opponent's attack...");
                    sendBoardsToClient(player2Board, player2Out,player2OppBoard);
                    player1Out.println("Your turn. Enter attack coordinates (row,column):");
                    while(isRunning){
                        input = getInputFromClient(player1In);
                        if(input == null){
                            break;
                        }
                        else if(input.matches("[0-9],[0-9]") && player1OppBoard[Integer.parseInt(input.split(",")[0])][Integer.parseInt(input.split(",")[1])] == ' '){
                            break;
                        }
                        else{
                            player1Out.println("Invalid input. Please enter attack coordinates (row,column):");
                        }
                    }
                } else {
                    // Attaque du joueur 2 et attente du joueur 1
                    player1Out.println("Opponent's turn. Waiting for opponent's attack...");
                    sendBoardsToClient(player1Board, player1Out, player1OppBoard);
                    player2Out.println("Your turn. Enter attack coordinates (row,column):");
                    while(isRunning){
                        input = getInputFromClient(player2In);
                        if(input == null){
                            break;
                        }
                        else if(input.matches("[0-9],[0-9]") && player2OppBoard[Integer.parseInt(input.split(",")[0])][Integer.parseInt(input.split(",")[1])] == ' '){
                            break;
                        }
                        else{
                            player2Out.println("Invalid input. Please enter attack coordinates (row,column):");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if(input == null){
                break;
            }
            else{
                int row = Integer.parseInt(input.split(",")[0]);
                int col = Integer.parseInt(input.split(",")[1]);
                String result = null;
                //Réception de l'attaque et envoi du résultat aux joueurs
                if(player1Turn){
                    result = attack(row, col, player1OppBoard, player2Board, player2Ships);
                    sendBoards(player1Board, player1OppBoard, player1Out);
                    player1Out.println("Attack result: " + result);
                    player2Out.println("Opponent attacked at row " + row + ", column " + col + " with result: " + result);
                }
                else{
                    result = attack(row, col, player2OppBoard, player1Board, player1Ships);
                    sendBoards(player2Board, player2OppBoard, player2Out);
                    player2Out.println("Attack result: " + result);
                    player1Out.println("Opponent attacked at row " + row + ", column " + col + " with result: " + result);
                }
                //Vérification de la fin de partie
                if (checkWin()) {
                    if (player1Turn) {
                        player1Out.println("You win! The connection will be closed.");
                        player2Out.println("You lose. The connection will be closed.");
                    } else {
                        player2Out.println("You win! The connection will be closed.");
                        player1Out.println("You lose. The connection will be closed.");
                    }
                    break;
                }
            }
            player1Turn = !player1Turn;
        }
    }

    private void placeShips() throws IOException {
        //Joueur 1 place ses bateaux
        player1Turn = true;
        player2Out.println("Wait for your opponent to place his ships.");
        String input = null;
        player1Out.println("Randomly place your ships on the board? (y/n)");
        input = getInputFromClient(player1In);
        if(input != null){
            if(input.equals("y")){
                for(Ship ship : player1Ships){
                    placeBoatRandom(player1Board, ship);
                }
                sendBoards(player1Board, player1OppBoard, player1Out);
            }
            else{
                for (Ship ship : player1Ships) {
                    player1Out.println("[Initialisation] Place your ships on the board.");
                    while (true) {
                        sendBoards(player2Board, player2OppBoard, player2Out);
                        placeBoardManually(ship, player1In, player1Out, player1Board);
                        if(placeShipsOnBoard(player1Board, player1OppBoard, player1Out, ship) == true){
                            break;
                        }
                    }
                }
            }
        }
        //Joueur 2 place ses bateaux
        player1Turn = false;
        player1Out.println("Wait for your opponent to place his ships.");
        player2Out.println("Randomly place your ships on the board? (y/n)");
        input = getInputFromClient(player2In);
        if(input != null){
            if(input.equals("y")){
                for(Ship ship : player2Ships){
                    placeBoatRandom(player2Board, ship);
                }
                sendBoards(player2Board, player2OppBoard, player2Out);
            }
            else{
                for (Ship ship : player2Ships) {
                    player2Out.println("[Initialisation] Place your ships on the board.");
                    while (true) {
                        sendBoards(player2Board, player2OppBoard, player2Out);
                        placeBoardManually(ship, player2In, player2Out, player2Board);
                        if(placeShipsOnBoard(player2Board, player2OppBoard, player2Out, ship) == true){
                            break;
                        }
                    }
                }
            }
    }
        player1Turn = true;
    }
    //Méthode pour placer un bateau manuellement
    public void placeBoardManually(Ship ship, BufferedReader playerIn, PrintWriter playerOut, char[][] playerBoard) throws IOException{
        playerOut.println("Place your " + ship.getType() + " (" + ship.getSize() + " cells). Enter coordinates (row,column):");
        String input = getInputFromClient(playerIn);
        if(input != null){
            if(input.matches("[0-9],[0-9]")){
                int row = Integer.parseInt(input.split(",")[0]);
                int col = Integer.parseInt(input.split(",")[1]);
                if ((row >= 0 && row < 10) && (col >= 0 && col < 10) && playerBoard[row][col] == ' ' && isOneDirectionAvailable(row, col, playerBoard, ship) == true) {
                    ship.setPositionInit(row, col);
                    while(isInPlateau(ship, playerBoard) != true){
                        playerOut.println("Insert the direction of the ship (up, down, left, right):");
                        input = playerIn.readLine();
                        ship.setDirection(input);
                    }
                } else {
                    playerOut.println("Invalid coordinates. Try again:");
                }
            }
            else{
                playerOut.println("Invalid coordinates. Try again:");
            }
        }
    }
    //Méthode pour vérifier si le bateau peut être placé dans la direction choisie
    public boolean placeShipsOnBoard(char[][] playerBoard, char[][] PlayerOppBoard,PrintWriter playerOut, Ship ship){
        int[] StartCoord = ship.getPositionInit();
        if(ship.getDirection().equals("up")){
            for(int i = 0; i < ship.getSize(); i++){
                playerBoard[StartCoord[0] - i][StartCoord[1]] = 'S';
            }
            sendBoards(playerBoard, PlayerOppBoard, playerOut);
            return true;
        }
        else if(ship.getDirection().equals("down")){
            for(int i = 0; i < ship.getSize(); i++){
                playerBoard[StartCoord[0] + i][StartCoord[1]] = 'S';
            }
            sendBoards(playerBoard, PlayerOppBoard, playerOut);
            return true;
        }
        else if(ship.getDirection().equals("left")){
            for(int i = 0; i < ship.getSize(); i++){
                playerBoard[StartCoord[0]][StartCoord[1] - i] = 'S';
            }
            sendBoards(playerBoard, PlayerOppBoard, playerOut);
            return true;
        }
        else if(ship.getDirection().equals("right")){
            for(int i = 0; i < ship.getSize(); i++){
                playerBoard[StartCoord[0]][StartCoord[1] + i] = 'S';
            }
            sendBoards(playerBoard, PlayerOppBoard, playerOut);
            return true;
        }
        return false;
    }

    //Méthode pour placer un bateau aléatoirement
    private void placeBoatRandom(char[][] board, Ship ship) {
        boolean bateau_place = false;
        Random rand = new Random();
        int row = rand.nextInt(10);
        int col = rand.nextInt(10);
        if(bateau_place == false){
            ship.setPositionInit(row, col);
            int direction = rand.nextInt(4);
            if(direction == 0){
                ship.setDirection("up");
            }
            else if(direction == 1){
                ship.setDirection("down");
            }
            else if(direction == 2){
                ship.setDirection("left");
            }
            else if(direction == 3){
                ship.setDirection("right");
            }

            if(isInPlateau(ship, board) == true && isOneDirectionAvailable(row, col, board, ship) == true){
                if(ship.getDirection().equals("up")){
                    for(int i = 0; i < ship.getSize(); i++){
                        board[row - i][col] = 'S';
                    }
                    bateau_place = true;
                }
                else if(ship.getDirection().equals("down")){
                    for(int i = 0; i < ship.getSize(); i++){
                        board[row + i][col] = 'S';
                    }
                    bateau_place = true;
                }
                else if(ship.getDirection().equals("left")){
                    for(int i = 0; i < ship.getSize(); i++){
                        board[row][col - i] = 'S';
                    }
                    bateau_place = true;
                }
                else if(ship.getDirection().equals("right")){
                    for(int i = 0; i < ship.getSize(); i++){
                        board[row][col + i] = 'S';
                    }
                    bateau_place = true;
                }
            }
            else{
                placeBoatRandom(board, ship);
            }
        }
    }
    //Méthode pour vérifier si le bateau est dans le plateau sans sortir ou chevaucher un autre bateau
    private boolean isInPlateau(Ship ship, char[][] board) {
        int[] StartCoord = ship.getPositionInit();
        if(ship.getDirection().equals("up")){
            for(int i = 0; i < ship.getSize(); i++){
                if((StartCoord[0] - i) < 0){
                    return false;
                }
                else if(board[StartCoord[0] - i][StartCoord[1]] != ' '){
                    return false;
                }
            }
            return true;
        }
        else if(ship.getDirection().equals("down")){
            for(int i = 0; i < ship.getSize(); i++){
                if((StartCoord[0] + i) > 9){
                    return false;
                }
                else if(board[StartCoord[0] + i][StartCoord[1]] != ' '){
                    return false;
                }
            }
            return true;
        }
        else if(ship.getDirection().equals("left")){
            for(int i = 0; i < ship.getSize(); i++){
                if((StartCoord[1] - i) < 0){
                    return false;
                }
                else if(board[StartCoord[0]][StartCoord[1] - i] != ' '){
                    return false;
                }
            }
            return true;
        }
        else if(ship.getDirection().equals("right")){
            for(int i = 0; i < ship.getSize(); i++){
                if((StartCoord[1] + i) > 9){
                    return false;
                }
                else if(board[StartCoord[0]][StartCoord[1] + i] != ' '){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    //Méthode pour vérifier si le bateau a au moins une direction disponible (Pour éviter de bloquer le programme si il n'y a plus de place lorsqu'il entourée de bateaux)
    public boolean isOneDirectionAvailable(int row, int col, char[][] Board, Ship ship) {
        int size = ship.getSize();
        
        if (row + size <= 10) {
            boolean isAvailable = true;
            for (int i = 0; i < size; i++) {
                if (Board[row+i][col] != ' ') {
                    isAvailable = false;
                }
            }
            if (isAvailable) {
                return true;
            }
        }
        
        if (col + size <= 10) {
            boolean isAvailable = true;
            for (int i = 0; i < size; i++) {
                if (Board[row][col+i] != ' ') {
                    isAvailable = false;
                }
            }
            if (isAvailable) {
                return true;
            }
        }
        
        if (row - size >= -1) {
            boolean isAvailable = true;
            for (int i = 0; i < size; i++) {
                if (Board[row-i][col] != ' ') {
                    isAvailable = false;
                }
            }
            if (isAvailable) {
                return true;
            }
        }
        
        if (col - size >= -1) {
            boolean isAvailable = true;
            for (int i = 0; i < size; i++) {
                if (Board[row][col-i] != ' ') {
                    isAvailable = false;
                }
            }
            if (isAvailable) {
                return true;
            }
        }
        
        return false;
    }
    
        
    //Méthode pour initialiser les plateaux
    private void initializeBoards() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                player1Board[i][j] = ' ';
                player2Board[i][j] = ' ';
                player1OppBoard[i][j] = ' ';
                player2OppBoard[i][j] = ' ';
            }
        }
    }
    //Méthode pour afficher les plateaux aux clients
    private void sendBoards(char[][] BoardPlayer, char[][] PlayerOppBoard, PrintWriter playerOut){
        String finalBoardStringPlayer1 = "                 Your Board               |      |        Your Opponent's Board      \n  | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 |      | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 |\n";
        finalBoardStringPlayer1 += "  -----------------------------------------      -----------------------------------------\n";
        for(int i = 0; i < 10; i++){
            finalBoardStringPlayer1 += i+" | ";
            for(int j = 0; j < 10; j++){
                finalBoardStringPlayer1 += BoardPlayer[i][j] + " | ";
            }
            finalBoardStringPlayer1 += "     | ";
            for(int j = 0; j < 10; j++){
                finalBoardStringPlayer1 += PlayerOppBoard[i][j] + " | ";
            }
            finalBoardStringPlayer1 += "\n  -----------------------------------------      -----------------------------------------\n";
        }
        playerOut.println(finalBoardStringPlayer1);
    }
    
    //Méthode pour attaquer
    private String attack(int row, int col, char[][] AtkPlayerBoard, char[][] OpponentBoard, List<Ship> OpponentShipList) {
        if (OpponentBoard[row][col] == ' ') {
            AtkPlayerBoard[row][col] = 'M';
            return "missed";
        } else {
            AtkPlayerBoard[row][col] = 'H';
            OpponentBoard[row][col] = 'X';
            for(Ship ship : OpponentShipList){
                if(ship.isTouched(row, col) == true){
                    ship.touch();
                    if(ship.isSunk() == true){
                        OpponentShipList.remove(ship);
                        return "hit and the ship "+ship.getType()+" is sunk";
                    }
                }
            }
            return "hit";
        }
    }
    //Méthode pour envoyer chaque ligne des plateaux aux clients (dans le but de stocker les plateaux dans les clients)
    private void sendBoardsToClient(char[][] board, PrintWriter out, char[][] boardOpp) {
        //Send the board to the client in a string format
        String finalBoardString = "";
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                finalBoardString += board[i][j];
            }
        }
        out.println(finalBoardString);
        finalBoardString = "";
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                finalBoardString += boardOpp[i][j];
            }
        }
        out.println(finalBoardString);
    }

    //Méthode pour déconnecter les clients si ils ne jouent pas pendant 2 minutes
    private String getInputFromClient(BufferedReader in) throws IOException {
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 120000) {
            if (in.ready() && isRunning) {
                return in.readLine();
            }
        }
        if(player1Turn){
            player2Out.println("You win. Your opponent run out of time to play. The connection will be closed.");
        }
        else{
            player1Out.println("You win. Your opponent run out of time to play. The connection will be closed.");
        }
        isRunning = false;
        player1In.close();
        player2In.close();
        player1Out.close();
        player2Out.close();
        player1Socket.close();
        player2Socket.close();
        Thread.currentThread().interrupt();
        return null;
    }

    //Méthode pour arrêter le jeu
    public boolean isStopped() {
        return isRunning;
    }

    //Méthode pour vérifier si un joueur a gagné
    private boolean checkWin() {
        if (player1Ships.isEmpty()) {
            return true;
        } else if (player2Ships.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
}
