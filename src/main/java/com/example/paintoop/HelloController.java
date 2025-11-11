package com.example.paintoop;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Label statusText;

    @FXML
    private Canvas canvas;

    @FXML
    private ListView<String> toolsListView;

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
    private boolean isPanning = false;
    private String currentTool = "select";
    private Shape previewShape;

    private boolean hasOutline = true;
    private boolean hasFill = false;
    private Color outlineColor = Color.BLACK;
    private Color fillColor = Color.BLUE;
    private double panStartX, panStartY;

    private Stage primaryStage;
    private boolean hasUnsavedChanges = false;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        setupCloseHandler();
    }

    private void setupCloseHandler() {
        if (primaryStage != null) {
            primaryStage.setOnCloseRequest(this::handleCloseRequest);
        }
    }

    private void handleCloseRequest(WindowEvent event) {
        if (hasUnsavedChanges) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Сохранение");
            alert.setHeaderText("У вас есть несохраненные изменения");
            alert.setContentText("Вы хотите сохранить перед выходом?");

            ButtonType saveButton = new ButtonType("Сохранить");
            ButtonType dontSaveButton = new ButtonType("Не сохранять");
            ButtonType cancelButton = new ButtonType("Отмена");

            alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    if (saveToFile()) {
                        hasUnsavedChanges = false;
                        return;
                    } else {
                        event.consume();
                    }
                } else if (result.get() == dontSaveButton) {
                    return;
                } else {
                    event.consume();
                }
            } else {
                event.consume();
            }
        }
    }

    private boolean saveToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить изображение");

        FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("JPEG files (*.jpg)", "*.jpg");

        fileChooser.getExtensionFilters().addAll(pngFilter, jpgFilter);
        fileChooser.setSelectedExtensionFilter(pngFilter);

        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                double virtualWidth = drawingCanvas.getVirtualWidth();
                double virtualHeight = drawingCanvas.getVirtualHeight();

                Canvas tempCanvas = new Canvas(virtualWidth, virtualHeight);
                GraphicsContext imageGC = tempCanvas.getGraphicsContext2D();

                imageGC.setFill(Color.WHITE);
                imageGC.fillRect(0, 0, virtualWidth, virtualHeight);

                for (Shape shape : repository.getAllShapes()) {
                    shape.draw(imageGC);
                }

                WritableImage writableImage = tempCanvas.snapshot(null, null);

                String extension = getFileExtension(file);
                if (extension == null) {
                    extension = ".png";
                    file = new File(file.getAbsolutePath() + extension);
                }

                BufferedImage bufferedImage = convertToBufferedImage(writableImage);
                String format = extension.equals(".jpg") || extension.equals(".jpeg") ? "JPEG" : "PNG";

                ImageIO.write(bufferedImage, format, file);

                return true;
            } catch (IOException e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Ошибка сохранения");
                errorAlert.setHeaderText("Не удалось сохранить изображение");
                errorAlert.setContentText("Произошла ошибка при сохранении файла: " + e.getMessage());
                errorAlert.showAndWait();
                return false;
            }
        }
        return false;
    }

    private BufferedImage convertToBufferedImage(WritableImage writableImage) {
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = writableImage.getPixelReader().getColor(x, y);
                int argb = (int) (color.getOpacity() * 255) << 24 |
                        (int) (color.getRed() * 255) << 16 |
                        (int) (color.getGreen() * 255) << 8 |
                        (int) (color.getBlue() * 255);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        return bufferedImage;
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return null;
        }
        return name.substring(lastIndexOf);
    }

    private void markUnsavedChanges() {
        this.hasUnsavedChanges = true;
    }

    @FXML
    public void initialize() {
        drawingCanvas = new DrawingCanvas(canvas, repository);
        setupMouseHandlers();
        setupStyleButtons();
        setupZoomHandlers();
        setupToolsListView();

        outlineColorPicker.setValue(outlineColor);
        fillColorPicker.setValue(fillColor);

        outlineColorPicker.setOnAction(e -> {
            outlineColor = outlineColorPicker.getValue();
            Shape selectedShape = drawingCanvas.getSelectedShape();
            if (selectedShape != null) {
                repository.saveState();
                if (hasOutline) {
                    selectedShape.setStrokeColor(outlineColor);
                    drawingCanvas.redrawAllShapes();
                    welcomeText.setText("Цвет контура выделенной фигуры изменен");
                    markUnsavedChanges();
                } else {
                    welcomeText.setText("Контур отключен. Включите контур для изменения цвета.");
                }
            } else {
                welcomeText.setText("Цвет контура установлен для новых фигур");
            }
        });

        fillColorPicker.setOnAction(e -> {
            fillColor = fillColorPicker.getValue();
            Shape selectedShape = drawingCanvas.getSelectedShape();
            if (selectedShape != null) {
                repository.saveState();
                if (hasFill) {
                    selectedShape.setFillColor(fillColor);
                    drawingCanvas.redrawAllShapes();
                    welcomeText.setText("Цвет заливки выделенной фигуры изменен");
                    markUnsavedChanges();
                } else {
                    welcomeText.setText("Заливка отключена. Включите заливку для изменения цвета.");
                }
            } else {
                welcomeText.setText("Цвет заливки установлен для новых фигур");
            }
        });

    }

    private void setupToolsListView() {
        toolsListView.getItems().addAll(
                "Прямоугольник",
                "Эллипс",
                "Линия",
                "Многоугольник"
        );

        toolsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                switch (newValue) {
                    case "Прямоугольник":
                        currentTool = "rectangle";
                        welcomeText.setText("Инструмент: Прямоугольник");
                        break;
                    case "Эллипс":
                        currentTool = "ellipse";
                        welcomeText.setText("Инструмент: Эллипс");
                        break;
                    case "Линия":
                        currentTool = "line";
                        welcomeText.setText("Инструмент: Линия");
                        break;
                    case "Многоугольник":
                        currentTool = "polygon";
                        welcomeText.setText("Инструмент: Многоугольник");
                        break;
                }
            }
        });

        toolsListView.getSelectionModel().select(0);
    }

    @FXML
    protected void onSelectToolClick() {
        currentTool = "select";
        welcomeText.setText("Инструмент: Выделение");

        toolsListView.getSelectionModel().clearSelection();
    }

    public void setupAfterSceneReady() {
        setupKeyboardHandlers();
    }

    private void setupKeyboardHandlers() {
        canvas.getScene().setOnKeyPressed(this::handleKeyPressed);
    }

    private void setupZoomHandlers() {
        canvas.setOnScroll(this::handleScroll);
    }

    private void handleScroll(ScrollEvent event) {
        if (event.isControlDown()) {
            double zoomFactor = event.getDeltaY() > 0 ? 0.1 : -0.1;
            drawingCanvas.zoom(zoomFactor, event.getX(), event.getY());
            welcomeText.setText(String.format("Масштаб: %.0f%%", drawingCanvas.getScale() * 100));
            event.consume();
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.isControlDown()) {
            switch (event.getCode()) {
                case Z:
                    undo();
                    event.consume();
                    break;
                case EQUALS:
                case PLUS:
                    drawingCanvas.zoom(0.1, canvas.getWidth() / 2, canvas.getHeight() / 2);
                    welcomeText.setText(String.format("Масштаб увеличен: %.0f%%", drawingCanvas.getScale() * 100));
                    event.consume();
                    break;
                case MINUS:
                    drawingCanvas.zoom(-0.1, canvas.getWidth() / 2, canvas.getHeight() / 2);
                    welcomeText.setText(String.format("Масштаб уменьшен: %.0f%%", drawingCanvas.getScale() * 100));
                    event.consume();
                    break;
                case R:
                    drawingCanvas.resetView();
                    welcomeText.setText("Вид сброшен к масштабу 100%");
                    event.consume();
                    break;
                case O:
                    drawingCanvas.pan(30, 0);
                    event.consume();
                    break;
                case P:
                    drawingCanvas.pan(-30, 0);
                    event.consume();
                    break;
                case S:
                    if (saveToFile()) {
                        hasUnsavedChanges = false;
                    }
                    event.consume();
                    break;
            }
        } else {
            switch (event.getCode()) {
                case UP:
                    drawingCanvas.pan(0, 30);
                    event.consume();
                    break;
                case DOWN:
                    drawingCanvas.pan(0, -30);
                    event.consume();
                    break;
                case LEFT:
                    drawingCanvas.pan(30, 0);
                    event.consume();
                    break;
                case RIGHT:
                    drawingCanvas.pan(-30, 0);
                    event.consume();
                    break;
                case DELETE:
                    onDeleteButtonClick();
                    event.consume();
                    break;
            }
        }
    }

    private void undo() {
        repository.undo();
        drawingCanvas.redrawAllShapes();
        if (drawingCanvas.getSelectedShape() != null) {
            drawingCanvas.getSelectedShape().setSelected(false);
        }
        markUnsavedChanges();
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
        if (isPanning) {
            canvas.setCursor(Cursor.CLOSED_HAND);
        } else if (currentTool.equals("select") && drawingCanvas.getSelectedShape() != null) {
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
                    if (drawingCanvas.getSelectedShape().contains(drawingCanvas.toModelX(event.getX()), drawingCanvas.toModelY(event.getY()))) {
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

        if (event.isMiddleButtonDown() || (event.isPrimaryButtonDown() && event.isShiftDown())) {
            isPanning = true;
            panStartX = event.getX();
            panStartY = event.getY();
            canvas.setCursor(Cursor.CLOSED_HAND);
            return;
        }

        if (currentTool.equals("select")) {
            Shape.ResizeType resizeType = drawingCanvas.getResizeTypeAt(startX, startY);
            if (resizeType != Shape.ResizeType.NONE) {
                isResizing = true;
                drawingCanvas.setCurrentResizeType(resizeType);
                repository.saveState();
            } else {
                Shape selected = drawingCanvas.selectShapeAt(startX, startY);
                isMoving = (selected != null);
                if (isMoving) {
                    repository.saveState();
                }
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

        if (isPanning) {
            double panDeltaX = currentX - panStartX;
            double panDeltaY = currentY - panStartY;
            drawingCanvas.pan(panDeltaX, panDeltaY);
            panStartX = currentX;
            panStartY = currentY;
        } else if (isMoving && currentTool.equals("select")) {
            double modelDeltaX = deltaX / drawingCanvas.getScale();
            double modelDeltaY = deltaY / drawingCanvas.getScale();
            drawingCanvas.moveSelectedShape(modelDeltaX, modelDeltaY);
            startX = currentX;
            startY = currentY;
        } else if (isResizing && currentTool.equals("select")) {
            double modelX = drawingCanvas.toModelX(currentX);
            double modelY = drawingCanvas.toModelY(currentY);
            drawingCanvas.resizeSelectedShape(drawingCanvas.getCurrentResizeType(), modelX, modelY);
        } else if (isDrawing) {
            if (currentTool.equals("line")) {
                double modelStartX = drawingCanvas.toModelX(startX);
                double modelStartY = drawingCanvas.toModelY(startY);
                double modelCurrentX = drawingCanvas.toModelX(currentX);
                double modelCurrentY = drawingCanvas.toModelY(currentY);
                previewShape = createLinePreview(modelStartX, modelStartY, modelCurrentX, modelCurrentY);
            } else {
                double modelX1 = drawingCanvas.toModelX(startX);
                double modelY1 = drawingCanvas.toModelY(startY);
                double modelX2 = drawingCanvas.toModelX(currentX);
                double modelY2 = drawingCanvas.toModelY(currentY);

                double x = Math.min(modelX1, modelX2);
                double y = Math.min(modelY1, modelY2);
                double width = Math.abs(modelX2 - modelX1);
                double height = Math.abs(modelY2 - modelY1);
                previewShape = createShapePreview(x, y, width, height);
            }

            if (previewShape != null) {
                drawingCanvas.drawPreview(previewShape);
            }
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        double endX = event.getX();
        double endY = event.getY();

        if (isPanning) {
            isPanning = false;
            canvas.setCursor(Cursor.DEFAULT);
        } else if (isDrawing) {
            isDrawing = false;

            if (currentTool.equals("line")) {
                double modelStartX = drawingCanvas.toModelX(startX);
                double modelStartY = drawingCanvas.toModelY(startY);
                double modelEndX = drawingCanvas.toModelX(endX);
                double modelEndY = drawingCanvas.toModelY(endY);

                Shape finalShape = createLineFinal(modelStartX, modelStartY, modelEndX, modelEndY);
                if (finalShape != null) {
                    drawingCanvas.addShape(finalShape);
                    markUnsavedChanges();
                }
            } else {
                double modelX1 = drawingCanvas.toModelX(startX);
                double modelY1 = drawingCanvas.toModelY(startY);
                double modelX2 = drawingCanvas.toModelX(endX);
                double modelY2 = drawingCanvas.toModelY(endY);

                double x = Math.min(modelX1, modelX2);
                double y = Math.min(modelY1, modelY2);
                double width = Math.abs(modelX2 - modelX1);
                double height = Math.abs(modelY2 - modelY1);

                Shape finalShape = createShapeFinal(x, y, width, height);
                if (finalShape != null) {
                    drawingCanvas.addShape(finalShape);
                    markUnsavedChanges();
                }
            }
        }

        isMoving = false;
        isResizing = false;
        drawingCanvas.setCurrentResizeType(Shape.ResizeType.NONE);
    }

    private Shape createLinePreview(double startX, double startY, double endX, double endY) {
        Color strokeColor = hasOutline ? Color.GRAY : Color.TRANSPARENT;
        return new Line(startX, startY, endX, endY, strokeColor);
    }

    private Shape createLineFinal(double startX, double startY, double endX, double endY) {
        double distance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
        if (distance < 5) {
            return null;
        }

        Color strokeColor = hasOutline ? outlineColor : Color.TRANSPARENT;
        return new Line(startX, startY, endX, endY, strokeColor);
    }

    private Shape createShapePreview(double x, double y, double width, double height) {
        Color strokeColor = hasOutline ? Color.GRAY : Color.TRANSPARENT;
        Color fillColor = hasFill ? Color.LIGHTGRAY : Color.TRANSPARENT;

        switch (currentTool) {
            case "rectangle":
                return new Rectangle(x, y, width, height, strokeColor, fillColor);
            case "ellipse":
                return new Ellipse(x, y, width, height, strokeColor, fillColor);
            case "polygon":
                return new Polygon(x, y, width, height, strokeColor, fillColor);
            default:
                return null;
        }
    }

    private Shape createShapeFinal(double x, double y, double width, double height) {
        if (width < 5 || height < 5) {
            return null;
        }

        Color strokeColor = hasOutline ? outlineColor : Color.TRANSPARENT;
        Color fillColor = hasFill ? this.fillColor : Color.TRANSPARENT;

        switch (currentTool) {
            case "rectangle":
                return new Rectangle(x, y, width, height, strokeColor, fillColor);
            case "ellipse":
                return new Ellipse(x, y, width, height, strokeColor, fillColor);
            case "polygon":
                return new Polygon(x, y, width, height, strokeColor, fillColor);
            default:
                return null;
        }
    }

    @FXML
    protected void onDeleteButtonClick() {
        Shape selectedShape = drawingCanvas.getSelectedShape();
        if (selectedShape != null) {
            repository.saveState();
            drawingCanvas.deleteSelectedShape();
            welcomeText.setText("Фигура удалена");
            if (statusText != null) {
                statusText.setText("Фигура удалена");
            }
            markUnsavedChanges();
        } else {
            welcomeText.setText("Нет выделенной фигуры для удаления");
        }
    }

    @FXML
    protected void onClearButtonClick() {
        repository.saveState();
        repository.clear();
        drawingCanvas.redrawAllShapes();
        welcomeText.setText("Холст очищен");
        if (statusText != null) {
            statusText.setText("Холст очищен");
        }
        markUnsavedChanges();
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