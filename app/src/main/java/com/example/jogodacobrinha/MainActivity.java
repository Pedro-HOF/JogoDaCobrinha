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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    //lista de pontos da cobra / tamanho da cobra
    private final List<SnakePoints> snakePointsList = new ArrayList<>();
    private SurfaceView surfaceView;
    private TextView scoreTV;

    //surface holder para desenhar a cobra no canvas da superficie
    private SurfaceHolder surfaceHolder;

    //Posicao do movimento da cobra. Valores devem ser right, left, top ou bottom.
    //Por padrao a cobra se move para a direita
    private String movingPosition = "right";

    //score
    private int score = 0;

    //tamanho da cobra / tamanho do ponto
    //voce pode mudar o tamanho da cobra e do ponto aqui
    private static final int pointSize = 28;

    //tamanho padrao do tale da cobra
    private static final int defaultTalePoints = 3;

    // cor da cobra
    private static final int snakeColor = Color.YELLOW;

    //velocidade de movimento da cobra. valor deve ficar entre 1 - 1000
    private static final int snakeMovingSpeed = 800;

    //coordenadas da posicao aleatoria do ponto na surfaceView
    private int positionX, positionY;

    //timer para mover a cobra / mudar a posicao da cobra depois de um tempos especifico (snakeMovingSpeed)
    private Timer timer;

    //canvas para desenhar a cobra e mostra na surfaceView
    private Canvas canvas = null;

    //cor do ponto da cobra
    private Paint pointColor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Pegando a SurfaceView e pontuacao  do arquivo xml
        surfaceView = findViewById(R.id.surfaceView);
        scoreTV = findViewById(R.id.scoreTV);

        //Pegando as imagens dos botoes do arquivo xml
        final AppCompatImageButton topBtn = findViewById(R.id.topBtn);
        final AppCompatImageButton leftBtn = findViewById(R.id.leftBtn);
        final AppCompatImageButton rightBtn = findViewById(R.id.rightBtn);
        final AppCompatImageButton bottomBtn = findViewById(R.id.bottomBtn);

        //Adicionando callback para SurfaceView
        surfaceView.getHolder().addCallback(this);

        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Verifica se a posicao anterior do movimento nao e para baixo
                //Por exemplo se a cobra se move para baixo entao a combra nao pode se mover diretamente para cima
                //A cobra deve pegar o caminho da direita ou esquerda antes aí para cima
                if(movingPosition.equals("bottom")){
                    movingPosition = "top";
                }
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!movingPosition.equals("right"))
                    movingPosition = "left";
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!movingPosition.equals("left"))
                    movingPosition = "right";
            }
        });

        bottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!movingPosition.equals("top"))
                    movingPosition = "bottom";
            }
        });

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

        this.surfaceHolder = surfaceHolder;

        // Aguarda o layout terminar para garantir que a largura/altura da surfaceView seja válida
        surfaceView.post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    private void init(){

        //limpa os pontos da cobra / tamanho da cobra
        snakePointsList.clear();

        //deixa o padrao de pontos 0
        scoreTV.setText("0");

        //faz o score 0
        score = 0;

        //movimento padrao da cobra
        movingPosition = "right";

        //posicao padrao inicial da cobra na tela
        int startPositionX = (pointSize) * defaultTalePoints;

        // fazendo o tamanho padrao da cobra / pontos
        for(int i = 0; i < defaultTalePoints; i++){

            //adicionando pontos para tale da cobra
            SnakePoints snakePoint = new SnakePoints(startPositionX, pointSize);
            snakePointsList.add(snakePoint);

            //valor sendo aumentado para o proximo ponto da cobra
            startPositionX = startPositionX - (pointSize * 2);
        }

        //adiciona pontos aleatórios na tela para ser comido pela cobra
        addPoint();

        //comeca a mover a cobra / comeca o jogo
        moveSnake();
    }

    private void addPoint(){

        //pega a largura e altura da surfaceView e adiciona ponto na tela para ser comida pela cobra
        int surfaceWidth = surfaceView.getWidth() - (pointSize * 2);
        int surfaceHeight = surfaceView.getHeight() - (pointSize * 2);

        int randomXPosition = new Random().nextInt(surfaceWidth / pointSize);
        int randomYPosition = new Random().nextInt(surfaceHeight / pointSize);

        //checa se a posicao do ponto eh par, so precisa de par
        if((randomXPosition % 2) != 0){
            randomXPosition = randomXPosition + 1;
        }

        //checa se a posicao do ponto eh par, so precisa de par
        if((randomYPosition % 2) != 0){
            randomYPosition = randomYPosition + 1;
        }
        positionX = (pointSize * randomXPosition) + pointSize;
        positionY = (pointSize * randomYPosition) + pointSize;
    }
    private void moveSnake(){

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                //pegando a posicao da cabeca
                int headPositionX = snakePointsList.get(0).getPositionX();
                int headPositionY = snakePointsList.get(0).getPositionY();

                //verifica se a cobra comeu um ponto
                if(headPositionX == positionX && headPositionY == positionY){
                    //aumenta o tamanho da cobra depois de comer um ponto
                    growSnake();

                    //adiciona outro ponto aleatorio na tela
                    addPoint();
                }

                //checa de que lado a cobra esta se movendo
                switch (movingPosition){
                    case "right":

                        //mexe a cabeca da cobra pra direita, outros pontos seguem a cabeca da cobra para mover a cobra
                        snakePointsList.get(0).setPositionX(headPositionX + (pointSize * 2));
                        snakePointsList.get(0).setPositionY(headPositionY);
                        break;

                    case "left":

                        //mexe a cabeca da cobra pra esquerda, outros pontos seguem a cabeca da cobra para mover a cobra
                        snakePointsList.get(0).setPositionX(headPositionX - (pointSize * 2));
                        snakePointsList.get(0).setPositionY(headPositionY);
                        break;

                    case "top":

                        //mexe a cabeca da cobra pra cima, outros pontos seguem a cabeca da cobra para mover a cobra
                        snakePointsList.get(0).setPositionX(headPositionX);
                        snakePointsList.get(0).setPositionY(headPositionY - (pointSize * 2));
                        break;

                    case "bottom":

                        //mexe a cabeca da cobra pra baixo, outros pontos seguem a cabeca da cobra para mover a cobra
                        snakePointsList.get(0).setPositionX(headPositionX);
                        snakePointsList.get(0).setPositionY(headPositionY + (pointSize * 2));
                        break;
                }


                //checa se gameover na parede ou nela mesma
                if(checkGameOver(headPositionX, headPositionY)){

                    //parar o timer / para de mover a cobra
                    timer.purge();
                    timer.cancel();

                    //mostra mensagem de gameover
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Sua pontuacao foi: " + score + " pontos");
                    builder.setTitle("Perdeu o jogo!");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Comecar Novamente", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //reinicia o jogo / recomeca os dados
                            init();
                        }
                    });

                    //timer roda no fundo para mostrar dialogo na thread principal
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            builder.show();
                        }
                    });
                }

                else{

                    //trava o canvas no surfaceHolder para desenhar nele
                    canvas = surfaceHolder.lockCanvas();
                    //limpa o canvas com cor branca
                    canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
                    //muda a posicao da cabeca da cobra, outros pontos da cobra vao seguir a cabeca da cobra
                    canvas.drawCircle(snakePointsList.get(0).getPositionX(), snakePointsList.get(0).getPositionY(), pointSize, createPointColor());
                    //desenha os pontos circulos aleatorios na tela para a cobra comer
                    canvas.drawCircle(positionX, positionY, pointSize, createPointColor());
                    //outros pontos estao seguindo a cabeca da cobra, posicao 0 e a cabeca da cobra
                    for(int i = 1; i < snakePointsList.size(); i++){
                        int getTempPositionX = snakePointsList.get(i).getPositionX();
                        int getTempPositionY = snakePointsList.get(i).getPositionY();

                        //mova pontos atraves da cabeca da cobra
                        snakePointsList.get(i).setPositionX(headPositionX);
                        snakePointsList.get(i).setPositionY(headPositionY);
                        canvas.drawCircle(snakePointsList.get(i).getPositionX(), snakePointsList.get(i).getPositionY(), pointSize, createPointColor());

                        //muda a posicao da cabeca
                        headPositionX = getTempPositionX;
                        headPositionY = getTempPositionY;
                    }
                    //destrava o canvas para desenhhar no surfaceview
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }, 1000- snakeMovingSpeed, 1000- snakeMovingSpeed);
    }

    private void growSnake(){

        //cria um novo ponto da cobra
        SnakePoints snakePoints = new SnakePoints(0,0);

        //adiciona ponto no tale da cobra
        snakePointsList.add(snakePoints);

        //aumenta o score
        score++;

        //coloca o score no textViews
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTV.setText(String.valueOf(score));
            }
        });

    }
    private boolean checkGameOver(int headPositionX, int headPositionY){
        boolean gameOver = false;

        //checa se a cobra bateu na parede
        if(snakePointsList.get(0).getPositionX() < 0 ||
                snakePointsList.get(0).getPositionY() < 0 ||
                snakePointsList.get(0).getPositionX() >= surfaceView.getWidth() ||
                snakePointsList.get(0).getPositionY() >= surfaceView.getHeight())
        {
           gameOver = true;
        }
        else{

            //checa se a cobra bateu nela mesma
            for(int i = 1; i < snakePointsList.size(); i++){

                if(headPositionX == snakePointsList.get(i).getPositionX() &&
                        headPositionY == snakePointsList.get(i).getPositionY()){
                    gameOver = true;
                    break;
                }

            }
        }

        return gameOver;
    }

    private Paint createPointColor(){

        //checa se a cor do ponto da cobra ja existe
        if(pointColor == null){
            pointColor = new Paint();
            pointColor.setColor(snakeColor);
            pointColor.setStyle(Paint.Style.FILL);
            pointColor.setAntiAlias(true); //antialiasing
        }

        return pointColor;
    }
}