package com.example.paintoop;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Shape {
    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected boolean isSelected = false;
    protected static final double RESIZE_HANDLE_SIZE = 8;

    public enum ResizeType {
        NONE, NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST
    }

    public Shape(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void draw(GraphicsContext gc);

    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX <= x + width &&
                pointY >= y && pointY <= y + height;
    }

    public ResizeType getResizeHandle(double pointX, double pointY) {
        if (!isSelected) return ResizeType.NONE;

        // Проверяем угловые маркеры
        if (isInResizeHandle(pointX, pointY, x, y)) return ResizeType.NORTHWEST;
        if (isInResizeHandle(pointX, pointY, x + width, y)) return ResizeType.NORTHEAST;
        if (isInResizeHandle(pointX, pointY, x, y + height)) return ResizeType.SOUTHWEST;
        if (isInResizeHandle(pointX, pointY, x + width, y + height)) return ResizeType.SOUTHEAST;

        // Проверяем боковые маркеры
        if (isInResizeHandle(pointX, pointY, x + width/2, y)) return ResizeType.NORTH;
        if (isInResizeHandle(pointX, pointY, x + width/2, y + height)) return ResizeType.SOUTH;
        if (isInResizeHandle(pointX, pointY, x, y + height/2)) return ResizeType.WEST;
        if (isInResizeHandle(pointX, pointY, x + width, y + height/2)) return ResizeType.EAST;

        return ResizeType.NONE;
    }

    private boolean isInResizeHandle(double pointX, double pointY, double handleX, double handleY) {
        return pointX >= handleX - RESIZE_HANDLE_SIZE/2 &&
                pointX <= handleX + RESIZE_HANDLE_SIZE/2 &&
                pointY >= handleY - RESIZE_HANDLE_SIZE/2 &&
                pointY <= handleY + RESIZE_HANDLE_SIZE/2;
    }

    public void resize(ResizeType resizeType, double newX, double newY) {
        double minSize = 10;

        switch (resizeType) {
            case NORTHWEST:
                double newWidth = (x + width) - newX;
                double newHeight = (y + height) - newY;
                if (newWidth >= minSize && newHeight >= minSize) {
                    width = newWidth;
                    height = newHeight;
                    x = newX;
                    y = newY;
                }
                break;
            case NORTHEAST:
                newWidth = newX - x;
                newHeight = (y + height) - newY;
                if (newWidth >= minSize && newHeight >= minSize) {
                    width = newWidth;
                    height = newHeight;
                    y = newY;
                }
                break;
            case SOUTHWEST:
                newWidth = (x + width) - newX;
                newHeight = newY - y;
                if (newWidth >= minSize && newHeight >= minSize) {
                    width = newWidth;
                    height = newHeight;
                    x = newX;
                }
                break;
            case SOUTHEAST:
                newWidth = newX - x;
                newHeight = newY - y;
                if (newWidth >= minSize && newHeight >= minSize) {
                    width = newWidth;
                    height = newHeight;
                }
                break;
            case NORTH:
                newHeight = (y + height) - newY;
                if (newHeight >= minSize) {
                    height = newHeight;
                    y = newY;
                }
                break;
            case SOUTH:
                newHeight = newY - y;
                if (newHeight >= minSize) {
                    height = newHeight;
                }
                break;
            case EAST:
                newWidth = newX - x;
                if (newWidth >= minSize) {
                    width = newWidth;
                }
                break;
            case WEST:
                newWidth = (x + width) - newX;
                if (newWidth >= minSize) {
                    width = newWidth;
                    x = newX;
                }
                break;
            default:
                break;
        }
    }

    public void drawSelection(GraphicsContext gc) {
        if (!isSelected) return;

        gc.setStroke(Color.RED);
        gc.setLineWidth(1);
        gc.setLineDashes(5);
        gc.strokeRect(x - 2, y - 2, width + 4, height + 4);
        gc.setLineDashes(null);

        gc.setFill(Color.WHITE);
        gc.setStroke(Color.RED);

        drawResizeHandle(gc, x, y);
        drawResizeHandle(gc, x + width, y);
        drawResizeHandle(gc, x, y + height);
        drawResizeHandle(gc, x + width, y + height);

        drawResizeHandle(gc, x + width/2, y);
        drawResizeHandle(gc, x + width/2, y + height);
        drawResizeHandle(gc, x, y + height/2);
        drawResizeHandle(gc, x + width, y + height/2);
    }

    private void drawResizeHandle(GraphicsContext gc, double handleX, double handleY) {
        gc.fillRect(handleX - RESIZE_HANDLE_SIZE/2, handleY - RESIZE_HANDLE_SIZE/2,
                RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE);
        gc.strokeRect(handleX - RESIZE_HANDLE_SIZE/2, handleY - RESIZE_HANDLE_SIZE/2,
                RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE);
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}

class Square extends Shape {
    private Color strokeColor;
    private Color fillColor;

    public Square(double x, double y, double width, double height, Color strokeColor, Color fillColor) {
        super(x, y, width, height);
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (fillColor != null && !fillColor.equals(Color.TRANSPARENT)) {
            gc.setFill(fillColor);
            gc.fillRect(x, y, width, height);
        }

        if (strokeColor != null && !strokeColor.equals(Color.TRANSPARENT)) {
            gc.setStroke(strokeColor);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);
        }

        drawSelection(gc);
    }

    public Color getStrokeColor() { return strokeColor; }
    public void setStrokeColor(Color strokeColor) { this.strokeColor = strokeColor; }

    public Color getFillColor() { return fillColor; }
    public void setFillColor(Color fillColor) { this.fillColor = fillColor; }
}

class Circle extends Shape {
    private Color strokeColor;
    private Color fillColor;

    public Circle(double x, double y, double width, double height, Color strokeColor, Color fillColor) {
        super(x, y, width, height);
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
    }

    @Override
    public void draw(GraphicsContext gc) {
        double radius = Math.min(width, height);
        double centerX = x + (width - radius) / 2;
        double centerY = y + (height - radius) / 2;

        if (fillColor != null && !fillColor.equals(Color.TRANSPARENT)) {
            gc.setFill(fillColor);
            gc.fillOval(centerX, centerY, radius, radius);
        }

        if (strokeColor != null && !strokeColor.equals(Color.TRANSPARENT)) {
            gc.setStroke(strokeColor);
            gc.setLineWidth(2);
            gc.strokeOval(centerX, centerY, radius, radius);
        }

        drawSelection(gc);
    }

    public Color getStrokeColor() { return strokeColor; }
    public void setStrokeColor(Color strokeColor) { this.strokeColor = strokeColor; }

    public Color getFillColor() { return fillColor; }
    public void setFillColor(Color fillColor) { this.fillColor = fillColor; }
}

class Triangle extends Shape {
    private Color strokeColor;
    private Color fillColor;

    public Triangle(double x, double y, double width, double height, Color strokeColor, Color fillColor) {
        super(x, y, width, height);
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
    }

    @Override
    public void draw(GraphicsContext gc) {
        double[] xPoints = {x + width / 2, x, x + width};
        double[] yPoints = {y, y + height, y + height};

        if (fillColor != null && !fillColor.equals(Color.TRANSPARENT)) {
            gc.setFill(fillColor);
            gc.fillPolygon(xPoints, yPoints, 3);
        }

        if (strokeColor != null && !strokeColor.equals(Color.TRANSPARENT)) {
            gc.setStroke(strokeColor);
            gc.setLineWidth(2);
            gc.strokePolygon(xPoints, yPoints, 3);
        }

        drawSelection(gc);
    }

    public Color getStrokeColor() { return strokeColor; }
    public void setStrokeColor(Color strokeColor) { this.strokeColor = strokeColor; }

    public Color getFillColor() { return fillColor; }
    public void setFillColor(Color fillColor) { this.fillColor = fillColor; }
}