package flappyBird;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird implements ActionListener, KeyListener {

    public static FlappyBird flappyBird;
    public Renderer renderer;
    public Rectangle bird;
    public ArrayList<Rectangle> columns;
    public JFrame jFrame;
    public Timer timer;
    public Random rand;
    public boolean gameOver, started;
    public int ticks, ySpeed, score, record;
    public final int speed = 5;
    public final int WIDTH = 1200, HEIGHT = 800;
    private final String iconFilePath = "resources/icons/";

    public FlappyBird(){
        jFrame = new JFrame();
        renderer = new Renderer();
        timer = new Timer(20, this);
        rand = new Random();

        reset();

        jFrame.add(renderer);
    }

    public void config(){
        jFrame.setTitle("Flappy Bird");
        ImageIcon imageIcon = new ImageIcon(iconFilePath + "game-icon.png");
        jFrame.setIconImage(imageIcon.getImage());

        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.addKeyListener(this);
        jFrame.setSize(WIDTH, HEIGHT);
        jFrame.setResizable(false);
        jFrame.setVisible(true);

        timer.start();
    }

    public void reset() {
        score = 0;
        bird = new Rectangle(WIDTH / 2 - 19, HEIGHT / 2 - 13, 39, 26);
        if (columns == null) {
            columns = new ArrayList<>();
        }else{
            columns.clear();
        }

        for (int i = 0; i < WIDTH/250 +1; i++) {
            addColumn(true);
        }
        gameOver = false;
    }

    public void jump() {
        if (ySpeed>0){
            ySpeed = -10;
        }else if (ySpeed<-10){
            ySpeed -= 5;
        }else {
            ySpeed -= 10;
        }
    }

    public void addColumn(boolean start){
        int space = 250;
        int width = 100;
        int height = 50 + rand.nextInt(300);
        int stride = 150;

        if (start) {
            columns.add(new Rectangle(WIDTH + width + columns.size() * stride, HEIGHT - height - 130, width, height));
            columns.add(new Rectangle(WIDTH + width + (columns.size() - 1) * stride, 0, width, HEIGHT - height - space));
        } else {
            columns.add(new Rectangle(columns.get(columns.size()-1).x + 2*stride, HEIGHT - height - 130, width, height));
            columns.add(new Rectangle(columns.get(columns.size()-2).x + 2*stride, 0, width, HEIGHT - height - space));
        }
    }

    private static BufferedImage rotate(BufferedImage bimg, Double angle) {
        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                cos = Math.abs(Math.cos(Math.toRadians(angle)));
        int w = bimg.getWidth();
        int h = bimg.getHeight();
        int neww = (int) Math.floor(w*cos + h*sin),
                newh = (int) Math.floor(h*cos + w*sin);
        BufferedImage rotated = new BufferedImage(neww, newh, bimg.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.translate((neww-w)/2, (newh-h)/2);
        graphic.rotate(Math.toRadians(angle), w/2, h/2);
        graphic.drawRenderedImage(bimg, null);
        graphic.dispose();
        return rotated;
    }

    public void paintBird(Graphics g, Rectangle bird) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(iconFilePath + "bird.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert img != null;
        Image fimg = rotate(img, (double) (ySpeed*4.5));
        g.drawImage(fimg, bird.x-3, bird.y-2, bird.width+3, bird.height+2, renderer);
    }

    public void paintColumn(Graphics g, Rectangle column){
        int head = 50;
        String pathVar;
        if (column.y==0){
            pathVar = "top";
        }else {
            pathVar = "bot";
        }
        ImageIcon imageIcon = new ImageIcon(String.format(iconFilePath + "%s-column.png", pathVar));
        Image img = imageIcon.getImage();

        ImageIcon imageIconPipe = new ImageIcon(String.format(iconFilePath + "%s-column-pipe.png", pathVar));
        Image imgPipe = imageIconPipe.getImage();

        if (pathVar.equals("top")){
            g.drawImage(imgPipe, column.x, column.y, column.width, column.height-head, renderer);
            g.drawImage(img, column.x, column.height-head, column.width, head, renderer);
        }else {
            g.drawImage(imgPipe, column.x, column.y + head, column.width, column.height-head, renderer);
            g.drawImage(img, column.x, column.y, column.width, head, renderer);
        }

    }

    public void repaint(Graphics g){
        ImageIcon imageIcon = new ImageIcon(iconFilePath + "background.png");
        Image img = imageIcon.getImage();
        g.drawImage(img,0, 0, WIDTH, HEIGHT - 125, renderer);
        ImageIcon imageIcon2 = new ImageIcon(iconFilePath + "ground.png");
        Image img2 = imageIcon2.getImage();
        g.drawImage(img2, 0,HEIGHT - 130, WIDTH, 130, renderer);

        flappyBird.paintBird(g, bird);
        for (Rectangle column : columns) {
            flappyBird.paintColumn(g, column);
        }

        if (!started){
            g.setColor(Color.RED.brighter());
            g.setFont(new Font("Arial", Font.ITALIC, 30));
            g.fillRect(WIDTH/2 - 250, HEIGHT/2 -100, 500, 200);
            g.setColor(Color.WHITE);
            g.drawString("Welcome! Press X for new game", WIDTH/2 - 230 , HEIGHT/2 );
        }

        if (gameOver){
            g.setColor(Color.RED.brighter());

            g.fillRect(WIDTH/2 - 250, HEIGHT/2 -100, 500, 200);
            g.setColor(Color.WHITE);
            g.drawString("Game Over! Press X for new game", WIDTH/2 - 235 , HEIGHT/2 );
            started = false;
        }

        if (!gameOver){
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 70));
            g.drawString(String.valueOf(score/2), WIDTH / 2 - 10, 55);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 55));
        g.drawString(String.format("Record: %d", record), 20, 55);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ticks++;

        if (started){
            for (int i = 0; i < columns.size(); i++) {
                Rectangle column = columns.get(i);
                column.x -= speed;
            }

            if (ticks % 2 == 0 && ySpeed < 15) {
                ySpeed += 2;
            }

            for (int i = 0; i < columns.size(); i++) {
                Rectangle column = columns.get(i);
                if (column.x + column.width < 0) {
                    if (column.y == 0) {
                        addColumn(false);
                    }
                    columns.remove(column);
                }
            }

            bird.y += ySpeed;

            for (Rectangle column : columns) {
                if (column.intersects(bird)) {
                    gameOver = true;
                }
                if (bird.x + bird.width / 2 > column.x + column.width / 2 - 1 &&
                        bird.x + bird.width / 2 < column.x + column.width / 2 + 1) {
                    score++;
                }

            }
            if (bird.y > HEIGHT - 130 - bird.height) {
                bird.y = HEIGHT - 130 - bird.height;
                gameOver = true;
            }
        }
        if (record < score/2){
            record = score/2;
        }
        renderer.repaint();
    }

    public static void main(String[] args) {
        flappyBird = new FlappyBird();
        flappyBird.config();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == 'x'){
            if (gameOver){
                reset();
                started = true;
            }else {
                started = true;
                jump();
            }
        }else if (e.getKeyChar() == ' '){
            if (!gameOver){
                jump();
            }
        }
    }



    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
