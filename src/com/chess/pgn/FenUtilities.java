package com.chess.pgn;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.pieces.*;

import static com.chess.engine.board.Board.*;

public class FenUtilities {

    private FenUtilities() {
        throw new RuntimeException("Not instantiable!");
    }

    public static Board createGameFromFEN(final String fenString) {
        return parseFEN(fenString);
    }

    public static String createFENFromGame(final Board board) {
        return calculateBoardText(board) + "" +
               calculateCurrentPlayerText(board) + "" +
               calculateCastleText(board) + "" +
               calculateEnPassantSquare(board) + "" +
                "0 1";
    }

    private static Board parseFEN(String fenString) {
        final String[] fenParts = fenString.trim().split(" ");
        final String boardString = fenParts[0];

        final Builder builder = new Builder();
        int rank = 7;
        int file = 0;

        for (char c : boardString.toCharArray()) {
            if (c == '/') {
                rank--;
                file = 0;
            } else if (Character.isDigit(c)) {
                file += Character.getNumericValue(c);
            } else {
                chessPiece piece = getPieceFromChar(c, file, rank, fenParts[2]);
                builder.setPiece(piece);
                file++;
            }
        }

        builder.setMoveMaker(moveMaker(fenParts[1]));

        return builder.build();
    }

    private static chessPiece getPieceFromChar(char c, int file, int rank, String castlingString) {
        Alliance alliance = Character.isUpperCase(c) ? Alliance.WHITE : Alliance.BLACK;
        c = Character.toLowerCase(c);

        switch (c) {
            case 'p':
                return new Pawn(alliance, rank * 8 + file);
            case 'n':
                return new Knight(alliance, rank * 8 + file);
            case 'b':
                return new Bishop(alliance, rank * 8 + file);
            case 'r':
                return new Rook(alliance, rank * 8 + file);
            case 'q':
                return new Queen(alliance, rank * 8 + file);
            case 'k':
                boolean kingSideCastle = (castlingString.contains(alliance.getChar() + "k"));
                boolean queenSideCastle = (castlingString.contains(alliance.getChar() + "q"));
                return new King(alliance, rank * 8 + file, kingSideCastle, queenSideCastle);
            default:
                throw new RuntimeException("Invalid piece character: " + c);
        }
    }

    private static Alliance moveMaker(String fenPart) {
        if (fenPart.equals("w")) {
            return Alliance.WHITE;
        } else if (fenPart.equals("b")) {
            return Alliance.BLACK;
        } else {
            throw new RuntimeException("Invalid move maker: " + fenPart);
        }
    }


    private static String calculateBoardText(Board board) {
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < BoardUtils.TOTAL_SQUARES; i++) {
            final String squareText = board.getSquare(i).toString();
            builder.append(squareText);
        }
        builder.insert(0, "/");
        builder.insert(17, "/");
        builder.insert(26, "/");
        builder.insert(35, "/");
        builder.insert(44, "/");
        builder.insert(53, "/");
        builder.insert(62, "/");

        return builder.toString().replaceAll("--------", "8")
                                 .replaceAll("-------", "7")
                                 .replaceAll("------", "6")
                                 .replaceAll("-----", "5")
                                 .replaceAll("----", "4")
                                 .replaceAll("---", "3")
                                 .replaceAll("--", "2")
                                 .replaceAll("-", "1");
    }

    private static String calculateEnPassantSquare(final Board board) {
        
        final Pawn enPassantPawn = board.getEnPassantPawn();
        if(enPassantPawn != null) {
            return BoardUtils.getPositionAtCoordinate(enPassantPawn.getPiecePosition() +
                    (8) * enPassantPawn.getPieceAlliance().getOppositeDirection());
        }
        return "-";
    }

    private static String calculateCastleText(final Board board) {
        final StringBuilder builder = new StringBuilder();

        if(board.whitePlayer().isKingSideCastleCapable()) {
            builder.append("K");
        }
        if(board.whitePlayer().isQueenSideCastleCapable()) {
            builder.append("Q");
        }
        if(board.blackPlayer().isKingSideCastleCapable()) {
            builder.append("k");
        }
        if(board.blackPlayer().isQueenSideCastleCapable()) {
            builder.append("q");
        }

        final String result = builder.toString();

        return result.isEmpty() ? "-" : result;
    }

    private static String calculateCurrentPlayerText(final Board board) {
        return board.currentPlayer().toString().substring(0, 1).toLowerCase();
    }

}
