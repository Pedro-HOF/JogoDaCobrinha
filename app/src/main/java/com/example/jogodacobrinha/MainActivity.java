package com.example.jogodacobrinha;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private final List<SnakePoints> snakePointsList = new ArrayList<>();
    private SurfaceView surfaceView;
    private TextView scoreTV;

    private SurfaceHolder surfaceHolder;
    private String movingPosition = "right";
    private int score = 0;
    private int highScore = 0; // ⭐ Novo: guarda o recorde

    private static final int pointSize = 28;
    private static final int defaultTalePoints = 3;
    private static final int snakeColor = Color.YELLOW;
    private static final int snakeMovingSpeed = 800;

    private int positionX = 0, positionY = 0;
    private Timer timer;
    private Canvas canvas = null;
    private Paint pointColor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);
        scoreTV = findViewById(R.id.scoreTV);

        // ⭐ Carrega o high score
        loadHighScore();

        AppCompatImageButton topBtn = findViewById(R.id.topBtn);
        AppCompatImageButton leftBtn = findViewById(R.id.leftBtn);
        AppCompatImageButton rightBtn = findViewById(R.id.rightBtn);
        AppCompatImageButton bottomBtn = findViewById(R.id.bottomBtn);

        surfaceView.getHolder().addCallback(this);

        topBtn.setOnClickListener(view -> { if(!movingPosition.equals("bottom")) movingPosition = "top"; });
        leftBtn.setOnClickListener(view -> { if(!movingPosition.equals("right")) movingPosition = "left"; });
        rightBtn.setOnClickListener(view -> { if(!movingPosition.equals("left")) movingPosition = "right"; });
        bottomBtn.setOnClickListener(view -> { if(!movingPosition.equals("top")) movingPosition = "bottom"; });
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        init();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) { }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) { }

    private void init(){
        snakePointsList.clear();
        scoreTV.setText("0");
        score = 0;
        movingPosition = "right";

        int startPositionX = (pointSize) * defaultTalePoints;
        for(int i = 0; i < defaultTalePoints; i++){
            SnakePoints snakePoints = new SnakePoints(startPositionX, pointSize);
            snakePointsList.add(snakePoints);
            startPositionX = startPositionX - (pointSize * 2);
        }

        addPoint();
        moveSnake();
    }

    private void addPoint(){
        int surfaceWidth = surfaceView.getWidth() - (pointSize * 2);
        int surfaceHeight = surfaceView.getHeight() - (pointSize * 2);

        int randomXPosition = new Random().nextInt(surfaceWidth / pointSize);
        int randomYPosition = new Random().nextInt(surfaceHeight / pointSize);

        if((randomXPosition % 2) != 0) randomXPosition++;
        if((randomYPosition % 2) != 0) randomYPosition++;

        positionX = (pointSize * randomXPosition) + pointSize;
        positionY = (pointSize * randomYPosition) + pointSize;
    }

    private void moveSnake(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int headPositionX = snakePointsList.get(0).getPositionX();
                int headPositionY = snakePointsList.get(0).getPositionY();

                if(headPositionX == positionX && headPositionY == positionY){
                    growSnake();
                    addPoint();
                }

                switch (movingPosition){
                    case "right": snakePointsList.get(0).setPositionX(headPositionX + (pointSize * 2)); break;
                    case "left": snakePointsList.get(0).setPositionX(headPositionX - (pointSize * 2)); break;
                    case "top": snakePointsList.get(0).setPositionY(headPositionY - (pointSize * 2)); break;
                    case "bottom": snakePointsList.get(0).setPositionY(headPositionY + (pointSize * 2)); break;
                }

                if(checkGameOver(headPositionX, headPositionY)){
                    timer.purge();
                    timer.cancel();

                    // ⭐ Atualiza e salva o high score
                    if (score > highScore) {
                        highScore = score;
                        saveHighScore();
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Sua pontuação foi: " + score + " pontos\nMaior pontuação: " + highScore + " pontos");
                    builder.setTitle("Perdeu o jogo!");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Começar novamente", (dialogInterface, i) -> init());

                    runOnUiThread(() -> builder.show());
                } else {
                    canvas = surfaceHolder.lockCanvas();
                    canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
                    canvas.drawCircle(snakePointsList.get(0).getPositionX(),
                            snakePointsList.get(0).getPositionY(),
                            pointSize, createPointColor());
                    canvas.drawCircle(positionX, positionY, pointSize, createPointColor());

                    for(int i = 1; i < snakePointsList.size(); i++){
                        int getTempPositionX = snakePointsList.get(i).getPositionX();
                        int getTempPositionY = snakePointsList.get(i).getPositionY();

                        snakePointsList.get(i).setPositionX(headPositionX);
                        snakePointsList.get(i).setPositionY(headPositionY);
                        canvas.drawCircle(snakePointsList.get(i).getPositionX(),
                                snakePointsList.get(i).getPositionY(),
                                pointSize, createPointColor());

                        headPositionX = getTempPositionX;
                        headPositionY = getTempPositionY;
                    }

                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }, 1000 - snakeMovingSpeed, 1000 - snakeMovingSpeed);
    }

    private void growSnake(){
        snakePointsList.add(new SnakePoints(0,0));
        score++;
        runOnUiThread(() -> scoreTV.setText(String.valueOf(score)));
    }

    private boolean checkGameOver(int headPositionX, int headPositionY){
        if(headPositionX < 0 || headPositionY < 0 ||
                headPositionX >= surfaceView.getWidth() ||
                headPositionY >= surfaceView.getHeight()) return true;

        for(int i = 1; i < snakePointsList.size(); i++){
            if(headPositionX == snakePointsList.get(i).getPositionX() &&
                    headPositionY == snakePointsList.get(i).getPositionY()) return true;
        }
        return false;
    }

    private Paint createPointColor(){
        if(pointColor == null){
            pointColor = new Paint();
            pointColor.setColor(snakeColor);
            pointColor.setStyle(Paint.Style.FILL);
            pointColor.setAntiAlias(true);
        }
        return pointColor;
    }

    // ⭐ Métodos para persistir o high score
    private void loadHighScore() {
        try {
            FileInputStream fis = openFileInput("leaderboard.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
            reader.close();
        } catch (Exception e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try {
            FileOutputStream fos = openFileOutput("leaderboard.txt", MODE_PRIVATE);
            fos.write(String.valueOf(highScore).getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
