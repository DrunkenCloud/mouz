import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
	
	static final int SCREEN_WIDTH = 600;
	static final int SCREEN_HEIGHT = 600;
	static final int UNIT_SIZE = 25;
	static final int GAME_UNITS = (SCREEN_HEIGHT * SCREEN_WIDTH)/UNIT_SIZE;
	static final int DELAY = 60;
	final int x[] = new int[GAME_UNITS];	
	final int y[] = new int[GAME_UNITS];
	int bodyParts = 6;
	int appleX, appleY, applesEaten = 0;
	char direction = 'S';

	boolean running = false;
	boolean finish = false;
	Timer timer;
	Random random;

	GamePanel() {
		random = new Random();
		this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
		this.setBackground(Color.black);
		this.setFocusable(true);
		this.addKeyListener(new MyKeyAdapter());
		startGame();
	}

	public void startGame() {
		x[0] = 0;
		y[0] = 0;
		while(x[0] == 0 || y[0] == 0) {
			if (x[0] == 0) {
				x[0] = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE))*UNIT_SIZE;
				for (int i = 0; i < bodyParts; i++) {
					x[i] = x[0];
				}
			}
			if (y[0] == 0) {
				y[0] = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE))*UNIT_SIZE;
				for (int i = 0; i < bodyParts; i++) {
					y[i] = y[0];
				}
			}
		}
		newApple();
		running = true;
		timer = new Timer(DELAY, this);
		timer.start();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}
	
	public void draw(Graphics g) {
		if (!running) {
			gameOver(g);
			return;
		}

		g.setColor(Color.red);
		g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

		for (int i = 0; i < bodyParts; i++) {
			if (i == 0) {
				g.setColor(Color.green);
				g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
			} else {
				g.setColor(new Color(45, 180, 10));
				g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
			}
		}

		g.setColor(Color.CYAN);
		g.setFont(new Font("Monospaced", Font.BOLD, 30));
		FontMetrics metrics = getFontMetrics(g.getFont());
		g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)), g.getFont().getSize());
	}

	public boolean validAppleXPosition() {
		for (int i = 0; i < bodyParts; i++) {
			if (appleX == x[i]) {
				return false;
			}
		}
		return true;
	}

	public boolean validAppleYPosition() {
		for (int i = 0; i < bodyParts; i++) {
			if (appleX == x[i]) {
				return false;
			}
		}
		return true;
	}

	public void newApple() {
		appleX = random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE))*UNIT_SIZE;
		appleY = random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE))*UNIT_SIZE;
	}

	public void move() {
		for (int i = bodyParts; i > 0; i--) {
			x[i] = x[i - 1];
			y[i] = y[i - 1];
		}

		switch (direction) {
			case 'W':
				y[0] = y[0] - UNIT_SIZE;
				break;

			case 'A':
				x[0] = x[0] - UNIT_SIZE;
				break;

			case 'S':
				y[0] = y[0] + UNIT_SIZE;
				break;
			
			case 'D':
				x[0] = x[0] + UNIT_SIZE;
				break;
		}

		if (x[0] == SCREEN_WIDTH) {
			x[0] = 0;
		}
		if (x[0] < 0) {
			x[0] = SCREEN_WIDTH - UNIT_SIZE;
		}
		if (y[0] == SCREEN_HEIGHT) {
			y[0] = 0;
		}
		if (y[0] < 0) {
			y[0] = SCREEN_HEIGHT - UNIT_SIZE;
		}
	}

	public void checkApple() {
		if ((x[0] == appleX) && y[0] == appleY) {
			bodyParts++;
			applesEaten++;
			newApple();
		}
	}

	public void checkGameOver() {
		for (int i = bodyParts; i > 0; i--) {
			if ((x[0] == x[i]) && (y[0] == y[i])) {
				running = false;
			}
		}
		if (bodyParts == 10) {
			running = false;
			finish = true;
		}
	}

	public void gameOver(Graphics g) {
		
		if (!finish){		
			g.setColor(new Color(127, 0, 255));
			g.setFont(new Font("Monospaced", Font.BOLD, 75));
			FontMetrics metrics = getFontMetrics(g.getFont());
			g.drawString("Game over!", (SCREEN_WIDTH - metrics.stringWidth("Game Over!"))/2, (SCREEN_HEIGHT - metrics.stringWidth("Game Over!")) * 2);
			g.setColor(Color.CYAN);
			g.setFont(new Font("Monospaced", Font.BOLD, 50));
			FontMetrics metrics1 = getFontMetrics(g.getFont());
			g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize()*4);
		} else {
			g.setColor(new Color(255, 195, 0));
			g.setFont(new Font("Monospaced", Font.BOLD, 75));
			FontMetrics metrics = getFontMetrics(g.getFont());
			g.drawString("VICTORY!", (SCREEN_WIDTH - metrics.stringWidth("VICTORY!"))/2, (SCREEN_HEIGHT - metrics.stringWidth("Game Over!")) * 2);
		}
	}

	@Override
	public void actionPerformed(ActionEvent c) {
		if (running) {
			move();
			checkApple();
			checkGameOver();
		}

		repaint();
	}

	public class MyKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent k) {
			switch (k.getKeyChar()) {
				case 'w':
					if (direction != 'S'){
						direction = 'W';
					}
					break;
				case 'a':
					if (direction != 'D'){
						direction = 'A';
					}
					break;
				case 's':
					if (direction != 'W') {
						direction = 'S';
					}
					break;
				case 'd':
					if (direction != 'A') {
						direction = 'D';
					}
					break;	
			}
			switch (k.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					if (direction != 'A') {
						direction = 'D';
					}
					break;
				case KeyEvent.VK_UP:
					if (direction != 'S'){
						direction = 'W';
					}
					break;
				case KeyEvent.VK_RIGHT:
					if (direction != 'D'){
						direction = 'A';
					}
					break;
				case KeyEvent.VK_DOWN:
					if (direction != 'W') {
						direction = 'S';
					}
					break;	
			}
		}
	}
}