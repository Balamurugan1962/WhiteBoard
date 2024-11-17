package com.figma.core;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Stack;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class Whiteboard {
    private static Stack<ArrayList<DrawingActions>> undoHistory;
    private static Stack<ArrayList<DrawingActions>> redoHistory;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private DrawingTool currentTool;
    private Mode currentMode;
    private static ArrayList<DrawingActions> actionHistory;
    private DrawingActions currentAction;
    private double lastMouseX, lastMouseY;
    private double startX, startY, stopX, stopY;
    private Color currentColor = Color.BLACK;
    private double strokeWidth = 5;
    private String currentText = "";
    private int currentFontSize = 14;

    public Whiteboard() {
        this.undoHistory = new Stack<ArrayList<DrawingActions>>();
        this.redoHistory = new Stack<ArrayList<DrawingActions>>();
        this.canvas = new Canvas(1200, 800);
        this.gc = canvas.getGraphicsContext2D();
        this.actionHistory = new ArrayList<>();
        this.currentMode = Mode.DRAW;
        this.currentTool = null;
        setupKeyHandler();
        gc.setFill(Color.WHITE);
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
        if (currentAction != null) {
            currentAction.color = currentColor;
            drawCanvas();
        }
    }

    public void setCurrentStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
        if (currentAction != null) {
            currentAction.strokeWidth = strokeWidth;
            drawCanvas();
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCurrentTool(DrawingTool tool) {
        currentTool = tool;
        currentMode = Mode.DRAW;
    }

    public void setCurrentMode(Mode mode) {
        currentMode = mode;
    }

    public Mode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentAction(boolean val) {
        if (currentAction != null) {
            currentAction.isSelected = val;
        }
    }

    public DrawingTool getCurrentTool() {
        return currentTool;
    }

    public ArrayList<DrawingActions> getActionHistory() {
        return actionHistory;
    }

    public void setActionHistory(ArrayList<DrawingActions> actionHistory) {
        this.actionHistory = actionHistory;
    }

    public void setTextContent(String text) {
        currentText = text;
        System.out.println(text);
//        if(currentAction!=null && currentAction.tool == DrawingTool.TEXT)
//            ((TextShape)(currentAction)).setText(text);
    }

    public void setTextFontSize(int fontSize) {
        currentFontSize = fontSize;
    }

    private void handleSelection(MouseEvent e) {
        currentAction = findElementAtPoint(e.getX(), e.getY());
        actionHistory.forEach(action -> action.isSelected = false);

        if (currentAction != null) {
            currentAction.isSelected = true;
        }

        drawCanvas();
    }

    private void handlePenDrawing(MouseEvent e) {
        ((Pen) currentAction).points.add(e.getX());
        ((Pen) currentAction).points.add(e.getY());
    }

    private DrawingActions findElementAtPoint(double x, double y) {
        return actionHistory.stream()
                .filter(action -> action.selectionBounds.contains(x, y))
                .findFirst()
                .orElse(null);
    }

    public void handleMousePressed(MouseEvent e) {
        if (currentTool == null) {
            currentMode = Mode.SELECT;
        }
        canvas.requestFocus();
        if (currentMode == Mode.DRAW) {
            switch (currentTool) {
                case PEN:
                    currentAction = new Pen(DrawingTool.PEN, currentColor, strokeWidth);
                    ((Pen) currentAction).points.add(e.getX());
                    ((Pen) currentAction).points.add(e.getY());
                    break;
                case RECTANGLE:
                    currentAction = new RectangleShape(DrawingTool.RECTANGLE, currentColor, strokeWidth);
                    startX = e.getX();
                    startY = e.getY();
                    ((RectangleShape) currentAction).setDimensions(
                            min(e.getX(), startX), min(e.getY(), startY),
                            abs(e.getX() - startX), abs(e.getY() - startY)
                    );
                    break;
                case CIRCLE:
                    currentAction = new CircleShape(DrawingTool.CIRCLE, currentColor, strokeWidth);
                    startX = e.getX();
                    startY = e.getY();
                    break;
//                case TEXT:
//                    currentAction = new TextShape(DrawingTool.TEXT, currentColor, currentFontSize);
//                    startX = e.getX();
//                    startY = e.getY();
//                    ((TextShape)currentAction).setPosition(startX,startY);
//                    break;
                default:
                    System.out.println("Unhandled tool: " + currentTool);
            }
        } else {
            handleSelection(e);
        }
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }

    public void handleMouseDragged(MouseEvent e) {
        if (currentMode == Mode.DRAW) {
            switch (currentTool) {
                case PEN:
                    ((Pen) currentAction).points.add(e.getX());
                    ((Pen) currentAction).points.add(e.getY());
                    break;
                case RECTANGLE:
                    ((RectangleShape) currentAction).setDimensions(
                            min(e.getX(), startX), min(e.getY(), startY),
                            abs(e.getX() - startX), abs(e.getY() - startY)
                    );
                    break;
                case CIRCLE:
                    double radius = Math.sqrt(Math.pow(e.getX() - startX, 2) + Math.pow(e.getY() - startY, 2));
                    ((CircleShape) currentAction).setProperties(startX, startY, radius);
                    break;
            }
        } else {
            moveSelectedAction(e);
        }

        drawCanvas();
        if (currentAction != null) {
            currentAction.draw(gc);
        }
    }

    private void moveSelectedAction(MouseEvent e) {
        double deltaX = e.getX() - lastMouseX;
        double deltaY = e.getY() - lastMouseY;
        lastMouseX = e.getX();
        lastMouseY = e.getY();

        if (currentAction != null) {
            currentAction.move(deltaX, deltaY);
        }
    }

    public void handleMouseReleased(MouseEvent e) {
        if (currentMode == Mode.DRAW && currentAction != null) {
            try {
                finalizeDrawingAction();
            } catch (Exception x) {
                System.out.println(x);
            }
            redoHistory.clear();
            undoHistory.push((ArrayList<DrawingActions>) actionHistory.clone());
        } else if(currentAction!=null) {
            System.out.println("Clicked");
            if (currentAction != null) {
                actionHistory.remove(currentAction);
                try {
                    actionHistory.add((DrawingActions) currentAction.clone());
                } catch (Exception x) {
                    System.out.println(x);
                }
                redoHistory.clear();
                undoHistory.push((ArrayList<DrawingActions>) actionHistory.clone());
            }
        }
        drawCanvas();

    }

    private void finalizeDrawingAction() throws CloneNotSupportedException {
        currentAction.setSelectionBound();
        DrawingActions a = (DrawingActions) currentAction.clone();
        actionHistory.add(a);
        currentAction = null;
    }

    public void drawCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        actionHistory.forEach(action -> action.draw(gc));
    }

    private void setupKeyHandler() {
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed((KeyEvent e) -> {
            if (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE) {
                if (currentAction != null && currentAction.isSelected) {
                    actionHistory.remove(currentAction);
                    currentAction = null;
                    drawCanvas();
                    System.out.println("Selected shape deleted.");
                }
            }
            if(e.getCode() == KeyCode.Z){
                this.undo();
                System.out.println("Undo");
                drawCanvas();
            }
        });
        canvas.setFocusTraversable(true);
    }

    public void undo() {
        redoHistory.push((ArrayList<DrawingActions>) actionHistory.clone());
        if (undoHistory.size()>1) {
            undoHistory.pop();
            actionHistory = cloneActionHistory((ArrayList<DrawingActions>) undoHistory.peek());
            System.out.println("Run");
        }else{
            undoHistory.clear();
            actionHistory = new ArrayList<>();

        }
        drawCanvas();
        System.out.println(undoHistory);
        System.out.println(actionHistory);
    }

    public void redo() {
        if (!redoHistory.isEmpty()) {
            actionHistory = cloneActionHistory((ArrayList<DrawingActions>) redoHistory.pop());
            drawCanvas();
        }
        System.out.println(redoHistory);
    }

    private ArrayList<DrawingActions> cloneActionHistory(ArrayList<DrawingActions> history) {
        ArrayList<DrawingActions> clonedHistory = new ArrayList<>();
        for (DrawingActions action : history) {
            try {
                clonedHistory.add((DrawingActions) action.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return clonedHistory;
    }
}