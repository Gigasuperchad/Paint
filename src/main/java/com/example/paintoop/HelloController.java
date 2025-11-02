package com.example.paintoop;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Canvas canvas;

    @FXML
    private Button outlineNoneButton;
    @FXML
    private Button outlineSolidButton;
    @FXML
    private ColorPicker outlineColorPicker;

    @FXML
    private Button fillNoneButton;
    @FXML
    private Button fillSolidButton;
    @FXML
    private ColorPicker fillColorPicker;

    private Repository repository = new LocalRepository();
    private DrawingCanvas drawingCanvas;
    private double startX, startY;
    private boolean isDrawing = false;
    private boolean isMoving = false;
    private boolean isResizing = false;
    private String currentTool = "select";
    private Shape previewShape;


    private boolean hasOutline = true;
    private boolean hasFill = false;
    private Color outlineColor = Color.BLACK;
    private Color fillColor = Color.BLUE;

    @FXML
    public void initialize() {
        drawingCanvas = new DrawingCanvas(canvas, repository);
        setupMouseHandlers();
        setupStyleButtons();

        outlineColorPicker.setValue(outlineColor);
        fillColorPicker.setValue(fillColor);

        outlineColorPicker.setOnAction(e -> {
            outlineColor = outlineColorPicker.getValue();
            welcomeText.setText("Цвет контура изменен");
        });

        fillColorPicker.setOnAction(e -> {
            fillColor = fillColorPicker.getValue();
            welcomeText.setText("Цвет заливки изменен");
        });
    }

    private void setupMouseHandlers() {
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseMoved(this::handleMouseMoved);
    }

    private void setupStyleButtons() {
        updateOutlineButtons();
        updateFillButtons();
    }

    private void updateOutlineButtons() {
        if (hasOutline) {
            outlineSolidButton.setStyle("-fx-background-color: #cccccc;");
            outlineNoneButton.setStyle("");
        } else {
            outlineNoneButton.setStyle("-fx-background-color: #cccccc;");
            outlineSolidButton.setStyle("");
        }
    }

    private void updateFillButtons() {
        if (hasFill) {
            fillSolidButton.setStyle("-fx-background-color: #cccccc;");
            fillNoneButton.setStyle("");
        } else {
            fillNoneButton.setStyle("-fx-background-color: #cccccc;");
            fillSolidButton.setStyle("");
        }
    }

    private void handleMouseMoved(MouseEvent event) {
        if (currentTool.equals("select") && drawingCanvas.getSelectedShape() != null) {
            Shape.ResizeType resizeType = drawingCanvas.getResizeTypeAt(event.getX(), event.getY());

            switch (resizeType) {
                case NORTHWEST:
                case SOUTHEAST:
                    canvas.setCursor(Cursor.NW_RESIZE);
                    break;
                case NORTHEAST:
                case SOUTHWEST:
                    canvas.setCursor(Cursor.NE_RESIZE);
                    break;
                case NORTH:
                case SOUTH:
                    canvas.setCursor(Cursor.N_RESIZE);
                    break;
                case EAST:
                case WEST:
                    canvas.setCursor(Cursor.E_RESIZE);
                    break;
                default:
                    if (drawingCanvas.getSelectedShape().contains(event.getX(), event.getY())) {
                        canvas.setCursor(Cursor.MOVE);
                    } else {
                        canvas.setCursor(Cursor.DEFAULT);
                    }
                    break;
            }
        } else {
            canvas.setCursor(Cursor.DEFAULT);
        }
    }

    private void handleMousePressed(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();

        if (currentTool.equals("select")) {
            Shape.ResizeType resizeType = drawingCanvas.getResizeTypeAt(startX, startY);
            if (resizeType != Shape.ResizeType.NONE) {
                isResizing = true;
                drawingCanvas.setCurrentResizeType(resizeType);
            } else {
                drawingCanvas.selectShapeAt(startX, startY);
                isMoving = (drawingCanvas.getSelectedShape() != null);
            }
        } else {
            isDrawing = true;
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        double currentX = event.getX();
        double currentY = event.getY();
        double deltaX = currentX - startX;
        double deltaY = currentY - startY;

        if (isMoving && currentTool.equals("select")) {
            drawingCanvas.moveSelectedShape(deltaX, deltaY);
            startX = currentX;
            startY = currentY;
        } else if (isResizing && currentTool.equals("select")) {
            drawingCanvas.resizeSelectedShape(drawingCanvas.getCurrentResizeType(), currentX, currentY);
        } else if (isDrawing) {
            double x = Math.min(startX, currentX);
            double y = Math.min(startY, currentY);
            double width = Math.abs(currentX - startX);
            double height = Math.abs(currentY - startY);

            previewShape = createPreviewShape(x, y, width, height);

            if (previewShape != null) {
                drawingCanvas.drawPreview(previewShape);
            }
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        double endX = event.getX();
        double endY = event.getY();

        if (isDrawing) {
            isDrawing = false;

            double x = Math.min(startX, endX);
            double y = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            Shape finalShape = createFinalShape(x, y, width, height);
            if (finalShape != null) {
                drawingCanvas.addShape(finalShape);
            }
        }

        isMoving = false;
        isResizing = false;
        drawingCanvas.setCurrentResizeType(Shape.ResizeType.NONE);
    }

    private Shape createPreviewShape(double x, double y, double width, double height) {
        Color strokeColor = hasOutline ? Color.GRAY : Color.TRANSPARENT;
        Color fillColor = hasFill ? Color.LIGHTGRAY : Color.TRANSPARENT;

        switch (currentTool) {
            case "square":
                double squareSize = Math.max(width, height);
                return new Square(x, y, squareSize, squareSize, strokeColor, fillColor);
            case "circle":
                return new Circle(x, y, width, height, strokeColor, fillColor);
            case "triangle":
                return new Triangle(x, y, width, height, strokeColor, fillColor);
            default:
                return null;
        }
    }

    private Shape createFinalShape(double x, double y, double width, double height) {
        Color strokeColor = hasOutline ? outlineColor : Color.TRANSPARENT;
        Color fillColor = hasFill ? this.fillColor : Color.TRANSPARENT;

        switch (currentTool) {
            case "square":
                double squareSize = Math.max(width, height);
                return new Square(x, y, squareSize, squareSize, strokeColor, fillColor);
            case "circle":
                return new Circle(x, y, width, height, strokeColor, fillColor);
            case "triangle":
                return new Triangle(x, y, width, height, strokeColor, fillColor);
            default:
                return null;
        }
    }
    @FXML
    protected void onSelectButtonClick() {
        welcomeText.setText("Инструмент: Выделение");
        currentTool = "select";
    }
    @FXML
    protected void onSquareButtonClick() {
        welcomeText.setText("Инструмент: Квадрат");
        currentTool = "square";
    }
    @FXML
    protected void onCircleButtonClick() {
        welcomeText.setText("Инструмент: Круг");
        currentTool = "circle";
    }
    @FXML
    protected void onTriangleButtonClick() {
        welcomeText.setText("Инструмент: Треугольник");
        currentTool = "triangle";
    }
    @FXML
    protected void onDeleteButtonClick() {
        drawingCanvas.deleteSelectedShape();
        welcomeText.setText("Удалено выделение");
    }
    @FXML
    protected void onClearButtonClick() {
        repository.clear();
        drawingCanvas.clearCanvas();
        welcomeText.setText("Холст очищен");
    }
    @FXML
    protected void onOutlineNoneClick() {
        hasOutline = false;
        updateOutlineButtons();
        welcomeText.setText("Контур: отключен");
    }
    @FXML
    protected void onOutlineSolidClick() {
        hasOutline = true;
        updateOutlineButtons();
        welcomeText.setText("Контур: сплошной");
    }
    @FXML
    protected void onFillNoneClick() {
        hasFill = false;
        updateFillButtons();
        welcomeText.setText("Заливка: отключена");
    }

    @FXML
    protected void onFillSolidClick() {
        hasFill = true;
        updateFillButtons();
        welcomeText.setText("Заливка: сплошная");
    }
}