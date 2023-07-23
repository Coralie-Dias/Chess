package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Move;
import com.chess.engine.board.Board;

import java.util.Collection;

/**
 * Created by Coralie Dias
 * 19052736
 */

public abstract class chessPiece {

    protected final PieceType pieceType;
    protected final int piecePosition;
    protected final Alliance pieceAlliance;
    protected final boolean isFirstMove;
    private final int cacheHashCode;

    chessPiece(final PieceType pieceType,
               final int piecePosition,
               final Alliance pieceAlliance,
               final boolean isFirstMove) {
        this.pieceType = pieceType;
        this.piecePosition = piecePosition;
        this.pieceAlliance = pieceAlliance;
        this.isFirstMove = isFirstMove;
        this.cacheHashCode = computeHashCode();
    }

    private int computeHashCode() {
        int result = pieceType.hashCode();
        result = 31 * result + pieceAlliance.hashCode();
        result = 31 * result + piecePosition;
        result = 31 * result + (isFirstMove ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if(this == other) {
            return true;
        }
        if(!(other instanceof chessPiece)){
            return false;
        }
        final chessPiece otherPiece = (chessPiece) other;
        return piecePosition == otherPiece.getPiecePosition() &&
               pieceType == otherPiece.getPieceType() &&
               pieceAlliance == otherPiece.getPieceAlliance() && isFirstMove == otherPiece.isFirstMove();
    }

    @Override
    public int hashCode() {
        return this.cacheHashCode;
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }

    public int getPieceValue() {
        return this.pieceType.getPieceValue();
    }

    public int getPiecePosition() {
        return this.piecePosition;
    }
    public Alliance getPieceAlliance() {
        return this.pieceAlliance;
    }

    public boolean isFirstMove() {
        return this.isFirstMove;
    }
    public abstract Collection<Move> calculateLegalMoves(final Board board);

    public abstract chessPiece movePiece(Move move);

    public enum PieceType {

        KNIGHT(300, "N"),
        BISHOP(300, "B"),
        ROOK(500, "R"),
        QUEEN(900, "Q"),
        KING(10000, "K"),
        PAWN(100, "P");

        private String pieceName;
        private int pieceValue;

        PieceType(final int pieceValue,
                  final String pieceName) {
            this.pieceName = pieceName;
            this.pieceValue = pieceValue;
        }

        @Override
        public String toString() {
            return this.pieceName;
        }

        public int getPieceValue() {
            return this.pieceValue;
        }

    }

}
