package chess;

public class Rule {
/**
 * Rules :
 *
 * Movement
 *  - Pawn can move one or two squares from start, after that only one
 *  - Horse can move two squares + one square in four directions or one square + two squares in four directions
 *  - Bishop can move any direction diagonally on its color, up to as many squares as are open
 *  - Rook can move any direction horizontally, up to as many squares as are open
 *  - Queen can move any direction horizontally or diagonally, up to as many squares as are open
 *  - King can move one square in any direction, so long as that move does not result in Check
 *
 * Capture
 *  - Pawn can capture piece to the upper-left or upper-right diagonal square, or special En passant move
 *  - Horse can take any piece that it can land on
 *  - Bishop can take any piece on a square it can move to
 *  - Rook can take any piece on a square it can move to
 *  - Queen can take any piece on a square it can move to
 *  - King can take any piece that it can move to, so long as taking the piece does not result in Check
 *
 *
 *
 *  How to find available moves: for each piece, iterate through all chess squares, and assign better values to the ones that are better,
 *  and 0 or -1 to the ones that can't be moved to
 *
 */
    private Rule(Game.ChessPiece piece) {

        switch (piece.name) {

            case "pawn" :
        }

    }

    private Rule PawnRule() {

        return null;

    }



}
