import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_HEIGHT * SCREEN_WIDTH) / UNIT_SIZE;
    static final int DELAY = 60;
    ArrayList<Point> route;
    int[][] grid = new int[SCREEN_WIDTH / UNIT_SIZE][SCREEN_HEIGHT / UNIT_SIZE];
    boolean[][] wallVertical = new boolean[SCREEN_WIDTH / UNIT_SIZE + 1][SCREEN_HEIGHT / UNIT_SIZE];
    boolean[][] wallHorizontal = new boolean[SCREEN_WIDTH / UNIT_SIZE][SCREEN_HEIGHT / UNIT_SIZE + 1];
    int playerX, playerY, endX, endY;
    boolean running = false, showSolution = false;
    char direction = 'A';
    Timer timer;
    Random random;

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH + 2, SCREEN_HEIGHT + 80));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    public void possibleVerticalWall(int x, int y) {
        wallVertical[x][y] = true;

        if (!dfs(playerX, playerY, new boolean[SCREEN_WIDTH / UNIT_SIZE][SCREEN_HEIGHT / UNIT_SIZE])) {
            wallVertical[x][y] = false;
        }
    }

    public void possibleHorizontalWall(int x, int y) {
        wallHorizontal[x][y] = true;

        if (!dfs(playerX, playerY, new boolean[SCREEN_WIDTH / UNIT_SIZE][SCREEN_HEIGHT / UNIT_SIZE])) {
            wallHorizontal[x][y] = false;
        }
    }

    public boolean dfs(int x, int y, boolean[][] visited) {
        if (x < 0 || x >= SCREEN_WIDTH / UNIT_SIZE || y < 0 || y >= SCREEN_HEIGHT / UNIT_SIZE || visited[x][y]) {
            return false;
        }

        if (x == endX && y == endY) {
            return true;
        }

        visited[x][y] = true;

        boolean canMoveUp = !wallHorizontal[x][y];
        boolean canMoveDown = !wallHorizontal[x][y + 1];
        boolean canMoveLeft = !wallVertical[x][y];
        boolean canMoveRight = !wallVertical[x + 1][y];

        return (canMoveUp && dfs(x, y - 1, visited)) || (canMoveDown && dfs(x, y + 1, visited)) || (canMoveLeft && dfs(x - 1, y, visited)) || (canMoveRight && dfs(x + 1, y, visited));
    }

    class Node {
        int x, y, dist;
        Node(int x, int y, int dist) {
            this.x = x;
            this.y = y;
            this.dist = dist;
        }
    }

    public void startGame() {
        int fallback = 0;
        while (fallback < 100){
            playerX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE);
            playerY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE);
            while (playerX < (SCREEN_WIDTH / UNIT_SIZE) / 8 || playerX > (SCREEN_WIDTH / UNIT_SIZE) * 7 / 8) {
                playerX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE);
            }
            while (playerY < (SCREEN_HEIGHT / UNIT_SIZE) / 8 || playerY > (SCREEN_HEIGHT / UNIT_SIZE) * 7 / 8) {
                playerY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE);
            }
            endX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE);
            endY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE);
            int failsafe = 0;
            while ((Math.sqrt(Math.pow(playerX - endX, 2) + Math.pow(playerY - endY, 2)) < (SCREEN_WIDTH / UNIT_SIZE)) && failsafe < 100) {
                while (endX < (SCREEN_WIDTH / UNIT_SIZE) / 8 || endX > (SCREEN_WIDTH / UNIT_SIZE) * 7 / 8) {
                    endX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE);
                }
                while (endY < (SCREEN_HEIGHT / UNIT_SIZE) / 8 || endY > (SCREEN_HEIGHT / UNIT_SIZE) * 7 / 8) {
                    endY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE);
                }
                failsafe += 1;
            }
            if (failsafe < 100) {
                break;
            }
            fallback += 1;
        }
        if (fallback == 100) {
            System.out.println("Error Making maze (1)");
        }
        grid[endX][endY] = 2;

        do {
            resetWalls();
            createWalls();
        } while (!isMazeValid());

        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void resetWalls() {
        for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE + 1; i++) {
            for (int j = 0; j < SCREEN_HEIGHT / UNIT_SIZE; j++) {
                wallVertical[i][j] = false;
            }
        }

        for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
            for (int j = 0; j < SCREEN_HEIGHT / UNIT_SIZE + 1; j++) {
                wallHorizontal[i][j] = false;
            }
        }
    }

    public void createWalls() {
        for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
            wallVertical[0][i] = true;
            wallVertical[SCREEN_WIDTH / UNIT_SIZE][i] = true;
        }

        for (int j = 0; j < SCREEN_WIDTH / UNIT_SIZE; j++) {
            wallHorizontal[j][0] = true;
            wallHorizontal[j][SCREEN_HEIGHT / UNIT_SIZE] = true;
        }

        for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
            for (int j = 1; j < SCREEN_HEIGHT / UNIT_SIZE; j++) {
                if (random.nextInt(10) > 3) {
                    possibleHorizontalWall(i, j);
                }
            }
        }

        for (int i = 1; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
            for (int j = 0; j < SCREEN_HEIGHT / UNIT_SIZE; j++) {
                if (random.nextInt(10) > 3) {
                    possibleVerticalWall(i, j);
                }
            }
        }
    }

    public boolean isMazeValid() {
        for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
            if (!wallVertical[0][i] || !wallVertical[SCREEN_WIDTH / UNIT_SIZE][i]) {
                return false;
            }
        }

        for (int j = 0; j < SCREEN_WIDTH / UNIT_SIZE; j++) {
            if (!wallHorizontal[j][0] || !wallHorizontal[j][SCREEN_HEIGHT / UNIT_SIZE]) {
                return false;
            }
        }

        boolean[][] visited = new boolean[SCREEN_WIDTH / UNIT_SIZE][SCREEN_HEIGHT / UNIT_SIZE];
        if (!dfs(playerX, playerY, visited)) {
            return false;
        }

        return true;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    
    public void draw(Graphics g) {
        if (!running) {
            gameOver(g);
            return;
        }

        g.setColor(Color.white);

        for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
            for (int j = 0; j < SCREEN_HEIGHT / UNIT_SIZE + 1; j++) {
                if (wallHorizontal[i][j]) {
                    g.drawLine(i * UNIT_SIZE, j * UNIT_SIZE, (i + 1) * UNIT_SIZE, j * UNIT_SIZE);
                }
            }
        }

        for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE + 1; i++) {
            for (int j = 0; j < SCREEN_HEIGHT / UNIT_SIZE; j++) {
                if (wallVertical[i][j]) {
                    g.drawLine(i * UNIT_SIZE, j * UNIT_SIZE, i * UNIT_SIZE, (j + 1) * UNIT_SIZE);
                }
            }
        }

        g.setColor(Color.MAGENTA);
        g.fillRect(playerX * UNIT_SIZE, playerY * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);

        g.setColor(Color.cyan);
        g.fillRect(endX * UNIT_SIZE, endY * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);

        if (showSolution) {
            g.setColor(Color.green);
            if (route != null) {
                for (Point p : route) {
                    g.drawRect(p.x * UNIT_SIZE, p.y * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
                }
            }
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Monospaced", Font.BOLD, 26));
            FontMetrics metrics1 = getFontMetrics(g.getFont());
            g.drawString("Press 'q' to hide solution", (SCREEN_WIDTH - metrics1.stringWidth("Press 'q' to hide solution")) / 2, SCREEN_HEIGHT + 36);
        } else {
            g.setColor(Color.RED);
            g.setFont(new Font("Monospaced", Font.BOLD, 26));
            FontMetrics metrics1 = getFontMetrics(g.getFont());
            g.drawString("Press 'q' to show solution", (SCREEN_WIDTH - metrics1.stringWidth("Press 'q' to show solution")) / 2, SCREEN_HEIGHT + 36);
        }

        g.setColor(Color.ORANGE);
        g.setFont(new Font("Monospaced", Font.BOLD, 45));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Maze", (SCREEN_WIDTH - metrics.stringWidth("Maze")) / 2, g.getFont().getSize());
    }

    public void move() {
        switch (direction) {
            case 'W':
                if (playerY == 0) {
                    break;
                }
                if (!wallHorizontal[playerX][playerY]) {
                    playerY -= 1;
                }
                break;

            case 'A':
                if (playerX == 0) {
                    break;
                }
                if (!wallVertical[playerX][playerY]) {
                    playerX -= 1;
                }
                break;

            case 'S':
                if (playerY == SCREEN_HEIGHT / UNIT_SIZE - 1) {
                    break;
                }
                if (!wallHorizontal[playerX][playerY + 1]) {
                    playerY += 1;
                }
                break;
            
            case 'D':
                if (playerX == SCREEN_WIDTH / UNIT_SIZE - 1) {
                    break;
                }    
                if (!wallVertical[playerX + 1][playerY]) {
                    playerX += 1;
                }
                break;
        }
    }

    public void checkGameOver() {
        if (playerX == endX && playerY == endY) {
            running = false;
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(new Color(255, 195, 0));
        g.setFont(new Font("Monospaced", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("VICTORY!", (SCREEN_WIDTH - metrics.stringWidth("VICTORY!")) / 2, (SCREEN_HEIGHT - metrics.stringWidth("Game Over!")) * 2);
    }

    @Override
    public void actionPerformed(ActionEvent c) {
        if (running) {
            move();
            checkGameOver();
        }

        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent k) {
            switch (k.getKeyChar()) {
                case 'w':
                case 'W':
                    direction = 'W';
                    break;
                case 'a':
                case 'A':
                    direction = 'A';
                    break;
                case 's':
                case 'S':
                    direction = 'S';
                    break;
                case 'd':
                case 'D':
                    direction = 'D';
                    break;
                case 'q':
                    showSolution = !showSolution;
                    break;
            }
            switch (k.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    direction = 'A';
                    break;
                case KeyEvent.VK_UP:
                    direction = 'W';
                    break;
                case KeyEvent.VK_RIGHT:
                    direction = 'D';
                    break;
                case KeyEvent.VK_DOWN:
                    direction = 'S';
                    break;    
            }
        }
    }
}
