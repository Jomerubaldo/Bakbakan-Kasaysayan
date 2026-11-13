package com.bakbakan.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public final class BackdropArt {

    private BackdropArt() {
    }

    public static void sunRays(GraphicsContext gc, double cx, double cy,
                               double length, Color color) {
        gc.setFill(color);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45.0);
            double spread = Math.toRadians(9.0);
            double x1 = cx + Math.cos(angle - spread) * length;
            double y1 = cy + Math.sin(angle - spread) * length;
            double x2 = cx + Math.cos(angle + spread) * length;
            double y2 = cy + Math.sin(angle + spread) * length;
            gc.fillPolygon(new double[]{cx, x1, x2}, new double[]{cy, y1, y2}, 3);
        }
    }

    public static void star(GraphicsContext gc, double cx, double cy,
                            double radius, Color color) {
        double[] xs = new double[10];
        double[] ys = new double[10];
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? radius : radius * 0.42;
            double angle = -Math.PI / 2 + i * Math.PI / 5.0;
            xs[i] = cx + Math.cos(angle) * r;
            ys[i] = cy + Math.sin(angle) * r;
        }
        gc.setFill(color);
        gc.fillPolygon(xs, ys, 10);
    }

    public static void cloud(GraphicsContext gc, double x, double y,
                             double width, Color color) {
        gc.setFill(color);
        gc.fillOval(x, y, width * 0.55, width * 0.22);
        gc.fillOval(x + width * 0.25, y - width * 0.10, width * 0.55, width * 0.28);
        gc.fillOval(x + width * 0.55, y, width * 0.45, width * 0.20);
    }
}
