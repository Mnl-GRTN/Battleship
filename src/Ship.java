public class Ship {
    private String type;
    private int size;
    private int[] positionInit;
    private String direction;
    private boolean sunk;
    private int nbTouche;

    public Ship(String type, int size) {
        this.type = type;
        this.size = size;
        this.direction = "null";
    }

    public String getType() {
        return this.type;
    }

    public int getSize() {
        return this.size;
    }

    public int[] getPositionInit() {
        return this.positionInit;
    }

    public void setPositionInit(int row, int col) {
        int[] position = {row, col};
        this.positionInit = position;
    }

    public String getDirection() {
        return this.direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isSunk() {
        return this.sunk;
    }


    public boolean isTouched(int x,int y){
        for (int i=0; i<this.size; i++){
            if (this.direction.equals("right")) {
                if (this.positionInit[0] == x && this.positionInit[1] + i == y) {
                    return true;
                }
            } else if (this.direction.equals("down")) {
                if (this.positionInit[0] + i == x && this.positionInit[1] == y) {
                    return true;
                }
            } else if (this.direction.equals("left")) {
                if (this.positionInit[0] == x && this.positionInit[1] - i == y) {
                    return true;
                }
            } else if (this.direction.equals("up")) {
                if (this.positionInit[0] - i == x && this.positionInit[1] == y) {
                    return true;
                }
            }
        }
        return false;
    }

    public void touch(){
        this.nbTouche++;
        if(this.nbTouche == this.size){
            this.sunk = true;
        }
    }

}