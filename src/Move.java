

class Move
{
    private int row;
    private int col;

    public Move(){
        row = -1;
        col = -1;
    }

    public Move(int r, int c){
        row = r;
        col = c;
    }

    public int getRow(){
        return row;
    }

    public int getCol(){
        return col;
    }

    public void setRow(int r){
        row = r;
    }

    public void setCol(int c){
        col = c;
    }

    @Override
    public String toString() {
        char colChar = (char) ('A' + col); // Convert column index (0-8) to 'A'-'I'
        int rowNumber = row + 1; // Convert row index (0-8) to (1-9)

        return colChar + Integer.toString(rowNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return row == move.row && col == move.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}
