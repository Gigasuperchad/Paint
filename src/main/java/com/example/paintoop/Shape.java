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
    public abstract Shape copy();

    public abstract void setStrokeColor(Color color);
    public abstract void setFillColor(Color color);

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

        if (isInResizeHandle(pointX, pointY, x, y)) return ResizeType.NORTHWEST;
        if (isInResizeHandle(pointX, pointY, x + width, y)) return ResizeType.NORTHEAST;
        if (isInResizeHandle(pointX, pointY, x, y + height)) return ResizeType.SOUTHWEST;
        if (isInResizeHandle(pointX, pointY, x + width, y + height)) return ResizeType.SOUTHEAST;

        if (isInResizeHandle(pointX, pointY, x + width/2, y)) return ResizeType.NORTH;
        if (isInResizeHandle(pointX, pointY, x + width/2, y + height)) return ResizeType.SOUTH;
        if (isInResizeHandle(pointX, pointY, x, y + height/2)) return ResizeType.WEST;
        if (isInResizeHandle(pointX, pointY, x + width, y + height/2)) return ResizeType.EAST;

        return ResizeType.NONE;
    }

    protected boolean isInResizeHandle(double pointX, double pointY, double handleX, double handleY) {
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

    protected void drawResizeHandle(GraphicsContext gc, double handleX, double handleY) {
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

class Rectangle extends Shape {
    private Color strokeColor;
    private Color fillColor;

    public Rectangle(double x, double y, double width, double height, Color strokeColor, Color fillColor) {
        super(x, y, width, height);
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
    }

    @Override
    public Shape copy() {
        Rectangle copy = new Rectangle(x, y, width, height, strokeColor, fillColor);
        copy.setSelected(this.isSelected);
        return copy;
    }

    @Override
    public void setStrokeColor(Color color) {
        this.strokeColor = color;
    }

    @Override
    public void setFillColor(Color color) {
        this.fillColor = color;
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
    public Color getFillColor() { return fillColor; }
}

class Ellipse extends Shape {
    private Color strokeColor;
    private Color fillColor;

    public Ellipse(double x, double y, double width, double height, Color strokeColor, Color fillColor) {
        super(x, y, width, height);
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
    }

    @Override
    public Shape copy() {
        Ellipse copy = new Ellipse(x, y, width, height, strokeColor, fillColor);
        copy.setSelected(this.isSelected);
        return copy;
    }

    // НОВЫЕ МЕТОДЫ ДЛЯ ИЗМЕНЕНИЯ ЦВЕТОВ
    @Override
    public void setStrokeColor(Color color) {
        this.strokeColor = color;
    }

    @Override
    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (fillColor != null && !fillColor.equals(Color.TRANSPARENT)) {
            gc.setFill(fillColor);
            gc.fillOval(x, y, width, height);
        }

        if (strokeColor != null && !strokeColor.equals(Color.TRANSPARENT)) {
            gc.setStroke(strokeColor);
            gc.setLineWidth(2);
            gc.strokeOval(x, y, width, height);
        }

        drawSelection(gc);
    }

    public Color getStrokeColor() { return strokeColor; }
    public Color getFillColor() { return fillColor; }
}

class Line extends Shape {
    private Color strokeColor;
    private double startX, startY, endX, endY;

    public Line(double startX, double startY, double endX, double endY, Color strokeColor) {
        super(Math.min(startX, endX), Math.min(startY, endY),
                Math.abs(endX - startX), Math.abs(endY - startY));
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.strokeColor = strokeColor;
    }

    @Override
    public Shape copy() {
        Line copy = new Line(startX, startY, endX, endY, strokeColor);
        copy.setSelected(this.isSelected);
        return copy;
    }

    @Override
    public void setStrokeColor(Color color) {
        this.strokeColor = color;
    }

    @Override
    public void setFillColor(Color color) {
        // У линии нет заливки, поэтому метод пустой
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (strokeColor != null && !strokeColor.equals(Color.TRANSPARENT)) {
            gc.setStroke(strokeColor);
            gc.setLineWidth(2);
            gc.strokeLine(startX, startY, endX, endY);
        }

        drawSelection(gc);
    }

    @Override
    public boolean contains(double pointX, double pointY) {
        double lineLength = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
        if (lineLength == 0) return false;

        double distance = Math.abs(
                (endY - startY) * pointX - (endX - startX) * pointY + endX * startY - endY * startX
        ) / lineLength;

        if (distance <= 5) {
            double dotProduct = ((pointX - startX) * (endX - startX) + (pointY - startY) * (endY - startY))
                    / (lineLength * lineLength);
            return dotProduct >= 0 && dotProduct <= 1;
        }
        return false;
    }

    @Override
    public ResizeType getResizeHandle(double pointX, double pointY) {
        if (!isSelected) return ResizeType.NONE;
        if (isInResizeHandle(pointX, pointY, startX, startY)) return ResizeType.NORTHWEST;
        if (isInResizeHandle(pointX, pointY, endX, endY)) return ResizeType.SOUTHEAST;

        return ResizeType.NONE;
    }

    @Override
    public void resize(ResizeType resizeType, double newX, double newY) {
        switch (resizeType) {
            case NORTHWEST:
                startX = newX;
                startY = newY;
                break;
            case SOUTHEAST:
                endX = newX;
                endY = newY;
                break;
            default:
                break;
        }
        updateBoundingBox();
    }

    @Override
    public void drawSelection(GraphicsContext gc) {
        if (!isSelected) return;

        gc.setStroke(Color.RED);
        gc.setLineWidth(1);
        gc.setLineDashes(5);
        gc.strokeRect(x - 2, y - 2, width + 4, height + 4);
        gc.setLineDashes(null);

        gc.setFill(Color.WHITE);
        gc.setStroke(Color.RED);

        drawResizeHandle(gc, startX, startY);
        drawResizeHandle(gc, endX, endY);
    }

    private void updateBoundingBox() {
        x = Math.min(startX, endX);
        y = Math.min(startY, endY);
        width = Math.abs(endX - startX);
        height = Math.abs(endY - startY);
    }

    @Override
    public void setX(double x) {
        double deltaX = x - this.x;
        startX += deltaX;
        endX += deltaX;
        updateBoundingBox();
    }

    @Override
    public void setY(double y) {
        double deltaY = y - this.y;
        startY += deltaY;
        endY += deltaY;
        updateBoundingBox();
    }

    public Color getStrokeColor() { return strokeColor; }
    public double getStartX() { return startX; }
    public double getStartY() { return startY; }
    public double getEndX() { return endX; }
    public double getEndY() { return endY; }
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
    public Shape copy() {
        Triangle copy = new Triangle(x, y, width, height, strokeColor, fillColor);
        copy.setSelected(this.isSelected);
        return copy;
    }

    @Override
    public void setStrokeColor(Color color) {
        this.strokeColor = color;
    }

    @Override
    public void setFillColor(Color color) {
        this.fillColor = color;
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
}