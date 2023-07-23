package com.chess.gui;

import com.chess.engine.board.Move;
import com.chess.engine.pieces.chessPiece;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.chess.gui.Table.*;

public class TakenPiecesPanel extends JPanel {

    private final JPanel northPanel;
    private static final Dimension TAKEN_PIECES_DIMENSION = new Dimension(40, 80);
    private final JPanel southPanel;

    private static final Color PANEL_COLOR = Color.decode("0xFDFE6");
    private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);

    public TakenPiecesPanel() {
        super(new BorderLayout());
        this.setBackground(PANEL_COLOR);
        this.setBorder(PANEL_BORDER);
        this.northPanel = new JPanel(new GridLayout(8, 2));
        this.southPanel = new JPanel(new GridLayout(8, 2));
        this.northPanel.setBackground(PANEL_COLOR);
        this.southPanel.setBackground(PANEL_COLOR);
        this.add(this.northPanel, BorderLayout.NORTH);
        this.add(this.southPanel, BorderLayout.SOUTH);
        setPreferredSize(TAKEN_PIECES_DIMENSION);
    }

    public void redo(final MoveLog moveLog) {

        this.southPanel.removeAll();
        this.northPanel.removeAll();

        final List<chessPiece> whiteTakenPieces = new ArrayList<>();
        final List<chessPiece> blackTakenPieces = new ArrayList<>();

        for(final Move move : moveLog.getMoves()) {
            if(move.isAttack()) {
                final chessPiece takenPiece = move.getAttackedPiece();
                if(takenPiece.getPieceAlliance().isWhite()) {
                    whiteTakenPieces.add(takenPiece);
                }else if(takenPiece.getPieceAlliance().isBlack()) {
                    blackTakenPieces.add(takenPiece);
                }else {
                    throw new RuntimeException("should not reach here!");
                }
            }
        }

        Collections.sort(whiteTakenPieces, new Comparator<chessPiece>() {
            @Override
            public int compare(chessPiece p1, chessPiece p2) {
                return Integer.compare(p1.getPieceValue(), p2.getPieceValue());
            }
        });

        Collections.sort(blackTakenPieces, new Comparator<chessPiece>() {
            @Override
            public int compare(chessPiece p1, chessPiece p2) {
                return Integer.compare(p1.getPieceValue(), p2.getPieceValue());
            }
        });

        for(final chessPiece takenPiece : whiteTakenPieces) {
            try{
                final BufferedImage image = ImageIO.read(new File("art/pieces/plain" + takenPiece.getPieceAlliance().toString().substring(0,1) + "" + takenPiece.toString()));
                final ImageIcon icon = new ImageIcon(image);
                final JLabel imageLabel = new JLabel();
                this.southPanel.add(imageLabel);
            }catch(final IOException e) {
                e.printStackTrace();
            }
        }

        for(final chessPiece takenPiece : blackTakenPieces) {
            try {
                final BufferedImage image = ImageIO.read(new File("art/pieces/plain" + takenPiece.getPieceAlliance().toString().substring(0, 1) + "" + takenPiece.toString()));
                final ImageIcon icon = new ImageIcon(image);
                final JLabel imageLabel = new JLabel();
                this.southPanel.add(imageLabel);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        validate();
    }
}
