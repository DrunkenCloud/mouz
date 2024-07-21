import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_HEIGHT * SCREEN_WIDTH)/UNIT_SIZE;
    static final int DELAY = 60;
    ArrayList<Point> route;
    int[][] grid = new int[SCREEN_WIDTH / UNIT_SIZE][SCREEN_HEIGHT / UNIT_SIZE];
    int playerX, playerY, endX, endY;
    boolean running = false, showSolution = false;
    char direction = 'A';
    Timer timer;
    Random random;

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT + 80));
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

    public void possibleWall(int x, int y) {
        grid[x][y] = 1;
        if (!dfs(playerX, playerY, new boolean[SCREEN_WIDTH/UNIT_SIZE][SCREEN_HEIGHT/UNIT_SIZE])) {
            grid[x][y] = 0;
        }
    }

    public boolean dfs(int x, int y, boolean[][] visited) {
        if (x < 0 || x >= SCREEN_WIDTH/UNIT_SIZE || y < 0 || y >= SCREEN_HEIGHT/UNIT_SIZE || grid[x][y] == 1 || visited[x][y]) {
            return false;
        }
        if (x == endX && y == endY) {
            return true;
        }

        visited[x][y] = true;

        return dfs(x + 1, y, visited) || dfs(x - 1, y , visited) || dfs(x, y + 1, visited) || dfs(x, y - 1, visited);
    }

    public ArrayList<Point> solveGame(){
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int[][] distances = new int[SCREEN_WIDTH/UNIT_SIZE][SCREEN_HEIGHT/UNIT_SIZE];

        for (int i = 0; i < SCREEN_WIDTH/UNIT_SIZE; i++) {
            Arrays.fill(distances[i], Integer.MAX_VALUE);
        }

        distances[playerX][playerY] = 0;

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.dist));
        pq.add(new Node(playerX, playerY, 0));
        boolean[][]visited = new boolean[SCREEN_WIDTH/UNIT_SIZE][SCREEN_HEIGHT/UNIT_SIZE];
        Map<Point, Point> prev = new HashMap<>();

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            int cx = current.x;
            int cy = current.y;

            if (visited[cx][cy]) continue;
            visited[cx][cy] = true;

            if (cx == endX && cy == endY) {
                return reconstructPath(prev, new Point(endX, endY));
            }

            for (int[] dir: directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                if (nx >= 0 && nx < SCREEN_WIDTH/UNIT_SIZE && ny >= 0 && ny < SCREEN_HEIGHT/UNIT_SIZE && !visited[nx][ny] && grid[nx][ny] != 1) {
                    int newDist = distances[cx][cy] + 1;
                    if (newDist < distances[nx][ny]) {
                        distances[nx][ny] = newDist;
                        pq.add(new Node(nx, ny, newDist));
                        prev.put(new Point(nx, ny), new Point(cx, cy));
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    private ArrayList<Point> reconstructPath(Map<Point, Point> prev, Point end) {
        ArrayList<Point> path = new ArrayList<Point>();

        for (Point at = end; at != null; at = prev.get(at)) {
            path.add(at);
        }

        Collections.reverse(path);
        return path;
    }

    public void startGame() {
        int fallback = 0;
        while (fallback < 100){
            playerX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE));
            playerY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE));
            while (playerX > (SCREEN_WIDTH / UNIT_SIZE)/8 || playerX < (SCREEN_WIDTH / UNIT_SIZE)*(7/8)) {
                playerX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE));
            }
            while (playerY > (SCREEN_HEIGHT / UNIT_SIZE)/8 || playerY < (SCREEN_HEIGHT / UNIT_SIZE)*(7/8)) {
                playerY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE));
            }
            endX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE));
            endY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE));
            int failsafe = 0;
            while ((Math.sqrt(Math.pow(playerX - endX, 2) + Math.pow(playerY - endY, 2)) < ((SCREEN_WIDTH/UNIT_SIZE))) && failsafe < 100) {
                while (endX > (SCREEN_WIDTH / UNIT_SIZE)/8 || endX < (SCREEN_WIDTH / UNIT_SIZE)*(7/8)) {
                    endX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE));
                }
                while (endY > (SCREEN_HEIGHT / UNIT_SIZE)/8 || endY < (SCREEN_HEIGHT / UNIT_SIZE)*(7/8)) {
                    endY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE));
                }
                failsafe += 1;
            }
            if (failsafe < 99) {
                break;
            }
            fallback += 1;
        }
        if (fallback == 100) {
            System.out.println("Error Making maze (1)");
        }
        grid[endX][endY] = 2;

        for (int i = 0; i < SCREEN_HEIGHT/UNIT_SIZE; i++) {
            for (int j = 0; j < SCREEN_WIDTH/UNIT_SIZE; j++) {
                if ((random.nextInt()%10) > 3) {
                    possibleWall(i, j);
                }
            }
        }

        running = true;
        timer = new Timer(DELAY, this);
        route = solveGame();
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
        for (int i = 0; i < SCREEN_WIDTH/UNIT_SIZE; i++) {
            for (int j = 0; j < SCREEN_HEIGHT/UNIT_SIZE; j++) {
                if (grid[i][j] == 1) {
                    g.fillRect(i*UNIT_SIZE, j*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
                }
            }
        }

        g.setColor(Color.MAGENTA);
        g.fillRect(playerX * UNIT_SIZE, playerY * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);

        g.setColor(Color.cyan);
        g.fillRect(endX * UNIT_SIZE, endY * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);

        if (showSolution) {
	        g.setColor(Color.green);
	        for (Point p : route) {
	            g.drawRect(p.x * UNIT_SIZE, p.y * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE); 
	        }
	        g.setColor(Color.ORANGE);
		    g.setFont(new Font("Monospaced", Font.BOLD, 26));
		    FontMetrics metrics1 = getFontMetrics(g.getFont());
		    g.drawString("Press 'q' for to hide solution", (SCREEN_WIDTH - metrics1.stringWidth("Press 'q' for to hide solution"))/2, SCREEN_HEIGHT + 36);
	    } else {
                g.setColor(Color.RED);
                g.setFont(new Font("Monospaced", Font.BOLD, 26));
                FontMetrics metrics1 = getFontMetrics(g.getFont());
   		        g.drawString("Press 'q' for to show solution", (SCREEN_WIDTH - metrics1.stringWidth("Press 'q' for to show solution"))/2, SCREEN_HEIGHT + 36);
	    }

        g.setColor(Color.ORANGE);
        g.setFont(new Font("Monospaced", Font.BOLD, 45));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Maze", (SCREEN_WIDTH - metrics.stringWidth("Maze"))/2, g.getFont().getSize());
    }

    public void move() {
        switch (direction) {
            case 'W':
                if (playerY == 0) {
                    break;
                }
                if (grid[playerX][playerY - 1] != 1) {
                    playerY -= 1;
                }
                break;

            case 'A':
                if (playerX == 0) {
                    break;
                }
                if (grid[playerX - 1][playerY] != 1) {
                    playerX -= 1;
                }
                break;

            case 'S':
                if (playerY == (SCREEN_HEIGHT/UNIT_SIZE) - 1) {
                    break;
                }
                if (grid[playerX][playerY + 1] != 1) {
                    playerY += 1;
                }
                break;
            
            case 'D':
                if (playerX == (SCREEN_WIDTH/UNIT_SIZE) - 1) {
                    break;
                }    
                if (grid[playerX + 1][playerY] != 1) {
                    playerX += 1;
                }
                break;
        }
        route = solveGame();
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
        g.drawString("VICTORY!", (SCREEN_WIDTH - metrics.stringWidth("VICTORY!"))/2, (SCREEN_HEIGHT - metrics.stringWidth("Game Over!")) * 2);
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
                	if (showSolution) {
                		showSolution = false;
                	} else {
                		showSolution = true;
                	}
                	break;
            }
            switch (k.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    direction = 'D';
                    break;
                case KeyEvent.VK_UP:
                    direction = 'W';
                    break;
                case KeyEvent.VK_RIGHT:
                    direction = 'A';
                    break;
                case KeyEvent.VK_DOWN:
                    direction = 'S';
                    break;    
            }
        }
    }
}