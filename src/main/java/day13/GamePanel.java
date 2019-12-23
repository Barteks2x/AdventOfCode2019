package day13;

import intcode.Intcode;
import utils.Position2;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class GamePanel extends JPanel {
    private LinkedBlockingDeque<Integer> input;
    private final Map<Position2, Integer> screen = new ConcurrentHashMap<>();

    public GamePanel(BigInteger[] program) {
        input = new LinkedBlockingDeque<>();
        setFocusable(true);
        requestFocus();
        new Thread(() -> {
            AtomicInteger x = new AtomicInteger();
            AtomicInteger y = new AtomicInteger();

            AtomicInteger step = new AtomicInteger();
            Intcode.runProgram(program, () -> {
                Position2 ballPos = null;
                Position2 paddle = null;
                for (Position2 pos : screen.keySet()) {
                    Integer val = screen.get(pos);
                    if (val == null) {
                        continue;
                    }
                    if (val == 4) {
                        ballPos = pos;
                    }
                    if (val == 3) {
                        paddle = pos;
                    }
                }

                if (paddle == null || ballPos == null) {
                    return 0;
                }
                try {
                    Thread.sleep(4);
                } catch (InterruptedException ignored) {
                }
                return (int) Math.signum(ballPos.x - paddle.x);
            }, out -> {
                int s = step.getAndIncrement() % 3;
                if (s == 0) {
                    x.set(out);
                } else if (s == 1) {
                    y.set(out);
                } else {
                    screen.put(new Position2(x.get(), y.get()), out);
                    EventQueue.invokeLater(this::repaint);
                }
            });
        }).start();
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    input.add(-1);
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    input.add(1);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
                    input.add(0);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        screen.forEach((pos, tile) -> {
            if (pos.x == -1 && pos.y == 0) {
                g.clearRect(0, 0, 200, 10);
                g.setColor(Color.BLACK);
                //System.out.println(tile);
                g.drawString(tile + "", 10, 10);
                return;
            }
            int color = 0xFFFFFFFF;
            if (tile == 1) {
                color = 0xFF000000;
            } else if (tile == 2) {
                color = 0xFF0000FF;
            } else if (tile == 3) {
                color = 0xFF999999;
            } else if (tile == 4) {
                color = 0xFFFF0000;
            }
            g.setColor(new Color(color));
            g.fillRect(pos.x * 10 + 20, pos.y * 10 + 20, 10, 10);
        });
    }
}
