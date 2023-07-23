package com.chess.gui;

import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Square;
import com.chess.engine.pieces.chessPiece;
import com.chess.engine.player.AI.MiniMax;
import com.chess.engine.player.AI.MoveStrategy;
import com.chess.engine.player.MoveTransition;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

public class Table extends Observable {

    private final JFrame gameFrame;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final BoardPanel boardPanel;
    private final MoveLog moveLog;
    private final GameSetup gameSetup;
    private Board chessBoard;
    private Square sourceSquare;
    private Square destinationSquare;
    private chessPiece humanMovedPiece;
    private BoardDirection boardDirection;
    private boolean highlightLegalMoves;

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
    private final static Dimension SQUARE_PANEL_DIMENSION = new Dimension(10, 10);
    private static String defaultPieceImagesPath = "art/pieces/plain/";

    private final Color lightSquareColor = Color.decode("#FFFACD");
    private final Color darkSquareColor = Color.decode("#593E1A");

    private static final Table INSTANCE = new Table();
    private Move computerMove;

    private Table() {
        this.gameFrame = new JFrame("JChess");
        this.gameFrame.setLayout(new BorderLayout());
        final JMenuBar tableMenuBar = createTableMenuBar();
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.chessBoard = Board.createStandardBoard();
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.boardPanel = new BoardPanel();
        this.moveLog = new MoveLog();
        this.addObserver(new TableGameAIWatcher());
        this.gameSetup = new GameSetup(this.gameFrame, true);
        this.boardDirection = BoardDirection.NORMAL;
        this.highlightLegalMoves = false;
        this.gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        this.gameFrame.setVisible(true);
    }

    public static Table get() {
        return INSTANCE;
    }

    public void show() {
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
    }

    private GameSetup getGameSetup() {
        return this.gameSetup;
    }

    private Board getGameBoard() {
        return this.chessBoard;
    }

    private JMenuBar createTableMenuBar() {
        JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        tableMenuBar.add(createOptionsMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");
        //PGN FILE FORMAT
        final JMenuItem openPGN = new JMenuItem("Load PGN File");
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Open up that pgn file!");
            }
        });
        fileMenu.add(openPGN);

        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);
        return fileMenu;
    }

    private JMenu createPreferencesMenu() {

        final JMenu preferencesMenu = new JMenu("Preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardDirection = boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });
        preferencesMenu.add(flipBoardMenuItem);
        preferencesMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighLighterCheckBox = new JCheckBoxMenuItem("Highlight Legal Moves", false);

        legalMoveHighLighterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightLegalMoves = legalMoveHighLighterCheckBox.isSelected();
            }
        });
        preferencesMenu.add(legalMoveHighLighterCheckBox);
        return preferencesMenu;
    }

    private JMenu createOptionsMenu() {

        final JMenu optionsMenu = new JMenu("Options");

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game");
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Table.get().getGameSetup().promptUser();
                Table.get().setupUpdate(Table.get().getGameSetup());
            }
        });

        optionsMenu.add(setupGameMenuItem);

        return optionsMenu;
    }

    private void setupUpdate(final GameSetup gameSetup) {
        setChanged();
        notifyObservers(gameSetup);
    }

    private static class TableGameAIWatcher implements Observer {

        @Override
        public void update(Observable o, Object arg) {

            if(Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) &&
                    !Table.get().getGameBoard().currentPlayer().isInCheckMate() &&
                    !Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
                //create an AI thread
                //execute AI work
                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            }

            if(Table.get().getGameBoard().currentPlayer().isInCheckMate()) {
                System.out.println("game over, " + Table.get().getGameBoard().currentPlayer() + "is in checkmate!");
            }

            if(Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
                System.out.println("game over, " + Table.get().getGameBoard().currentPlayer() + "is in stalemate!");
            }

        }
    }

    public void updateGameBoard(final Board board) {
        this.chessBoard = board;
    }

    public void updateComputerMove(final Move move) {
        this.computerMove = move;
    }

    private MoveLog getMoveLog() {
        return this.moveLog;
    }

    private GameHistoryPanel getGameHistoryPanel() {
        return this.gameHistoryPanel;
    }

    private TakenPiecesPanel getTakenPiecesPanel() {
        return this.takenPiecesPanel;
    }

    private BoardPanel getBoardPanel() {
        return this.boardPanel;
    }

    private void moveMadeUpdate(final PlayerType playerType) {
        setChanged();
        notifyObservers(playerType);
    }

    private static class AIThinkTank extends SwingWorker<Move, String> {

        private AIThinkTank() {
        }

        @Override
        protected Move doInBackground() throws Exception {

            final MoveStrategy miniMax = new MiniMax(4);

            final Move bestMove = miniMax.execute(Table.get().getGameBoard());

            return bestMove;
        }

        @Override
        public void done() {

            try {
                final Move bestMove = get();
                Table.get().updateComputerMove(bestMove);
                Table.get().updateGameBoard(Table.get().getGameBoard().currentPlayer().makeMove(bestMove).getTransitionBoard());
                Table.get().getMoveLog().addMove(bestMove);
                Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();            }
        }
    }

    public enum BoardDirection {
        NORMAL{
            @Override
            List<SquarePanel> traverse(final List<SquarePanel> boardSquares) {
                return boardSquares;
            }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<SquarePanel> traverse(final List<SquarePanel> boardSquares) {
                List<SquarePanel> reversedList = new ArrayList<>(boardSquares);
                Collections.reverse(reversedList);
                return reversedList;
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<SquarePanel> traverse(final List<SquarePanel> boardSquares);
        abstract BoardDirection opposite();

    }

    private class BoardPanel extends JPanel {
        final List<SquarePanel> boardSquares;

        BoardPanel() {
            super(new GridLayout(8,8));
            this.boardSquares = new ArrayList<>();
            for(int i = 0; i < BoardUtils.TOTAL_SQUARES; i++) {
                final SquarePanel squarePanel = new SquarePanel(this, i);
                this.boardSquares.add(squarePanel);
                add(squarePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }

        public void drawBoard(final Board board) {
            removeAll();
            for(final SquarePanel squarePanel : boardDirection.traverse(boardSquares)) {
                squarePanel.drawSquare(board);
                add(squarePanel);
            }
            validate();
            repaint();
        }

    }

    public static class MoveLog {

        private final List<Move> moves;
        MoveLog() {
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves() {
            return this.moves;
        }

        public void addMove(final Move move) {
            this.moves.add(move);
        }

        public int size() {
            return this.moves.size();
        }
        public void clear() {
            this.moves.clear();
        }

        public Move removeMove(int index) {
            return this.moves.remove(index);
        }

        public boolean removeMove(final Move move) {
            return this.moves.remove(move);
        }

    }

    enum PlayerType {
        HUMAN,
        COMPUTER
    }



    private class SquarePanel extends JPanel {

        private final int squareId;

        SquarePanel(final BoardPanel boardPanel,
                    final int squareId){
            super(new GridBagLayout());
            this.squareId = squareId;
            setPreferredSize(SQUARE_PANEL_DIMENSION);
            assignTileColor();
            assignSquarePieceIcon(chessBoard);

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if(isRightMouseButton(e)) {
                        sourceSquare = null;
                        destinationSquare = null;
                        humanMovedPiece= null;
                    }else if(isLeftMouseButton(e)) {
                        if(sourceSquare == null) {
                            sourceSquare = chessBoard.getSquare(squareId);
                            humanMovedPiece = sourceSquare.getPiece();
                            if(humanMovedPiece == null) {
                                sourceSquare = null;
                            }
                        } else {
                            destinationSquare = chessBoard.getSquare(squareId);
                            final Move move = Move.MoveFactory.createMove(chessBoard, sourceSquare.getSquareCoordinate(), destinationSquare.getSquareCoordinate());
                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if(transition.getMoveStatus().isDone()) {
                                chessBoard = transition.getTransitionBoard();
                                moveLog.addMove(move);
                            }
                            sourceSquare = null;
                            destinationSquare = null;
                            humanMovedPiece = null;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                gameHistoryPanel.redo(chessBoard, moveLog);
                                takenPiecesPanel.redo(moveLog);

                                if(gameSetup.isAIPlayer(chessBoard.currentPlayer())) {
                                    Table.get().moveMadeUpdate(PlayerType.HUMAN);
                                }
                                boardPanel.drawBoard(chessBoard);
                            }
                        });
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });
            validate();
        }

        public void drawSquare(final Board board) {
            assignTileColor();
            assignSquarePieceIcon(board);
            highLightLegals(board);
            validate();
            repaint();
        }

        private void assignSquarePieceIcon(final Board board) {
            this.removeAll();
            if(board.getSquare(this.squareId).isSquareActive()) {
                try {
                    final BufferedImage image =
                            ImageIO.read(new File(defaultPieceImagesPath + board.getSquare(this.squareId).getPiece().getPieceAlliance().toString().substring(0, 1) +
                            board.getSquare(this.squareId).getPiece().toString() + ".gif"));
                    add(new JLabel(new ImageIcon(image)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void highLightLegals(final Board board) {
            if(highlightLegalMoves) {
                for(final Move move : pieceLegalMoves(board)) {
                    if(move.getDestinationCoordinate() == this.squareId) {
                        try {
                            add(new JLabel(new ImageIcon(ImageIO.read(new File("art/misc/green_dot.png")))));
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private Collection<Move> pieceLegalMoves(final Board board) {
            if(humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance()) {
                return humanMovedPiece.calculateLegalMoves(board);
            }
            return Collections.emptyList();
        }

        private void assignTileColor() {
            if (BoardUtils.EIGHTH_RANK[this.squareId] ||
                BoardUtils.SIXTH_RANK[this.squareId] ||
                BoardUtils.FOURTH_RANK[this.squareId] ||
                BoardUtils.SECOND_RANK[this.squareId]) {
              setBackground(this.squareId % 2 == 0 ? lightSquareColor : darkSquareColor);
            } else if (BoardUtils.SEVENTH_RANK[this.squareId] ||
                BoardUtils.FIFTH_RANK[this.squareId] ||
                BoardUtils.THIRD_RANK[this.squareId] ||
                BoardUtils.FIRST_RANK[this.squareId]) {
                setBackground(this.squareId % 2 != 0 ? lightSquareColor : darkSquareColor);
            }
        }

    }

}

