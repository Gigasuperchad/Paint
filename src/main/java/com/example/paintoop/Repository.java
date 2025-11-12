package com.example.paintoop;

import java.util.List;

public interface Repository {
    void addShape(Shape shape);
    void removeShape(Shape shape);
    List<Shape> getAllShapes();
    void clear();
    void undo();
    void saveState();
    void clearPersistentData();
    void bringToFront(Shape shape);
}