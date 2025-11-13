package com.example.paintoop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class LocalRepository implements Repository {
    private List<Shape> shapes = new ArrayList<>();
    private Deque<List<Shape>> history = new ArrayDeque<>();
    private static final int MAX_HISTORY_SIZE = 5;
    private static final String HISTORY_FILE = "history.json";
    private ObjectMapper objectMapper;

    public LocalRepository() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        loadHistoryFromFile();
    }

    @Override
    public void addShape(Shape shape) {
        saveState();
        shapes.add(shape);
        saveHistoryToFile();
    }

    @Override
    public void removeShape(Shape shape) {
        saveState();
        shapes.remove(shape);
        saveHistoryToFile();
    }

    @Override
    public List<Shape> getAllShapes() {
        return new ArrayList<>(shapes);
    }

    @Override
    public void clear() {
        saveState();
        shapes.clear();
        saveHistoryToFile();
    }

    @Override
    public void undo() {
        if (!history.isEmpty()) {
            List<Shape> previousState = history.pop();
            shapes.clear();
            shapes.addAll(previousState);
            saveHistoryToFile();
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
        saveHistoryToFile();
    }

    @Override
    public void bringToFront(Shape shape) {
        if (shapes.contains(shape)) {
            shapes.remove(shape);
            shapes.add(shape);
            saveState();
        }
    }

    public void clearPersistentData() {
        history.clear();
        shapes.clear();
        File file = new File(HISTORY_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    private void saveHistoryToFile() {
        try {
            RepositoryState repositoryState = new RepositoryState(new ArrayList<>(history), new ArrayList<>(shapes));
            objectMapper.writeValue(new File(HISTORY_FILE), repositoryState);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void loadHistoryFromFile() {
        try {
            File file = new File(HISTORY_FILE);
            if (file.exists()) {
                RepositoryState repositoryState = objectMapper.readValue(file, RepositoryState.class);
                history.clear();
                history.addAll(repositoryState.getHistory());
                shapes.clear();
                shapes.addAll(repositoryState.getCurrentShapes());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static class RepositoryState {
        private List<List<Shape>> history;
        private List<Shape> currentShapes;

        public RepositoryState(List<List<Shape>> history, List<Shape> currentShapes) {
            this.history = history;
            this.currentShapes = currentShapes;
        }

        public List<List<Shape>> getHistory() {
            return history;
        }

        public List<Shape> getCurrentShapes() {
            return currentShapes;
        }
    }
}