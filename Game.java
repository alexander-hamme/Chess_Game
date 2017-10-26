import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Remember Rows go left to right, but from TOP to BOTTOM or vice versa???  */


public class Game extends JFrame implements Runnable {

    private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

    private final int WIDTH = 700;
    private final int HEIGHT = 650;

    private final int SQUAREWIDTH = 60;

    private final int ROWS = 8;
    private final int COLS = 8;

    private final int BOARDWIDTH = SQUAREWIDTH * ROWS;
    private final int BOARDHEIGHT = SQUAREWIDTH * COLS;

    private final int BOARDBUFFER = 20;

    private int LEFTBUFFER = (WIDTH / 2) - (BOARDWIDTH / 2);      // Change WIDTH to window.width so it dynamically resizes?
    private int TOPBUFFER = (HEIGHT / 2) - (BOARDHEIGHT / 2);


    private int numb_start_pieces = 32;

    private int sleepTime = 100;

    private boolean boardexists = true;

    private String game_title = "Chess";

    private String[] rownames = new String[] {"a","b","c","d","e","f","g","h"};


    private Color background_color = new Color(65, 65, 45);
    private Color selection_color = new Color(0, 241, 247);
    private Color black_square = new Color(0x312F14);
    private Color white_square = new Color(0xF6FAC9);

    private Thread gameThread;

    private DrawPane canvas; // JPanel where maze is drawn
    private Container window;

    private ChessSquare[] chessSquares = new ChessSquare[ROWS*COLS];
    private ChessPiece[] chessPieces = new ChessPiece[numb_start_pieces];


    protected ChessSquare firstClicked;
    protected ChessSquare secondClicked;


    private class DrawPane extends JPanel {
        /**
         * This method is called implicitly.  It simply draws chess squares as
         * a collection of squares that are black or white.
         */

        protected void paintComponent(Graphics g) {

//            System.out.println("painted canvas");

            LEFTBUFFER = (canvas.getSize().width / 2) - (BOARDWIDTH / 2);
            TOPBUFFER = (canvas.getSize().height / 2) - (BOARDWIDTH / 2);

            g.setColor(background_color);
//            g.fillRect(0, 0, -1, -1);//
            g.fillRect(LEFTBUFFER - BOARDBUFFER, TOPBUFFER - BOARDBUFFER,
                    BOARDWIDTH + (2 * BOARDBUFFER), BOARDHEIGHT + (2 * BOARDBUFFER));//// width,height);

//            if (boardexists) {

            // Draw the chess squares

            int w_position, h_position;
            for (int j = 0; j < COLS; j++) {
                for (int i = 0; i < ROWS; i++) {
                    if (i % 2 == 0) {
                        if (j % 2 == 0) {
                            g.setColor(white_square);
                        } else {
                            g.setColor(black_square);
                        }

                    } else {
                        if (j % 2 == 0) {
                            g.setColor(black_square);
                        } else {
                            g.setColor(white_square);
                        }
                    }

                    w_position = (j * SQUAREWIDTH) + LEFTBUFFER;
                    h_position = (i * SQUAREWIDTH) + TOPBUFFER;

                    g.fillRect(w_position, h_position, SQUAREWIDTH, SQUAREWIDTH);
                }
            }

            if (Arrays.stream(chessPieces).noneMatch(Objects::isNull)) {
                for (ChessPiece piece: chessPieces) {
                    piece.updatePosition(piece.row, piece.col);
                    g.drawImage(piece.image, piece.posx, piece.posy, null);
                }
            }

            if (Arrays.stream(chessSquares).noneMatch(Objects::isNull)) {
                for (ChessSquare sq: chessSquares) {
                    sq.updateWindowPosition();
                    if (sq.selected){
                        int[] xpoints = new int[] {sq.posx, sq.posx, sq.posx + SQUAREWIDTH, sq.posx + SQUAREWIDTH, sq.posx};
                        int[] ypoints = new int[] {sq.posy, sq.posy + SQUAREWIDTH, sq.posy + SQUAREWIDTH, sq.posy, sq.posy};
//                        g.drawPolygon();
                        g.setColor(selection_color);
                        g.drawPolyline(xpoints, ypoints, 5);
                    }

                    if (sq.occupant != null) {
//                        g.setColor(new Color(100,50,0));
//                        g.fillRect(sq.posx+10, sq.posy+10, SQUAREWIDTH-20, SQUAREWIDTH-20);
                        g.setColor(Color.CYAN);
                        g.drawString(sq.row + ", " + sq.col, sq.posx, sq.posy);
                    }

                }
            }

            g.setColor(Color.BLACK);

            for (int i = 0; i < ROWS; i++) {
                g.drawString(rownames[i], LEFTBUFFER + (int)(SQUAREWIDTH * (i + 0.5)), TOPBUFFER + (int)(SQUAREWIDTH * (COLS + 0.6)));
                g.drawString(String.valueOf(ROWS - i), LEFTBUFFER - (SQUAREWIDTH / 2), TOPBUFFER + (SQUAREWIDTH * i) + 3 * SQUAREWIDTH / 4);
            }

//            if (firstClicked != null) {
//
//            }
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
            String toStrng = "square at row " + this.row + ", col " + this.col;

            toStrng = (this.occupant == null) ? "empty " + toStrng : toStrng + ", with (" + this.occupant.name + ")";

            return toStrng;
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

        private void clicked() {
            if (this.occupant == null) {

            }
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
            this.posy = TOPBUFFER + SQUAREWIDTH * (this.row - 1) + SQUAREWIDTH / 8;
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

            System.out.println(rownames[col-1] + col);

            chessSquares[i] = new ChessSquare(row, col, rownames[col-1] + col);

            if (col < 8) col ++;

            else if (col == 8) {
                col = 1;
                row ++;
            }
        }

        // IF USER IS PLAYING WHITE:   ELSE: ........


        int idx = 0;
        String side = "player";
        String color = "black";

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
            color = "white";

        }

        for (ChessPiece piece: chessPieces) {
            for (ChessSquare sq : chessSquares) {
                if (piece.row == sq.row && piece.col == sq.col) {
                    sq.updateOccupant(piece);
                }
            }
        }
    }

    private void getKeyPressed(KeyEvent key) {
        System.out.println(key.getKeyChar() + "\n" + key.getKeyCode() + "\n" + key.getExtendedKeyCode()
        );
    }

    private void getClicked(int x, int y) {
        for (ChessSquare sq : chessSquares) {
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
//
//                    secondClicked.selected = false;
//                    secondClicked = firstClicked;
//                    firstClicked = sq;
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

        private final int DONOTHING = 0;
        private final int MOVETOEMPTY = 1;
        private final int TAKEPIECE = 2;
        private final int DOCASTLE = 3;

        private int MOVEACTION;

//      no default constructor

        private MoveAction() {
//           nothing
        }

        private void checkSquares() {

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

                if (secondClicked.occupant.side.equals("opponent")) {
                    this.MOVEACTION = TAKEPIECE;
                } else if (secondClicked.occupant.side.equals("player")){
                    if (firstClicked.occupant.name.equals("king") && secondClicked.occupant.name.equals("rook")) {
                        this.MOVEACTION = DOCASTLE;
                    } // else do nothing
                } else {
                    this.MOVEACTION = DONOTHING;
                }
            }
//            return (firstClicked != null && secondClicked != null);
        }

        private void moveToEmpty(ChessSquare sq1, ChessSquare sq2) {
            System.out.println("Moveaction is: " + this.MOVEACTION);
            System.out.println("Moving " + sq1.occupant.side + " piece from " + sq1.toString() + " to " + sq2.toString());
            secondClicked.updateOccupant(firstClicked.occupant);
            firstClicked.removeOccupant();
            secondClicked.occupant.updatePosition(secondClicked.row, secondClicked.col);

            firstClicked.selected = false;
            secondClicked.selected = false;
            firstClicked = null;
            secondClicked = null;

            canvas.repaint();
        }

        private void takePiece(ChessSquare sq1, ChessSquare sq2) {

        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            checkSquares();

            switch (this.MOVEACTION) {

                case DONOTHING: return;

                case MOVETOEMPTY: moveToEmpty(firstClicked, secondClicked);

                case TAKEPIECE: takePiece(firstClicked, secondClicked);

                case DOCASTLE: return;
            }

            canvas.repaint();
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

        setSize(WIDTH, HEIGHT);         //        window.setSize(WIDTH, HEIGHT);

        canvas.addMouseListener(new MouseAdapter() {  //provides empty implementation of all of MouseListener`s methods

            @Override
            public void mousePressed(MouseEvent e) {
//                System.out.println("Press: " + e.getX() + "," + e.getY());
                getClicked(e.getX(), e.getY());
//                super.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
//                System.out.println("Released: " + e.getX() + "," + e.getY());
            }

        });
        /*
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent k) {
                System.out.println("key pressed: " + k.getKeyCode());
            }

            @Override
            public void keyPressed(KeyEvent k) {
                getKeyPressed(k);
            }

            @Override
            public void keyReleased(KeyEvent k) {
                super.keyReleased(k);
            }
        });
            */

        KeyStroke enterKey = KeyStroke.getKeyStroke("ENTER");

        canvas.getInputMap(IFW).put(enterKey, "Enter");

        canvas.getActionMap().put("Enter", new MoveAction());
//        canvas.getActionMap().put("Click", new MoveAction("Click"));

        setVisible(true);

    }

//    public void start() {       // provide parameters to start a custom configuration of game, EG 5 queens or something!

    synchronized private void start() {
        if (gameThread == null || ! gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }
        else
            notify();
    }


    public void run() {
        // run method for thread repeatedly ...
        canvas.repaint();
        try { Thread.sleep(sleepTime); } // wait a bit before starting
        catch (InterruptedException e) {
            System.out.println("Placeholder");
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting chess game");
        Game chess = new Game();
        chess.start();
        chess.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

