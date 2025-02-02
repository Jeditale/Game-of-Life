import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GameOfLifeGUI extends JPanel implements KeyListener, MouseListener {
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static final int CELL_SIZE = 20;

    private static final int GRID_COLS = SCREEN_WIDTH / CELL_SIZE;
    private static final int GRID_ROWS = SCREEN_HEIGHT / CELL_SIZE;

    private boolean[][] grid = new boolean[GRID_ROWS][GRID_COLS];
    private boolean running = false;
    private JFrame frame;
    private boolean isFullscreen = false;
    private Rectangle windowedBounds;
    private int ticks = 0; // Number of generations the civilization stays alive

    public GameOfLifeGUI(JFrame frame) {
        this.frame = frame;
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.addMouseListener(this);
        this.setFocusable(true);
        this.addKeyListener(this);
        initializeGrid();
    }

    private void initializeGrid() {
        Random random = new Random();
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                grid[i][j] = random.nextDouble() < 0.3; // 30% chance of being alive
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                // Alive cells are green, dead cells are black
                g.setColor(grid[i][j] ? Color.GREEN : Color.BLACK);
                g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // If game is over, display message
        if (isGameOver()) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", SCREEN_WIDTH / 3, SCREEN_HEIGHT / 2 - 50);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.drawString("Ticks survived: " + ticks, SCREEN_WIDTH / 3, SCREEN_HEIGHT / 2 + 20);
        }
    }

    private int countNeighbors(int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int nx = x + i, ny = y + j;
                if (nx >= 0 && nx < GRID_ROWS && ny >= 0 && ny < GRID_COLS) {
                    count += grid[nx][ny] ? 1 : 0;
                }
            }
        }
        return count;
    }

    private void nextGeneration() {
        boolean[][] newGrid = new boolean[GRID_ROWS][GRID_COLS];
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                int neighbors = countNeighbors(i, j);
                if (grid[i][j]) {
                    newGrid[i][j] = neighbors == 2 || neighbors == 3;
                } else {
                    newGrid[i][j] = neighbors == 3;
                }
            }
        }
        grid = newGrid;
        ticks++; // Increment the ticks
        repaint();
    }

    private boolean isGameOver() {
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                if (grid[i][j]) {
                    return false; // At least one cell is alive, so not game over
                }
            }
        }
        return true; // All cells are dead
    }

    private void runSimulation() {
        new Thread(() -> {
            running = true;
            while (running) {
                if (isGameOver()) {
                    stopSimulation(); // Stop the game if all cells are dead
                    break;
                }
                nextGeneration();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void stopSimulation() {
        running = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Allow clicking to toggle cells between alive and dead during the simulation
        if (e.getButton() == MouseEvent.BUTTON1 && !isGameOver()) { // Left-click to toggle cells
            int x = e.getY() / CELL_SIZE;
            int y = e.getX() / CELL_SIZE;
            if (x < GRID_ROWS && y < GRID_COLS) {
                grid[x][y] = !grid[x][y]; // Toggle the cell's state
                repaint();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!running) {
                runSimulation();
            } else {
                stopSimulation();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_F11) {
            toggleFullscreen();
        }
    }

    private void toggleFullscreen() {
        if (!isFullscreen) {
            windowedBounds = frame.getBounds();
            frame.dispose();
            frame.setUndecorated(true);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        } else {
            frame.dispose();
            frame.setUndecorated(false);
            frame.setBounds(windowedBounds);
            frame.setVisible(true);
        }
        isFullscreen = !isFullscreen;
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Conway's Game of Life");
        GameOfLifeGUI game = new GameOfLifeGUI(frame);

        frame.add(game);
        frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        game.requestFocusInWindow();
    }
}
