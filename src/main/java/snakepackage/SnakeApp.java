package snakepackage;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.*;

import enums.GridSize;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jd-
 *
 */
public class SnakeApp {

    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] spawn = {
        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2,
        3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
        new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2,
        GridSize.GRID_HEIGHT - 2)};
    private JFrame frame;
    private static Board board;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];
    private CountDownLatch latch = new CountDownLatch(MAX_THREADS);
    private JLabel longestLabel;
    private JLabel worstLabel;

    public SnakeApp() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(618, 640);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);
        board = new Board();


        frame.add(board,BorderLayout.CENTER);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new FlowLayout());
        longestLabel = new JLabel("Longest snake: N/A");
        worstLabel = new JLabel("Worst snake: N/A");
        statsPanel.add(longestLabel);
        statsPanel.add(worstLabel);
        frame.add(statsPanel, BorderLayout.NORTH);


        startButton = new JButton("Iniciar");
        pauseButton = new JButton("Pausar");
        resumeButton = new JButton("Reanudar");

        // disable buttons
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(false);

        JPanel actionsBPabel=new JPanel();
        actionsBPabel.setLayout(new FlowLayout());
        actionsBPabel.add(startButton);
        actionsBPabel.add(pauseButton);
        actionsBPabel.add(resumeButton);
        frame.add(actionsBPabel,BorderLayout.SOUTH);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start();
                startButton.setEnabled(false);
                pauseButton.setEnabled(true);
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseGame();
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(true);
            }
        });

        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resumeGame();
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(true);
            }
        });

    }

    private void pauseGame() {
        for (Snake snake : snakes) {
            snake.pause();
        }
        showSnakeStats(longestLabel, worstLabel);
    }

    private void resumeGame() {
        for (Snake snake : snakes) {
            snake.resume();
        }
    }

    /**
     * Show the longest snake and the worst snake
     */
    private void showSnakeStats(JLabel longestLabel, JLabel worstLabel) {
        Snake longestSnake = null;
        Snake worstSnake = null;

        for (Snake snake : snakes) {
            if (!snake.isSnakeEnd()) {
                if (longestSnake == null || snake.getBody().size() > longestSnake.getBody().size()) {
                    longestSnake = snake;
                }
            } else {
                if (worstSnake == null || snake.getDeathTime() < worstSnake.getDeathTime()) {
                    worstSnake = snake;
                }
            }
        }

        if (longestSnake != null) {
            longestLabel.setText("Longest snake: " + "snake " + longestSnake.getIdt() + " (Length: " + longestSnake.getBody().size() + ")");
        } else {
            longestLabel.setText("Longest snake: N/A");
        }

        if (worstSnake != null) {
            worstLabel.setText("Worst snake: " + "snake " + worstSnake.getIdt() + " (Death Time: " + (worstSnake.getDeathTime() / 1000) + " ms)");
        } else {
            worstLabel.setText("Worst snake: N/A");
        }
    }

    public static void main(String[] args) {
        app = new SnakeApp();
        app.init();
    }

    private void init() {
        
        for (int i = 0; i != MAX_THREADS; i++) {
            
            snakes[i] = new Snake(i + 1, spawn[i], i + 1, latch);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
        }

        frame.setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await(); 
                    System.out.println("Todas las serpientes han terminado.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void start(){
        for (int i = 0; i != MAX_THREADS; i++) {
            thread[i].start();
            System.out.println("Serpiente " + (i + 1) + " iniciada.");
        }
    }

    public static SnakeApp getApp() {
        return app;
    }

}
