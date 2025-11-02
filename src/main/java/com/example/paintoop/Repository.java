package com.example.paintoop;

import java.util.List;

public interface Repository {
    void addShape(Shape shape);
    void removeShape(Shape shape);
    List<Shape> getAllShapes();
    void clear();
}