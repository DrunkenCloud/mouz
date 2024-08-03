import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import java.util.PriorityQueue;
import java.util.Comparator;

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
        this.setPreferredSize(new Dimension(SCREEN_WIDTH + 1, SCREEN_HEIGHT + 80));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    class Node {
        int x, y, dist;
        Node(int x, int y, int dist) {
            this.x = x;
            this.y = y;
            this.dist = dist;
        }
    }

    public ArrayList<Point> dijkstra(int startX, int startY, int endX, int endY) {
        int[][] dist = new int[SCREEN_WIDTH / UNIT_SIZE][SCREEN_HEIGHT / UNIT_SIZE];
        Point[][] prev = new Point[SCREEN_WIDTH / UNIT_SIZE][SCREEN_HEIGHT / UNIT_SIZE];
        boolean[][] visited = new boolean[SCREEN_WIDTH / UNIT_SIZE][SCREEN_HEIGHT / UNIT_SIZE];
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.dist));

        for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
            for (int j = 0; j < SCREEN_HEIGHT / UNIT_SIZE; j++) {
                dist[i][j] = Integer.MAX_VALUE;
                prev[i][j] = null;
                visited[i][j] = false;
            }
        }

        dist[startX][startY] = 0;
        queue.add(new Node(startX, startY, 0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            int x = current.x, y = current.y;

            if (visited[x][y]) continue;
            visited[x][y] = true;

            if (x == endX && y == endY) break;

            int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

            for (int[] dir : directions) {
                int newX = x + dir[0], newY = y + dir[1];
                if (newX >= 0 && newX < SCREEN_WIDTH / UNIT_SIZE && newY >= 0 && newY < SCREEN_HEIGHT / UNIT_SIZE) {
                    if ((dir[0] == 1 && !wallVertical[x + 1][y]) || (dir[0] == -1 && !wallVertical[x][y]) ||
                        (dir[1] == 1 && !wallHorizontal[x][y + 1]) || (dir[1] == -1 && !wallHorizontal[x][y])) {
                        int newDist = dist[x][y] + 1;
                        if (newDist < dist[newX][newY]) {
                            dist[newX][newY] = newDist;
                            prev[newX][newY] = new Point(x, y);
                            queue.add(new Node(newX, newY, newDist));
                        }
                    }
                }
            }
        }

        ArrayList<Point> path = new ArrayList<>();
        for (Point at = new Point(endX, endY); at != null; at = prev[at.x][at.y]) {
            path.add(at);
        }

        Collections.reverse(path);
        return path.size() > 1 ? path : null;
    }

    public boolean allDone(boolean[][] visited) {
        for (int i = 0; i < SCREEN_WIDTH/UNIT_SIZE; i++) {
            for (int j = 0; j < SCREEN_HEIGHT/UNIT_SIZE; j++) {
                if (!visited[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    public void dfs(int x, int y, boolean[][] visited) {
        if (x < 0 || x >= SCREEN_WIDTH/UNIT_SIZE || y < 0 || y >= SCREEN_HEIGHT/UNIT_SIZE || visited[x][y]) {
            return;
        }

        visited[x][y] = true;

        if (allDone(visited)) {
            return;
        }

        while(!allDone(visited)) {
            Vector<Integer> possibles = new Vector<Integer>();
            
            if (y != 0 && !visited[x][y-1]) 
                possibles.add(1);
            if (x != 0 && !visited[x - 1][y])
                possibles.add(2);
            if (y != SCREEN_HEIGHT/UNIT_SIZE-1 && !visited[x][y + 1])
                possibles.add(3);
            if (x != SCREEN_WIDTH/UNIT_SIZE - 1 && !visited[x + 1][y])
                possibles.add(4);

            if (possibles.size() == 0) {
                return;
            }

            int direction = possibles.get(random.nextInt(possibles.size()));

            if (direction == 1) {
                wallHorizontal[x][y] = false;
                dfs(x, y - 1, visited);
            } else if (direction == 2) {
                wallVertical[x][y] = false;
                dfs(x - 1, y, visited);
            } else if (direction == 3) {
                wallHorizontal[x][y + 1] = false;
                dfs(x, y + 1, visited);
            } else {
                wallVertical[x + 1][y] = false;
                dfs(x + 1, y, visited);
            }
        }
    }

    public void temp(int x, int y, boolean[][] visited) {
        visited[x][y] = true;
        while (true) {
            Vector<Integer> possibles = new Vector<Integer>();
            if (y > 0 && !visited[x][y - 1])
                possibles.add(1);
            if (x > 0 && !visited[x - 1][y])
                possibles.add(2);
            if (y < SCREEN_HEIGHT / UNIT_SIZE - 1 && !visited[x][y + 1])
                possibles.add(3);
            if (x < SCREEN_WIDTH / UNIT_SIZE - 1 && !visited[x + 1][y])
                possibles.add(4);

            if (possibles.size() == 0)
                break;

            int direction = possibles.get(random.nextInt(possibles.size()));
            if (direction == 1) {
                wallHorizontal[x][y] = false;
                y -= 1;
            } else if (direction == 2) {
                wallVertical[x][y] = false;
                x -= 1;
            } else if (direction == 3) {
                wallHorizontal[x][y + 1] = false;
                y += 1;
            } else {
                wallVertical[x + 1][y] = false;
                x += 1;
            }
            visited[x][y] = true;
        }

        for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
            for (int j = 0; j < SCREEN_HEIGHT / UNIT_SIZE; j++) {
                if (!visited[i][j]) {
                    Vector<Integer> adjacents = new Vector<>();
                    if (j > 0 && visited[i][j - 1])
                        adjacents.add(1);
                    if (i > 0 && visited[i - 1][j])
                        adjacents.add(2);
                    if (j < SCREEN_HEIGHT / UNIT_SIZE - 1 && visited[i][j + 1])
                        adjacents.add(3);
                    if (i < SCREEN_WIDTH / UNIT_SIZE - 1 && visited[i + 1][j])
                        adjacents.add(4);

                    if (adjacents.size() > 0) {
                        int direction = adjacents.get(random.nextInt(adjacents.size()));
                        if (direction == 1) {
                            wallHorizontal[i][j] = false;
                            temp(i, j - 1, visited);
                        } else if (direction == 2) {
                            wallVertical[i][j] = false;
                            temp(i - 1, j, visited);
                        } else if (direction == 3) {
                            wallHorizontal[i][j + 1] = false;
                            temp(i, j + 1, visited);
                        } else {
                            wallVertical[i + 1][j] = false;
                            temp(i + 1, j, visited);
                        }
                    }
                }
            }
        }
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
            for(int j = 0; j < SCREEN_WIDTH/UNIT_SIZE + 1; j++) {
                wallVertical[j][i] = true;
            }
        }

        for (int i = 0; i < SCREEN_HEIGHT/UNIT_SIZE + 1; i++) {
            for (int j = 0; j < SCREEN_WIDTH / UNIT_SIZE; j++) {
                wallHorizontal[j][i] = true;
            }
        }

        boolean[][] visited = new boolean[SCREEN_WIDTH/UNIT_SIZE][SCREEN_HEIGHT/UNIT_SIZE];
        for (int i = 0; i < SCREEN_WIDTH/UNIT_SIZE; i++) {
            for (int j = 0; j < SCREEN_HEIGHT/UNIT_SIZE; j++) {
                visited[i][j] = false;
            }
        }

        temp(random.nextInt(SCREEN_WIDTH / UNIT_SIZE), random.nextInt(SCREEN_HEIGHT / UNIT_SIZE), visited);

        while(!allDone(visited)){
            boolean check = false;
            for (int i = 0; i < SCREEN_HEIGHT/UNIT_SIZE; i++) {
                for (int j = 0; j < SCREEN_WIDTH/UNIT_SIZE; j++) {
                    if (check) {
                        break;
                    }
                    if (!visited[i][j]) {
                        check = true;
                        temp(i, j, visited);
                    }
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

        return true;
    }

    public void startGame() {
        do {
            resetWalls();
            createWalls();
        } while (!isMazeValid());

        int fallback = 0;
        boolean validPoints = false;

        while (fallback < 100 && !validPoints) {
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

            while (Math.sqrt(Math.pow(playerX - endX, 2) + Math.pow(playerY - endY, 2)) < (SCREEN_WIDTH / UNIT_SIZE) && failsafe < 100) {
                while (endX < (SCREEN_WIDTH / UNIT_SIZE) / 8 || endX > (SCREEN_WIDTH / UNIT_SIZE) * 7 / 8) {
                    endX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE);
                }

                while (endY < (SCREEN_HEIGHT / UNIT_SIZE) / 8 || endY > (SCREEN_HEIGHT / UNIT_SIZE) * 7 / 8) {
                    endY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE);
                }

                failsafe++;
            }

            if (failsafe < 100) {
                validPoints = true;
            }

            fallback++;
        }

        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
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

        g.setColor(new Color(132, 3, 168));
        g.fillRect(playerX * UNIT_SIZE + 2, playerY * UNIT_SIZE + 2, UNIT_SIZE - 4, UNIT_SIZE - 4);

        g.setColor(new Color(135, 4, 61));
        g.fillRect(endX * UNIT_SIZE + 2, endY * UNIT_SIZE + 2, UNIT_SIZE - 4, UNIT_SIZE - 4);

        route = dijkstra(playerX, playerY, endX, endY);

        if (showSolution) {
            g.setColor(Color.RED);
            if (route != null) {
                for (Point p : route) {
                    g.drawRect(p.x * UNIT_SIZE + 1, p.y * UNIT_SIZE + 1, UNIT_SIZE - 2, UNIT_SIZE - 2);
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
            default:
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
