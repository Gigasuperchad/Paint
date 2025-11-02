package com.example.paintoop;

import java.util.ArrayList;
import java.util.List;

public class LocalRepository implements Repository {
    private List<Shape> shapes = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void addShape(Shape shape) {
        shapes.add(shape);
    }

    @Override
    public void removeShape(Shape shape) {
        shapes.remove(shape);
    }

    @Override
    public List<Shape> getAllShapes() {
        return new ArrayList<>(shapes);
    }

    @Override
    public void clear() {
        shapes.clear();
    }
}