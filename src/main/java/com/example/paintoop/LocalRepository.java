package com.example.paintoop;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class LocalRepository implements Repository {
    private List<Shape> shapes = new ArrayList<>();
    private Deque<List<Shape>> history = new ArrayDeque<>();
    private static final int MAX_HISTORY_SIZE = 5;

    @Override
    public void addShape(Shape shape) {
        saveState();
        shapes.add(shape);
    }

    @Override
    public void removeShape(Shape shape) {
        saveState();
        shapes.remove(shape);
    }

    @Override
    public List<Shape> getAllShapes() {
        return new ArrayList<>(shapes);
    }

    @Override
    public void clear() {
        saveState();
        shapes.clear();
    }

    @Override
    public void undo() {
        if (!history.isEmpty()) {
            List<Shape> previousState = history.pop();
            shapes.clear();
            shapes.addAll(previousState);
        }
    }

    @Override
    public void saveState() {
        List<Shape> stateCopy = new ArrayList<>();
        for (Shape shape : shapes) {
            stateCopy.add(shape.copy());
        }

        history.push(stateCopy);

        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeLast();
        }
    }
}