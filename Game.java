import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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


public class Game extends JFrame implements Runnable{

    private final int WIDTH = 700;
    private final int HEIGHT = 650;

    private final int SQUAREWIDTH = 60;

    private final int ROWS = 8;
    private final int COLS = 8;

    private final int BOARDWIDTH = SQUAREWIDTH * ROWS;
    private final int BOARDHEIGHT = SQUAREWIDTH * COLS;

    private final int BOARDBUFFER = 20;

    private final int LEFTBUFFER = (WIDTH / 2) - (BOARDWIDTH / 2);      // Change WIDTH to window.width?
    private final int TOPBUFFER = (HEIGHT / 2) - (BOARDHEIGHT / 2);

    // ADD BOTTOMBUFFER and RIGHTBUFFER TOO SO YOU CAN RESCALE WINDOW

    private int numb_start_pieces = 32;

    private int sleepTime = 100;

    private boolean boardexists = true;

    private String game_title = "Chess";

    private Color background_color = new Color(65, 65, 45);
    private Color black_square = new Color(0x312F14);
    private Color white_square = new Color(0xF6FAC9);

    private Thread gameThread;

    private DrawPane canvas; // JPanel where maze is drawn

    private ChessSquare[] chessSquares = new ChessSquare[ROWS*COLS];
    private ChessPiece[] chessPieces = new ChessPiece[numb_start_pieces];


    private class DrawPane extends JPanel {
        /**
         * This method is called implicitly.  It simply draws chess squares as
         * a collection of squares that are black or white.
         */


        public void paintComponent(Graphics g) {

            System.out.println("Painting canvas");
            g.setColor(background_color);
//            g.fillRect(0, 0, -1, -1);//
            g.fillRect(LEFTBUFFER - BOARDBUFFER, TOPBUFFER - BOARDBUFFER,
                    BOARDWIDTH + (2 * BOARDBUFFER), BOARDHEIGHT + (2 * BOARDBUFFER));//// width,height);

//            if (boardexists) {

            // Draw the chess squares
            int w = SQUAREWIDTH;
            int h = SQUAREWIDTH;
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

                    w_position = (j * w) + LEFTBUFFER;
                    h_position = (i * h) + TOPBUFFER;

                    g.fillRect(w_position, h_position, w, h);
                }
            }

            System.out.println((Arrays.stream(chessPieces).noneMatch(Objects::isNull)));
            System.out.println(Arrays.toString(chessPieces));

            // Don't always repaint all the images???
            if (Arrays.stream(chessPieces).noneMatch(Objects::isNull)) {
                System.out.println("chessPieces length > 0: " + Arrays.toString(chessPieces));
                for (ChessPiece piece: chessPieces) {
                    g.drawImage(piece.image, piece.posx, piece.posy, null);
                }
            }

        }
    }

    private class ChessPiece {

        private int row;
        private int col;
        private int posx;
        private int posy;
        private String name;
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

        private ChessPiece(int r, int c, String n, String imagepath) {
            name = n;
            updatePosition(r, c);
            loadImage(imagepath);
        }
    }

    private class ChessSquare {

        private int row;
        private int col;

        private String name;
        private ChessPiece occupant;

        private ChessSquare(int r, int c, String s) {
            this.row = r;
            this.col = c;
            this.name = s;
        }

        private void updateOccupant(ChessPiece piece) {
            this.occupant = piece;
        }

        private void removeOccupant() {
            this.occupant = null;
        }
    }

    private void constructBoard() {

        int row = 1;
        int col = 1;

        String[] rownames = new String[] {"a","b","c","d","e","f","g","h"};

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

            chessPieces[idx++] = new ChessPiece(row, col++, "rook", "rook" + color + ".PNG");

            chessPieces[idx++] = new ChessPiece(row, col++, "horse", "horseleft" + color + ".PNG");

            chessPieces[idx++] = new ChessPiece(row, col++, "bishop", "bishop" + color + ".PNG");

            chessPieces[idx++] = new ChessPiece(row, col++, "queen", "queen" + color + ".PNG");

            chessPieces[idx++] = new ChessPiece(row, col++, "king", "king" + color + ".PNG");

            chessPieces[idx++] = new ChessPiece(row, col++, "bishop", "bishop" + color + ".PNG");

            chessPieces[idx++] = new ChessPiece(row, col++, "horse", "horseright" + color + ".PNG");

            chessPieces[idx++] = new ChessPiece(row, col, "rook", "rook" + color + ".PNG");

            col = 1;

            if (side.equals("player")) {row = 2;}
            else {row = 7;}

            for (int k = 0; k < 8; k++) {
                chessPieces[idx++] = new ChessPiece(row, col, "pawn", "pawn" + color  + ".PNG");
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

    public Game() {

        constructBoard();

        canvas = new DrawPane();

        int width = ROWS * SQUAREWIDTH + 10;
        int height = COLS * SQUAREWIDTH + 10;

        canvas.setSize(WIDTH, HEIGHT);

        canvas.setBackground(Color.black);

        setContentPane(canvas);

        setSize(WIDTH, HEIGHT);
        setVisible(true);

        canvas.addMouseListener(new MouseAdapter() {  //provides empty implementation of all of MouseListener`s methods

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Press: " + e.getX() + "," + e.getY());

                super.mousePressed(e);
            }


            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("Released: " + e.getX() + "," + e.getY());

                super.mouseReleased(e);
            }

        });


    }

//    public void start() {       // provide parameters to start a custom configuration of game, EG 5 queens or something!

    synchronized public void start() {
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

