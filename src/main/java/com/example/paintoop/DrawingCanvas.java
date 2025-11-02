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

    private static final double MAX_PAN_MARGIN = 500; // pixels

    public DrawingCanvas(Canvas canvas, Repository repository) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.repository = repository;
        clearCanvas();
    }

    public void zoom(double delta, double pivotX, double pivotY) {
        double oldScale = scale;
        scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale + delta));


        double scaleFactor = scale / oldScale;
        translateX = pivotX - (pivotX - translateX) * scaleFactor;
        translateY = pivotY - (pivotY - translateY) * scaleFactor;


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
        translateX = 0;
        translateY = 0;
        redrawAllShapes();
    }

    private void applyPanBoundaries() {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();


        double visibleWidth = canvasWidth / scale;
        double visibleHeight = canvasHeight / scale;
        double maxTranslateX = Math.max(0, (visibleWidth * scale - canvasWidth) / 2 + MAX_PAN_MARGIN);
        double maxTranslateY = Math.max(0, (visibleHeight * scale - canvasHeight) / 2 + MAX_PAN_MARGIN);

        translateX = Math.max(-maxTranslateX, Math.min(maxTranslateX, translateX));
        translateY = Math.max(-maxTranslateY, Math.min(maxTranslateY, translateY));

        if (scale < 1.0) {
            double scaleFactor = (1.0 - scale) * MAX_PAN_MARGIN;
            translateX = Math.max(-scaleFactor, Math.min(scaleFactor, translateX));
            translateY = Math.max(-scaleFactor, Math.min(scaleFactor, translateY));
        }
    }

    public double toViewX(double modelX) {
        return modelX * scale + translateX;
    }

    public double toViewY(double modelY) {
        return modelY * scale + translateY;
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
        gc.translate(translateX, translateY);
        gc.scale(scale, scale);

        for (Shape shape : repository.getAllShapes()) {
            shape.draw(gc);
        }
        gc.restore();
    }

    public void drawPreview(Shape previewShape) {
        clearCanvas();
        gc.save();
        gc.translate(translateX, translateY);
        gc.scale(scale, scale);

        for (Shape shape : repository.getAllShapes()) {
            shape.draw(gc);
        }
        previewShape.draw(gc);

        gc.restore();
    }

    public void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void addShape(Shape shape) {
        repository.addShape(shape);
        redrawAllShapes();
    }

    public Shape selectShapeAt(double x, double y) {
        double modelX = toModelX(x);
        double modelY = toModelY(y);

        for (Shape shape : repository.getAllShapes()) {
            shape.setSelected(false);
        }

        for (int i = repository.getAllShapes().size() - 1; i >= 0; i--) {
            Shape shape = repository.getAllShapes().get(i);
            if (shape.contains(modelX, modelY)) {
                shape.setSelected(true);
                selectedShape = shape;
                redrawAllShapes();
                return shape;
            }
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
            selectedShape.setX(selectedShape.getX() + deltaX);
            selectedShape.setY(selectedShape.getY() + deltaY);
            redrawAllShapes();
        }
    }

    public void resizeSelectedShape(Shape.ResizeType resizeType, double newX, double newY) {
        if (selectedShape != null && resizeType != Shape.ResizeType.NONE) {
            selectedShape.resize(resizeType, newX, newY);
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

    public void setScale(double scale) {
        this.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
        applyPanBoundaries();
        redrawAllShapes();
    }

    public double getTranslateX() {
        return translateX;
    }

    public void setTranslateX(double translateX) {
        this.translateX = translateX;
        applyPanBoundaries();
        redrawAllShapes();
    }

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        this.translateY = translateY;
        applyPanBoundaries();
        redrawAllShapes();
    }
}