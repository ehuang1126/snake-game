package application;
	
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;


public class SnakePlus extends Application {
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private Snake snake;
	private Apple apple;
	private List<SnakeBody> snakeBodies = new ArrayList<SnakeBody>();
	private Group root;
	private GraphicsContext gc;
	private boolean gameOn = false;
	private double mouseX, mouseY;
	private int score;
	private int highScore;
	@Override
	public void start(Stage primaryStage) {
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		root = new Group();
		root.getChildren().add(canvas);
		snake = new Snake();
		root.getChildren().add(snake);
		apple = new Apple();
		apple.placeRandomly();
		root.getChildren().add(apple);
		Button startButton = new Button("Restart Game");
		startButton.setFont(new Font("Arial", 24));
		root.getChildren().add(startButton);
		root.applyCss();
		root.layout();
		startButton.setLayoutX((WIDTH - startButton.getWidth()) / 2);
		startButton.setLayoutY((HEIGHT - startButton.getHeight()) / 2);
		startButton.setVisible(false);
		startButton.setOnAction(event -> {
			startButton.setVisible(false);
			resetGame();
		});
		Scene scene = new Scene(root);
		gc = canvas.getGraphicsContext2D();
		highScore = readHighScore();
		displayScore();
		primaryStage.setScene(scene);
		primaryStage.setTitle("SnakePlus");
		primaryStage.show();
		
		scene.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
			mouseX = event.getX();
			mouseY = event.getY();
		});
		
		KeyFrame kf = new KeyFrame(Duration.millis(5), event -> {
			if (!gameOn) return;
			snake.move();
			int count = 0;
			for (SnakeBody snakeBody : snakeBodies) {
				snakeBody.move();
				if (count > 1 && snake.isOverlap(snakeBody)) {
					startButton.toFront();
					startButton.setVisible(true);
					gameOn = false;
				}
			}
			count++;
			if (snake.isOutOfBounds()) {
				startButton.toFront();
				startButton.setVisible(true);
				gameOn = false;
			}
			if (snake.isOverlap(apple)) {
				score++;
				if (score > highScore) {
					highScore = score;
					writeHighScore(highScore);
				}
				displayScore();
				apple.placeRandomly();
				SnakeBody body = new SnakeBody(snake.getMoveHistory(), snakeBodies.size() + 1);
				snakeBodies.add(body);
				root.getChildren().add(body);
			}
		});
		Timeline timeline = new Timeline(kf);
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
		resetGame();
		
	}
	
	public void resetGame() {
		score = 0;
		displayScore();
		gameOn = true;
		apple.placeRandomly();
		snake.reset();
		for (SnakeBody snakeBody : snakeBodies) {
			root.getChildren().remove(snakeBody);
		}
		snakeBodies = new ArrayList<SnakeBody>();
	}
	
	public void displayScore() {
		gc.clearRect(0, 0, WIDTH, 30);
		gc.setFont(new Font("Arial", 36));
		gc.fillText("Score: " + score, 10, 30);
		gc.fillText("high score : " + highScore, WIDTH - 300, 30);
	}
	
	public int readHighScore() {
		int hs = 0;
		try (BufferedReader br = new BufferedReader(new FileReader("highscore.txt"))) {
			hs = Integer.parseInt(br.readLine());
		} catch (Exception exc) {
			System.err.println(exc.getMessage());
		}
		return hs;
	}
	
	public void writeHighScore(int score) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter("highscore.txt"))) {
			bw.write(String.valueOf(score));
		} catch (Exception exc) {
			System.err.println(exc.getMessage());
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	class Snake extends ImageView {
	
		private double lastMouseX, lastMouseY;
		private double theta;
		private List<Point2D> moveHistory;
		
		public Snake() {
			super(new Image("Snakehead.png", 80, 80, false, false));
			setX(WIDTH / 2);
			setY(HEIGHT / 2);
		}
	
		public void reset() {
			setX(WIDTH/2);
			setY(HEIGHT/2);
			moveHistory = new ArrayList<Point2D>();
		}
		
		public List<Point2D> getMoveHistory() {
			return moveHistory;
		}

		public void move() {
			if (mouseX != lastMouseX || mouseY != lastMouseY) {
				double deltaX = mouseX - getX() - getImage().getWidth()/2;
				double deltaY = mouseY - getY() - getImage().getHeight()/2;
				
				theta = Math.atan2(deltaY, deltaX) ;
				setRotate(theta * 180/Math.PI);
				lastMouseX = mouseX;
				lastMouseY = mouseY;
			}
			
			double moveX = getX() + Math.cos(theta);
			double moveY = getY() + Math.sin(theta);
			setX(moveX);
			setY(moveY);
			moveHistory.add(new Point2D(moveX, moveY));
		}
		
		public boolean isOutOfBounds() {
			if (getX() >= WIDTH - getImage().getWidth() || getY() >= HEIGHT - getImage().getHeight() || getX() < 0 || getY() < 0) {
				return true;
			} else {
				return false;
			}
		}
		
		public boolean isOverlap(Node node) {
			return getBoundsInLocal().intersects(node.getBoundsInLocal());
		}
		
	}
	
	class Apple extends ImageView {
			private Random rnd = new Random();		
		public Apple() {
			super(new Image("apple.png", 80, 80, false, false));
		}
		
		public void placeRandomly() {
			double x = WIDTH - getImage().getWidth();
			double y = HEIGHT - getImage().getHeight();
			setX(rnd.nextInt((int) x));
			setY(rnd.nextInt((int) y));
		}
	}
	
	class SnakeBody extends ImageView {
		private List<Point2D> moves;
		private int moveIndex;
		
		public SnakeBody(List<Point2D> moves, int moveIndex) {
			super(new Image("snake_body.png", 80, 80, false, false));
			this.moves = moves;
			this.moveIndex = moves.size() - 80 * moveIndex;
		}
		
		public void move() {
			if (moveIndex >= 0) {
				Point2D point = moves.get(moveIndex);
				setX(point.getX());
				setY(point.getY());
			}
			moveIndex++;
		}
		
	}
	
}
