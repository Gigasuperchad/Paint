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

    public DrawingCanvas(Canvas canvas, Repository repository) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.repository = repository;
        clearCanvas();
    }

    public void addShape(Shape shape) {
        repository.addShape(shape);
        redrawAllShapes();
    }

    public void redrawAllShapes() {
        clearCanvas();
        for (Shape shape : repository.getAllShapes()) {
            shape.draw(gc);
        }
    }

    public void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void drawPreview(Shape previewShape) {
        clearCanvas();
        for (Shape shape : repository.getAllShapes()) {
            shape.draw(gc);
        }
        previewShape.draw(gc);
    }

    public Shape selectShapeAt(double x, double y) {
        for (Shape shape : repository.getAllShapes()) {
            shape.setSelected(false);
        }

        for (int i = repository.getAllShapes().size() - 1; i >= 0; i--) {
            Shape shape = repository.getAllShapes().get(i);
            if (shape.contains(x, y)) {
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
            return selectedShape.getResizeHandle(x, y);
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
}