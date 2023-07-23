package com.chess.engine.board;

import com.chess.engine.pieces.chessPiece;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Coralie Dias
 * 19052736
 */

//define class to represent chess tile
public abstract class Square {

    protected final int squareLocation;

    private static final Map<Integer, VacantSquare> VACANT_SQUARES_CACHE = createAllPossibleVacantSquares();

    private static Map<Integer,VacantSquare> createAllPossibleVacantSquares() {
        final Map<Integer, VacantSquare> vacantSquareMap = new HashMap<>();
        for (int i = 0; i < BoardUtils.TOTAL_SQUARES; i++){
            vacantSquareMap.put(i, new VacantSquare(i));
        }
        return Collections.unmodifiableMap(vacantSquareMap);
    }

    public static Square createSquare(final int squareLocation, final chessPiece piece) {
        return piece != null ? new ActiveSquare(squareLocation, piece) : VACANT_SQUARES_CACHE.get(squareLocation);
    }

    private Square (final int squareLocation){
        this.squareLocation = squareLocation;
    }

    //abstract method to define if a square is occupied by a piece
    public abstract boolean isSquareActive();

    //abstract method to acquire a chess piece
    public abstract chessPiece getPiece();

    public int getSquareCoordinate() {
        return this.squareLocation;
    }

    //subclass for an empty square
    public static final class VacantSquare extends Square {
        private VacantSquare(final int location){
            super(location);
        }

        @Override
        public String toString() {
            return "-";
        }

        @Override
        public boolean isSquareActive() {
            return false;
        }

        @Override
        public chessPiece getPiece() {
            return null;
        }
    }

    //subclass for a square occupied by a Chess Piece
    public static final class ActiveSquare extends Square{
        private final chessPiece pieceOnSquare;
        private ActiveSquare(int squareLocation, chessPiece pieceOnSquare) {
            super(squareLocation);
            this.pieceOnSquare= pieceOnSquare;
        }

        @Override
        public String toString() {
            return getPiece().getPieceAlliance().isBlack() ?
                   getPiece().toString().toLowerCase() : getPiece().toString();
        }

        @Override
        public boolean isSquareActive() {
            return true;
        }

        @Override
        public chessPiece getPiece(){
            return this.pieceOnSquare;
        }
    }
}
