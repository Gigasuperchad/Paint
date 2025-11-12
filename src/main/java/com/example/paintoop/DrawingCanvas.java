package com.example.paintoop;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DrawingCanvas {
    private Canvas canvas;
    private GraphicsContext gc;
    private Repository repository;
    private Shape selectedShape;
    private Shape.ResizeType currentResizeType = Shape.ResizeType.NONE;

    private double scale = 1.0;
    private double translateX = 0;
    private double translateY = 0;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 5.0;
    private double virtualWidth = 2000;
    private double virtualHeight = 1500;

    public DrawingCanvas(Canvas canvas, Repository repository) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.repository = repository;
        clearCanvas();
        centerView();
    }

    public void zoom(double delta, double pivotX, double pivotY) {
        scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale + delta));

        double modelX = toModelX(pivotX);
        double modelY = toModelY(pivotY);

        translateX = pivotX - modelX * scale;
        translateY = pivotY - modelY * scale;

        applyPanBoundaries();
        redrawAllShapes();
    }

    public void pan(double deltaX, double deltaY) {
        translateX += deltaX;
        translateY += deltaY;
        applyPanBoundaries();
        redrawAllShapes();
    }

    public void resetView() {
        scale = 1.0;
        centerView();
    }

    private void centerView() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        translateX = (canvasWidth - virtualWidth * scale) / 2;
        translateY = (canvasHeight - virtualHeight * scale) / 2;

        applyPanBoundaries();
        redrawAllShapes();
    }

    private void applyPanBoundaries() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        double scaledVirtualWidth = virtualWidth * scale;
        double scaledVirtualHeight = virtualHeight * scale;

        if (scaledVirtualWidth <= canvasWidth) {
            translateX = (canvasWidth - scaledVirtualWidth) / 2;
        } else {
            translateX = Math.max(canvasWidth - scaledVirtualWidth, Math.min(0, translateX));
        }

        if (scaledVirtualHeight <= canvasHeight) {
            translateY = (canvasHeight - scaledVirtualHeight) / 2;
        } else {
            translateY = Math.max(canvasHeight - scaledVirtualHeight, Math.min(0, translateY));
        }
    }

    public double toModelX(double viewX) {
        return (viewX - translateX) / scale;
    }

    public double toModelY(double viewY) {
        return (viewY - translateY) / scale;
    }

    public void redrawAllShapes() {
        clearCanvas();

        gc.save();

        gc.beginPath();
        gc.rect(translateX, translateY, virtualWidth * scale, virtualHeight * scale);
        gc.clip();

        gc.translate(translateX, translateY);
        gc.scale(scale, scale);

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, virtualWidth, virtualHeight);

        for (Shape shape : repository.getAllShapes()) {
            shape.draw(gc);
        }

        gc.restore();
    }

    public void drawPreview(Shape previewShape) {
        clearCanvas();

        gc.save();

        gc.beginPath();
        gc.rect(translateX, translateY, virtualWidth * scale, virtualHeight * scale);
        gc.clip();

        gc.translate(translateX, translateY);
        gc.scale(scale, scale);

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, virtualWidth, virtualHeight);

        for (Shape shape : repository.getAllShapes()) {
            shape.draw(gc);
        }

        previewShape.draw(gc);

        gc.restore();
    }

    public void clearCanvas() {
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void addShape(Shape shape) {
        if (isShapeIntersectingVirtualCanvas(shape)) {
            repository.addShape(shape);
            redrawAllShapes();
        }
    }

    private boolean isShapeIntersectingVirtualCanvas(Shape shape) {
        double x = shape.getX();
        double y = shape.getY();
        double width = shape.getWidth();
        double height = shape.getHeight();

        return x + width > 0 && x < virtualWidth &&
                y + height > 0 && y < virtualHeight;
    }

    private boolean isShapePositionValid(double x, double y, double width, double height) {

        return width > 0 && height > 0 && isShapeIntersectingVirtualCanvas(new Rectangle(x, y, width, height, Color.BLACK, Color.BLACK));
    }

    public Shape selectShapeAt(double x, double y) {
        double modelX = toModelX(x);
        double modelY = toModelY(y);

        if (modelX < 0 || modelX > virtualWidth || modelY < 0 || modelY > virtualHeight) {
            return null;
        }
        for (Shape shape : repository.getAllShapes()) {
            shape.setSelected(false);
        }

        Shape selected = null;
        for (int i = repository.getAllShapes().size() - 1; i >= 0; i--) {
            Shape shape = repository.getAllShapes().get(i);
            if (shape.contains(modelX, modelY)) {
                selected = shape;
                break;
            }
        }

        if (selected != null) {
            selected.setSelected(true);
            selectedShape = selected;
            repository.bringToFront(selected);

            redrawAllShapes();
            return selected;
        }

        selectedShape = null;
        redrawAllShapes();
        return null;
    }

    public Shape.ResizeType getResizeTypeAt(double x, double y) {
        if (selectedShape != null) {
            double modelX = toModelX(x);
            double modelY = toModelY(y);
            return selectedShape.getResizeHandle(modelX, modelY);
        }
        return Shape.ResizeType.NONE;
    }

    public void moveSelectedShape(double deltaX, double deltaY) {
        if (selectedShape != null) {
            double newX = selectedShape.getX() + deltaX;
            double newY = selectedShape.getY() + deltaY;

            if (isShapePositionValid(newX, newY, selectedShape.getWidth(), selectedShape.getHeight())) {
                selectedShape.setX(newX);
                selectedShape.setY(newY);
                redrawAllShapes();
            }
        }
    }

    public void resizeSelectedShape(Shape.ResizeType resizeType, double newX, double newY) {
        if (selectedShape != null && resizeType != Shape.ResizeType.NONE) {
            double oldX = selectedShape.getX();
            double oldY = selectedShape.getY();
            double oldWidth = selectedShape.getWidth();
            double oldHeight = selectedShape.getHeight();
            selectedShape.resize(resizeType, newX, newY);

            if (!isShapePositionValid(selectedShape.getX(), selectedShape.getY(),
                    selectedShape.getWidth(), selectedShape.getHeight())) {
                selectedShape.setX(oldX);
                selectedShape.setY(oldY);
                selectedShape.setWidth(oldWidth);
                selectedShape.setHeight(oldHeight);
            }

            redrawAllShapes();
        }
    }

    public void deleteSelectedShape() {
        if (selectedShape != null) {
            repository.removeShape(selectedShape);
            selectedShape = null;
            redrawAllShapes();
        }
    }

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public Shape.ResizeType getCurrentResizeType() {
        return currentResizeType;
    }

    public void setCurrentResizeType(Shape.ResizeType resizeType) {
        this.currentResizeType = resizeType;
    }

    public double getScale() {
        return scale;
    }
    public double getVirtualWidth() {
        return virtualWidth;
    }

    public double getVirtualHeight() {
        return virtualHeight;
    }
}