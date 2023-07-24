package com.example.project2;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ArkanoidGame extends Application {
  private static final int WIDTH = 800;
  private static final int HEIGHT = 600;
  private static final int PADDLE_WIDTH = 125;
  private static final int PADDLE_HEIGHT = 10;
  private static final int BALL_RADIUS = 8;
  private static final Color PADDLE_COLOR = Color.BLUE;
  private static final Color BALL_COLOR = Color.RED;
  private static final Color BLOCK_COLOR = Color.GREEN;

  private Pane root;
  private Rectangle paddle;
  private Circle ball;
  private int ballSpeedX = 2;
  private int ballSpeedY = -2;
  private int score = 0;
  private Text scoreText;
  private Text pauseText;
  private Text gameOverText;
  private Button restartButton;
  private boolean paused = true;
  private int currentLevel = 1;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Arkanoid Game");

    root = new Pane();
    Scene scene = new Scene(root, WIDTH, HEIGHT);
    Text instructionsText = new Text("Инструкции:\n\n" +
        "Управление: Используйте стрелки Влево и Вправо\n" +
        "Цель игры: Разбейте все зеленые блоки мячом\n" +
        "Пауза: Нажмите на кнопку P для приостановки игры");
    instructionsText.setFont(Font.font("Arial", 18));
    instructionsText.setX(100);
    instructionsText.setY(HEIGHT - 200);
    root.getChildren().add(instructionsText);


    Button startButton = new Button("Начало игры");
    startButton.setLayoutX((WIDTH - 100) / 2);
    startButton.setLayoutY((HEIGHT - 30) / 2);
    startButton.setOnAction(e -> {
      paused = false;
      root.getChildren().remove(startButton);
      root.getChildren().remove(instructionsText);
    });
    root.getChildren().add(startButton);

    createPaddle();
    createBall();
    createBlocksForLevel(currentLevel);
    createScoreText();
    createPauseText();
    createGameOverText();
    createRestartButton();

    scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));

    primaryStage.setScene(scene);
    primaryStage.show();

    AnimationTimer timer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        if (!paused) {
          moveBall();
          checkCollisionWithBlocks();
          checkLevelComplete();
        }
      }
    };
    timer.start();
  }

  private void checkVictory() {
    boolean hasGreenBlocks = false;

    for (Node node : root.getChildren()) {
      if (node instanceof Rectangle && ((Rectangle) node).getFill() == BLOCK_COLOR) {
        hasGreenBlocks = true;
        break;
      }
    }

    if (!hasGreenBlocks) {
      currentLevel++;

      if (currentLevel <= 5) {
        Text victoryText = new Text("Победа");
        victoryText.setFont(Font.font("Arial", 36));
        victoryText.setFill(Color.RED);
        victoryText.setX((WIDTH - victoryText.getBoundsInLocal().getWidth()) / 2);
        victoryText.setY(HEIGHT / 7);
        root.getChildren().add(victoryText);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> root.getChildren().remove(victoryText));
        pause.play();

        if (currentLevel > 1) {
          Button nextLevelButton = new Button("Перейти на " + (currentLevel) + " уровень");
          nextLevelButton.setLayoutX((WIDTH - 150) / 2);
          nextLevelButton.setLayoutY((HEIGHT - 30) / 2);
          nextLevelButton.setOnAction(e -> {
            root.getChildren().remove(nextLevelButton);
            restartLevel();
          });
          root.getChildren().add(nextLevelButton);
        } else {
          createRestartButton();
        }

        paused = true;
      } else {
        Text gameOverText = new Text("Вы прошли все уровни! Игра окончена.");
        gameOverText.setFont(Font.font("Arial", 24));
        gameOverText.setFill(Color.RED);
        gameOverText.setX((WIDTH - gameOverText.getBoundsInLocal().getWidth()) / 2);
        gameOverText.setY(HEIGHT / 2);
        root.getChildren().add(gameOverText);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
          root.getChildren().remove(gameOverText);
          createRestartButton();
        });
        pause.play();

        paused = true;
      }
    }
  }


  private void createPauseText() {
    pauseText = new Text("Вы поставили на паузу");
    pauseText.setFont(Font.font("Arial", 24));
    pauseText.setX((WIDTH - pauseText.getLayoutBounds().getWidth()) / 2);
    pauseText.setY(HEIGHT / 2);
  }

  private void createGameOverText() {
    gameOverText = new Text("Вы проиграли");
    gameOverText.setFont(Font.font("Arial", 36));
    gameOverText.setX((WIDTH - gameOverText.getLayoutBounds().getWidth()) / 2);
    gameOverText.setY(HEIGHT / 2);
  }

  private void createRestartButton() {
    restartButton = new Button("Перезапустить игру");
    restartButton.setLayoutX((WIDTH - 100) / 2);
    restartButton.setLayoutY((HEIGHT - 30) / 2 + 40);
    restartButton.setOnAction(e -> {
      restartLevel();
      root.getChildren().remove(gameOverText);
      root.getChildren().remove(restartButton);
    });
  }

  private void createPaddle() {
    paddle = new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_COLOR);
    paddle.setX((WIDTH - PADDLE_WIDTH) / 2);
    paddle.setY(HEIGHT - PADDLE_HEIGHT - 10);
    root.getChildren().add(paddle);
  }

  private void createBall() {
    ball = new Circle(BALL_RADIUS, BALL_COLOR);
    ball.setCenterX((WIDTH - BALL_RADIUS) / 2);
    ball.setCenterY(HEIGHT - PADDLE_HEIGHT - BALL_RADIUS - 10);
    root.getChildren().add(ball);
  }

  private void createBlocksForLevel(int level) {
    if (level == 1) {
      // Уровень 1
      for (int row = 0; row < 1; row++) {
        for (int col = 0; col < 8; col++) {
          Rectangle block = new Rectangle(90, 30, BLOCK_COLOR);
          block.setX(col * 100);
          block.setY(row * 35);
          root.getChildren().add(block);
        }
      }
    } else if (level == 2) {
      // Уровень 2
      for (int row = 0; row < 4; row++) {
        for (int col = 0; col < 6; col++) {
          Rectangle block = new Rectangle(90, 30, BLOCK_COLOR);
          block.setX(col * 120 + 60);
          block.setY(row * 35 + 50);
          root.getChildren().add(block);
        }
      }
    } else if (level == 3) {
      // Уровень 3
      for (int row = 0; row < 2; row++) {
        for (int col = 0; col < 9; col++) {
          Rectangle block = new Rectangle(90, 30, BLOCK_COLOR);
          block.setX(col * 100);
          block.setY(row * 35);
          root.getChildren().add(block);
        }
      }
    } else if (level == 4) {
      // Уровень 4
      for (int row = 0; row < 5; row++) {
        for (int col = 0; col < 5; col++) {
          Rectangle block = new Rectangle(90, 30, BLOCK_COLOR);
          block.setX(col * 100 + 100);
          block.setY(row * 35 + 50);
          root.getChildren().add(block);
        }
      }
    } else if (level == 5) {
      // Уровень 5
      for (int row = 0; row < 6; row++) {
        for (int col = 0; col < 10; col++) {
          Rectangle block = new Rectangle(70, 20, BLOCK_COLOR);
          block.setX(col * 75 + 50);
          block.setY(row * 25 + 50);
          root.getChildren().add(block);
        }
      }
    }
  }


  private void createScoreText() {
    scoreText = new Text("Score: 0");
    scoreText.setFont(Font.font("Arial", 12));
    scoreText.setX(10);
    scoreText.setY(220);
    root.getChildren().add(scoreText);
  }
  private void moveBall() {
    double ballX = ball.getCenterX();
    double ballY = ball.getCenterY();

    ballX += ballSpeedX;
    ballY += ballSpeedY;

    if (ballY + BALL_RADIUS >= paddle.getY() && ballX >= paddle.getX() && ballX <= paddle.getX() + PADDLE_WIDTH) {
      double relativeIntersectX = ballX - (paddle.getX() + PADDLE_WIDTH / 2);
      double normalizedRelativeIntersectionX = relativeIntersectX / (PADDLE_WIDTH / 2);
      ballSpeedX = (int) (normalizedRelativeIntersectionX * 5);
      ballSpeedY = -ballSpeedY;
      ballY = paddle.getY() - BALL_RADIUS;
    }

    if (ballX <= BALL_RADIUS || ballX >= WIDTH - BALL_RADIUS) {
      ballSpeedX = -ballSpeedX;
    }

    if (ballY <= BALL_RADIUS) {
      ballSpeedY = -ballSpeedY;
    }

    if (ballY >= HEIGHT - BALL_RADIUS) {
      ballSpeedX = 0;
      ballSpeedY = 0;
      paused = true;
      root.getChildren().add(gameOverText);
      root.getChildren().add(restartButton);
    }

    ball.setCenterX(ballX);
    ball.setCenterY(ballY);
  }

  private void checkCollisionWithBlocks() {
    for (int i = 0; i < root.getChildren().size(); i++) {
      if (root.getChildren().get(i) instanceof Rectangle) {
        Rectangle block = (Rectangle) root.getChildren().get(i);
        if (block != paddle && ball.getBoundsInParent().intersects(block.getBoundsInParent())) {
          root.getChildren().remove(block);
          ballSpeedY = -ballSpeedY;
          increaseScore();
          checkVictory();
        }
      }
    }
  }
  private void handleKeyPress(KeyCode code) {
    switch (code) {
      case LEFT:
        movePaddle(-10);
        break;
      case RIGHT:
        movePaddle(10);
        break;
      case P:
        if (paused) {
          paused = false;
          root.getChildren().remove(pauseText);
        } else {
          paused = true;
          root.getChildren().add(pauseText);
        }
        break;
      case R:
        if (paused) {
          restartLevel();
        }
        break;
      default:
        break;
    }
  }


  private void movePaddle(int dx) {
    double paddleX = paddle.getX();
    double newPaddleX = paddleX + dx;

    if (newPaddleX >= 0 && newPaddleX <= WIDTH - PADDLE_WIDTH) {
      paddle.setX(newPaddleX);
    }
  }

  private void increaseScore() {
    score++;
    scoreText.setText("Score: " + score);
  }

  private void checkLevelComplete() {
    if (root.getChildren().stream().filter(node -> node instanceof Rectangle).count() == 0) {
      root.getChildren().remove(ball);
      if (hasNextLevel()) {
        loadNextLevel();
      } else {
        ballSpeedX = 0;
        ballSpeedY = 0;
        scoreText.setText("Game Completed! Final Score: " + score);
      }
    }
  }

  private boolean hasNextLevel() {
    // Здесь можно добавить проверку, есть ли еще уровни для загрузки
    // Например, проверка по текущему уровню, максимальному количеству уровней и т.д.
    // В данном примере, мы всегда будем загружать один и тот же уровень
    return true;
  }

  private void loadNextLevel() {
    // Здесь можно загрузить следующий уровень
    // Например, создать новые блоки и т.д.
    // В данном примере, мы всегда будем загружать один и тот же уровень
    createBlocksForLevel(currentLevel);
    resetBallAndPaddle();
    paused = false; // Снимаем паузу после загрузки следующего уровня
  }

  private void resetBallAndPaddle() {
    ball.setCenterX((WIDTH - BALL_RADIUS) / 2);
    ball.setCenterY(HEIGHT - PADDLE_HEIGHT - BALL_RADIUS - 10);

    paddle.setX((WIDTH - PADDLE_WIDTH) / 2);
    paddle.setY(HEIGHT - PADDLE_HEIGHT - 10);
  }

  private void restartLevel() {
    root.getChildren().remove(gameOverText);
    root.getChildren().remove(restartButton);

    root.getChildren().remove(paddle);
    root.getChildren().remove(ball);

    root.getChildren().removeIf(node -> node instanceof Rectangle && ((Rectangle) node).getFill() == BLOCK_COLOR);

    createPaddle();
    createBall();

    paddle.setX((WIDTH - PADDLE_WIDTH) / 2);
    paddle.setY(HEIGHT - PADDLE_HEIGHT - 10);

    ball.setCenterX((WIDTH - BALL_RADIUS) / 2);
    ball.setCenterY(HEIGHT - PADDLE_HEIGHT - BALL_RADIUS - 10);

    ballSpeedX = 2;
    ballSpeedY = -2;

    score = 0;
    scoreText.setText("Score: " + score);

    createBlocksForLevel(currentLevel);

    paused = false;
  }
}



