import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

/** Rows increase from bottom to top, and Columns from left to right */

public class Game extends JFrame implements Runnable {

    private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

    private final int WIDTH = 700;                                          // window dimensions
    private final int HEIGHT = 650;

    private final int SQUAREWIDTH = 60;                                     // width of individual chess square

    private final int ROWS = 8;
    private final int COLS = 8;

    private final int BOARDWIDTH = SQUAREWIDTH * ROWS;                      // dimensions of chess board
    private final int BOARDHEIGHT = SQUAREWIDTH * COLS;

    private final int BOARDBUFFER = 20;                                     // pixel padding around board

    private int LEFTBUFFER = (WIDTH / 2) - (BOARDWIDTH / 2);                // padding to position chess board in center
    private int TOPBUFFER = (HEIGHT / 2) - (BOARDHEIGHT / 2);

    private int numbStartPieces = 32;

    private int sleepTime = 100;

    private String game_title = "Chess";

    private String[] columnNames = new String[] {"a","b","c","d","e","f","g","h"};

    private Color backgroundColor = new Color(65, 65, 45);
    private Color selectionColor = new Color(0, 241, 247);
    private Color blackSquare = new Color(0x312F14);
    private Color whiteSquare = new Color(0xF6FAC9);

    private Thread gameThread;

    private DrawPane canvas;
    private Container window;

    private ChessSquare[] chessSquares = new ChessSquare[ROWS*COLS];
    private ChessPiece[] chessPieces = new ChessPiece[numbStartPieces];
    private ArrayList<ChessPiece> takenPieces = new ArrayList<>();

    private ChessSquare firstClicked;                                     // first square selected (origin)
    private ChessSquare secondClicked;                                    // second square selected (destination)

    private class DrawPane extends JPanel {

        /**
         * This method is called implicitly.  It simply draws chess squares as
         * a collection of squares that are dark or light.
         */
        protected void paintComponent(Graphics g) {

            LEFTBUFFER = (canvas.getSize().width / 2) - (BOARDWIDTH / 2);
            TOPBUFFER = (canvas.getSize().height / 2) - (BOARDWIDTH / 2);

            g.setColor(backgroundColor);
            g.fillRect(LEFTBUFFER - BOARDBUFFER, TOPBUFFER - BOARDBUFFER,
                    BOARDWIDTH + (2 * BOARDBUFFER), BOARDHEIGHT + (2 * BOARDBUFFER));

            // Draw the chess squares
            int w_position, h_position;
            for (int j = 0; j < COLS; j++) {
                for (int i = 0; i < ROWS; i++) {
                    if (i % 2 == 0) {
                        if (j % 2 == 0) {
                            g.setColor(whiteSquare);
                        } else {
                            g.setColor(blackSquare);
                        }

                    } else {
                        if (j % 2 == 0) {
                            g.setColor(blackSquare);
                        } else {
                            g.setColor(whiteSquare);
                        }
                    }

                    w_position = (j * SQUAREWIDTH) + LEFTBUFFER;
                    h_position = (i * SQUAREWIDTH) + TOPBUFFER;

                    g.fillRect(w_position, h_position, SQUAREWIDTH, SQUAREWIDTH);
                }
            }

            // Draw chess pieces
            if (Arrays.stream(chessPieces).noneMatch(Objects::isNull)) {
                for (ChessPiece piece: chessPieces) {
                    piece.updatePosition(piece.row, piece.col);
                    g.drawImage(piece.image, piece.posx, piece.posy, null);
                }
            }

            // Draw selection boxes around selected squares
            if (Arrays.stream(chessSquares).noneMatch(Objects::isNull)) {
                for (ChessSquare sq: chessSquares) {
                    sq.updateWindowPosition();
                    if (sq.selected){
                        int[] xpoints = new int[] {sq.posx, sq.posx, sq.posx + SQUAREWIDTH, sq.posx + SQUAREWIDTH, sq.posx};
                        int[] ypoints = new int[] {sq.posy, sq.posy + SQUAREWIDTH, sq.posy + SQUAREWIDTH, sq.posy, sq.posy};
//                        g.drawPolygon();
                        g.setColor(selectionColor);
                        g.drawPolyline(xpoints, ypoints, 5);
                    }

                    //  Diplay row/col coordinate for every square
//                  g.setColor(Color.CYAN);
//                  g.drawString(sq.row + ", " + sq.col, sq.posx, sq.posy);
                }
            }

            g.setColor(Color.BLACK);

            // Draw row numbers and column names
            for (int i = 0; i < ROWS; i++) {
                g.drawString(columnNames[i], LEFTBUFFER + (int)(SQUAREWIDTH * (i + 0.5)), TOPBUFFER + (int)(SQUAREWIDTH * (COLS + 0.6)));
                g.drawString(String.valueOf(ROWS - i), LEFTBUFFER - (SQUAREWIDTH / 2), TOPBUFFER + (SQUAREWIDTH * i) + 3 * SQUAREWIDTH / 4);
            }
        }
    }

    private class ChessSquare {

        private int row;
        private int col;

        private int posx;
        private int posy;

        private String name;
        private ChessPiece occupant;

        private boolean selected = false;

        private ChessSquare(int r, int c, String s) {
            this.row = r;
            this.col = c;
            this.name = s;
            this.posx = LEFTBUFFER + SQUAREWIDTH * (this.col - 1);
            this.posy = TOPBUFFER + SQUAREWIDTH * (ROWS - this.row);        // Y must be switched to be on bottom of board
        }

        @Override
        public String toString() {
            String string = "square at row " + this.row + ", col " + this.col;

            string = (this.occupant == null) ? "empty " + string : string + ", with (" + this.occupant.name + ")";

            return string;
        }

        private void updateWindowPosition() {
            this.posx = LEFTBUFFER + SQUAREWIDTH * (this.col - 1);
            this.posy = TOPBUFFER + SQUAREWIDTH * (ROWS - this.row);
        }

        private void updateOccupant(ChessPiece piece) {
            this.occupant = piece;
        }

        private void removeOccupant() {
            this.occupant = null;
        }
    }

    private class ChessPiece {

        private int row;
        private int col;
        private int posx;
        private int posy;
        private String name;
        private String side;
        private String pathToImages = "/home/alex/Documents/coding/java/games/src/chess_pieces/";
        BufferedImage image = null;

        private void updatePosition(int r, int c) {
            this.row = r;
            this.col = c;
            this.posx = LEFTBUFFER + SQUAREWIDTH * (this.col - 1) + SQUAREWIDTH / 4;
            this.posy = TOPBUFFER + SQUAREWIDTH * (ROWS - this.row) + SQUAREWIDTH / 8;
        }

        private void removeFromBoard() {
            this.row = -1;
            this.col = -1;
            this.image = null;

        }

        private void loadImage(String filename) {
            try {
                this.image = ImageIO.read(new File(pathToImages + filename));
            } catch (IOException e) {
                System.out.println("Invalid file path to image: " + pathToImages + filename);
//                e.printStackTrace();
            }
        }

        private ChessPiece() {
        }

        private ChessPiece(int r, int c, String n, String imagepath, String playerOrOpponent) {
            name = n;
            side = playerOrOpponent;
            updatePosition(r, c);
            loadImage(imagepath);
        }
    }

    private class Pawn extends ChessPiece {

    }

    private void constructBoard() {

        int row = 1;
        int col = 1;

        for (int i = 0; i < chessSquares.length; i++) {

            System.out.println(columnNames[col-1] + col);

            chessSquares[i] = new ChessSquare(row, col, columnNames[col-1] + col);

            if (col < 8) col ++;

            else if (col == 8) {
                col = 1;
                row ++;
            }
        }

        /*
         * TODO: IF USER IS PLAYING WHITE: ..... ELSE: .....
         */

        // Construct chess pieces for each side

        int idx = 0;
        String side = "player";
        String color = "white";

        for (int j = 0; j < 2; j++) {

            col = 1;

            if (side.equals("player")) {row = 1;}
            else {row = 8;}

            chessPieces[idx++] = new ChessPiece(row, col++, "rook", "rook" + color + ".PNG", side);

            chessPieces[idx++] = new ChessPiece(row, col++, "horse", "horseleft" + color + ".PNG", side);

            chessPieces[idx++] = new ChessPiece(row, col++, "bishop", "bishop" + color + ".PNG", side);

            chessPieces[idx++] = new ChessPiece(row, col++, "queen", "queen" + color + ".PNG", side);

            chessPieces[idx++] = new ChessPiece(row, col++, "king", "king" + color + ".PNG", side);

            chessPieces[idx++] = new ChessPiece(row, col++, "bishop", "bishop" + color + ".PNG", side);

            chessPieces[idx++] = new ChessPiece(row, col++, "horse", "horseright" + color + ".PNG", side);

            chessPieces[idx++] = new ChessPiece(row, col, "rook", "rook" + color + ".PNG", side);

            col = 1;

            if (side.equals("player")) {row = 2;}
            else {row = 7;}

            for (int k = 0; k < 8; k++) {
                chessPieces[idx++] = new ChessPiece(row, col, "pawn", "pawn" + color  + ".PNG", side);
                col++;
            }

            side = "opponent";
            color = "black";

        }

        // Assign chess pieces to their respective squares
        for (ChessPiece piece: chessPieces) {
            for (ChessSquare sq : chessSquares) {
                if (piece.row == sq.row && piece.col == sq.col) {
                    sq.updateOccupant(piece);
                }
            }
        }
    }

    private void getClicked(int x, int y) {
        for (ChessSquare sq : chessSquares) {
            // If click coordinates correspond to a square
            if (sq.posx <= x && x <= sq.posx + SQUAREWIDTH
                && sq.posy <= y && y <= sq.posy + SQUAREWIDTH){
                System.out.println("Selected square at " + sq.row + " " + sq.col);
                sq.selected = true;

                if (firstClicked == null) {
                    firstClicked = sq;

                } else if (secondClicked == null) {
                    secondClicked = sq;

                } else if (firstClicked.equals(secondClicked) && secondClicked.equals(sq)){ // deselect squares
                    firstClicked.selected = false;
                    secondClicked.selected = false;
                    firstClicked = null;
                    secondClicked = null;
                } else {
                    firstClicked.selected = false;
                    firstClicked = secondClicked;
                    secondClicked = sq;
                }
                canvas.repaint();
                break;
            }
        }
    }

    private class MoveAction extends AbstractAction {

        /**
         * Action to take when Enter key is pressed.
         */

        private final int DONOTHING = 0;
        private final int MOVETOEMPTY = 1;
        private final int TAKEPIECE = 2;
        private final int DOCASTLE = 3;         // Castle move where King swaps with Rook
        private final int ENPASSANT = 4;        // Special En passant move with pawns

        private int MOVEACTION;


        private MoveAction() {
//          do nothing
        }

        private void checkSquares() {           // Check selection status of squares

            if (firstClicked == null || secondClicked == null || firstClicked.occupant == null) {
                this.MOVEACTION = DONOTHING;
                System.out.println("Checking two squares: false");
                return;
            }

            System.out.println("\nFirst clicked is square at row " + firstClicked.row + " col " + firstClicked.col
                    + "\nFirst clicked occupant is at row " + firstClicked.occupant.row + " col " + firstClicked.occupant.col
                    + ", and is a " + firstClicked.occupant.name);
            System.out.println("\nSecond clicked is square at row " + secondClicked.row + " col " + secondClicked.col);
            if (secondClicked.occupant != null) {
                    System.out.println("\nSecond clicked occupant is at row " + secondClicked.occupant.row + " col " + secondClicked.occupant.col
                    + ", and is a " + secondClicked.occupant.name);
            }

            if (secondClicked.occupant == null) {

                this.MOVEACTION = MOVETOEMPTY;

            } else {

                switch (secondClicked.occupant.side) {

                    case "opponent": this.MOVEACTION = TAKEPIECE; break;

                    case "player": this.MOVEACTION = DONOTHING;

                        if (firstClicked.occupant.name.equals("king") && secondClicked.occupant.name.equals("rook")) {
                            this.MOVEACTION = DOCASTLE;
                        }

                        break;

                    default: this.MOVEACTION = DONOTHING;
                }
            }
        }

        private void moveToEmpty(ChessSquare sq1, ChessSquare sq2) {

            System.out.println("Moving " + sq1.occupant.name + " from " + sq1.toString() + " to " + sq2.toString());
            secondClicked.updateOccupant(firstClicked.occupant);
            firstClicked.removeOccupant();
            secondClicked.occupant.updatePosition(secondClicked.row, secondClicked.col);

            canvas.repaint();
            clearSelected();        // This must be called AFTER canvas.repaint
        }

        private void takePiece(ChessSquare sq1, ChessSquare sq2) throws AssertionError{
            try {
                assert (!(sq1.occupant.side.equals(sq2.occupant.side)));
            } catch (AssertionError e) {
                System.out.println("Couldn't take piece");
                return;
            }

            System.out.println("Taking " + sq2.occupant.side + " "+ sq2.occupant.name + " with "
            + sq1.occupant.side + " " + sq1.occupant.name);

            takenPieces.add(sq2.occupant);
            sq2.occupant.removeFromBoard();
            sq2.removeOccupant();

            sq2.updateOccupant(sq1.occupant);
            sq1.removeOccupant();
            sq2.occupant.updatePosition(sq2.row, sq2.col);

            canvas.repaint();
            clearSelected();        // This must be called AFTER canvas.repaint

        }

        private void doCastle(ChessSquare sq1, ChessSquare sq2) {
            try {
                assert (sq1.occupant.name.equals("king") && (sq2.occupant.name.equals("rook")));
            } catch (AssertionError ignored) {}

            // Check that squares in between king and rook are empty
        }

        private void clearSelected() {

            if (firstClicked != null) firstClicked.selected = false;
            if (secondClicked != null) secondClicked.selected = false;
            firstClicked = null;
            secondClicked = null;
            canvas.repaint();
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            checkSquares();

            switch (this.MOVEACTION) {

                case DONOTHING: break;

                case MOVETOEMPTY: moveToEmpty(firstClicked, secondClicked); break;

                case TAKEPIECE: takePiece(firstClicked, secondClicked); break;

                case DOCASTLE: // castle() ; break;

                default: break;
            }

            clearSelected();
        }
    }

    private Game() {

        constructBoard();

        window = getContentPane();

        canvas = new DrawPane();

        window.add(canvas);

        canvas.setSize(WIDTH, HEIGHT);

        canvas.setBackground(Color.black);

        setContentPane(canvas);

        setSize(WIDTH, HEIGHT);

        canvas.addMouseListener(new MouseAdapter() {  //provides empty implementation of all of MouseListener`s methods

            @Override
            public void mousePressed(MouseEvent e) {
                getClicked(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
//                System.out.println("Released: " + e.getX() + "," + e.getY());
            }

        });

        KeyStroke enterKey = KeyStroke.getKeyStroke("ENTER");

        canvas.getInputMap(IFW).put(enterKey, "Enter");

        canvas.getActionMap().put("Enter", new MoveAction());

        setVisible(true);

    }

/* TODO: provide parameters to start a custom configuration of game, EG the N queens problem, etc */

    synchronized private void start() {
        if (gameThread == null || ! gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }
        else
            notify();
    }

    public void run() {
        // run method for thread repeatedly calls repaint on canvas and adds short delay to iterations
        canvas.repaint();
        try { Thread.sleep(sleepTime); } // wait a bit before starting
        catch (InterruptedException ignored) {}
    }

    public static void main(String[] args) {
        System.out.println("Starting chess game");
        Game chess = new Game();
        chess.start();
        chess.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

