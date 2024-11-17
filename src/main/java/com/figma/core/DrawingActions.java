package com.figma.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

abstract public class DrawingActions implements Cloneable{
    DrawingTool tool;
    Color color;
    double strokeWidth;
    Rectangle selectionBounds;
    boolean isSelected;
    DrawingActions(DrawingTool tool, Color color,double strokeWidth){
        this.tool = tool;
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.selectionBounds = new Rectangle();
        this.isSelected = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DrawingActions that = (DrawingActions) obj;

        // Compare the selection bounds
        return this.selectionBounds.getX() == that.selectionBounds.getX() &&
                this.selectionBounds.getY() == that.selectionBounds.getY() &&
                this.selectionBounds.getWidth() == that.selectionBounds.getWidth() &&
                this.selectionBounds.getHeight() == that.selectionBounds.getHeight();
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    abstract void draw(GraphicsContext gc);
    abstract void setSelectionBound();
    abstract void move(double deltaX, double deltaY);
}
class Pen extends DrawingActions {
    List<Double> points;

    Pen(DrawingTool tool, Color color, double strokeWidth) {
        super(tool, color, strokeWidth);
        points = new ArrayList<Double>();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Pen cloned = (Pen) super.clone();
        cloned.points = new ArrayList<>(this.points);
        cloned.color = this.color;
        cloned.strokeWidth = this.strokeWidth;
        return cloned;
    }

    @Override
    void draw(GraphicsContext gc) {
        gc.save();
        if (points.size() >= 4) {
            gc.setStroke(color);
            gc.setLineWidth(strokeWidth);
            for (int i = 2; i < points.size(); i += 2) {
                gc.strokeLine(points.get(i - 2), points.get(i - 1), points.get(i), points.get(i + 1));
            }
            if(isSelected){
                setSelectionBound();
                gc.setStroke(Color.BLUE);
                gc.setLineWidth(1.5);
                gc.strokeRect(
                        selectionBounds.getX() - 5,
                        selectionBounds.getY() - 5,
                        selectionBounds.getWidth() + 10,
                        selectionBounds.getHeight() + 10
                );
            }
            gc.restore();
        }
    }

    @Override
    void setSelectionBound(){
        if (points.isEmpty()) return;

        double minX = points.get(0);
        double minY = points.get(1);
        double maxX = minX;
        double maxY = minY;

        for (int i = 2; i < points.size(); i += 2) {
            double x = points.get(i);
            double y = points.get(i + 1);
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        selectionBounds.setX(minX);
        selectionBounds.setY(minY);
        selectionBounds.setWidth(maxX - minX);
        selectionBounds.setHeight(maxY - minY);
    }

    @Override
    void move(double deltaX, double deltaY) {
        for (int i = 0; i < points.size(); i += 2) {
            points.set(i, points.get(i) + deltaX);
            points.set(i + 1, points.get(i + 1) + deltaY);
        }
        setSelectionBound();
    }

}

class RectangleShape extends DrawingActions {
    double x, y, width, height;

    RectangleShape(DrawingTool tool, Color color, double strokeWidth) {
        super(tool, color, strokeWidth);
    }

    public void setDimensions(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    void draw(GraphicsContext gc) {
        gc.save();
        gc.setStroke(color);
        gc.setLineWidth(strokeWidth);
        gc.strokeRect(x, y, width, height);

        if (isSelected) {
            setSelectionBound();
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1.5);
            gc.strokeRect(
                    selectionBounds.getX() - 5,
                    selectionBounds.getY() - 5,
                    selectionBounds.getWidth() + 10,
                    selectionBounds.getHeight() + 10
            );
        }
        gc.restore();
    }

    @Override
    void setSelectionBound() {
        selectionBounds.setX(x - 5);
        selectionBounds.setY(y - 5);
        selectionBounds.setWidth(width + 10);
        selectionBounds.setHeight(height + 10);
    }

    @Override
    void move(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
        setSelectionBound();
    }

    void resize(double x,double y,double newWidth, double newHeight) {
        this.x =x;
        this.y = y;
        width = newWidth;
        height = newHeight;
        setSelectionBound();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        RectangleShape cloned = (RectangleShape) super.clone();
        cloned.x = this.x;
        cloned.y = this.y;
        cloned.width = this.width;
        cloned.height = this.height;
        cloned.color = this.color;
        cloned.strokeWidth = this.strokeWidth;

        return cloned;
    }
}

class CircleShape extends DrawingActions {
    private double centerX, centerY, radius;

    public CircleShape(DrawingTool tool, Color color, double strokeWidth) {
        super(tool, color, strokeWidth);
    }

    public void setProperties(double centerX, double centerY, double radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.setStroke(this.color);
        gc.setLineWidth(strokeWidth);
        gc.strokeOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
        if (isSelected) {
            setSelectionBound();
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1.5);
            gc.strokeRect(
                    selectionBounds.getX() - 5,
                    selectionBounds.getY() - 5,
                    selectionBounds.getWidth() + 10,
                    selectionBounds.getHeight() + 10
            );
        }

        gc.restore();
    }

    @Override
    public void setSelectionBound() {
        selectionBounds.setX(centerX - radius);
        selectionBounds.setY(centerY - radius);
        selectionBounds.setWidth(2 * radius);
        selectionBounds.setHeight(2 * radius);
    }

    @Override
    public void move(double deltaX, double deltaY) {
        centerX += deltaX;
        centerY += deltaY;
        setSelectionBound();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        CircleShape cloned = (CircleShape) super.clone();
        cloned.centerX = this.centerX;
        cloned.centerY = this.centerY;
        cloned.radius = this.radius;
        return cloned;
    }

}


class TextShape extends DrawingActions {
    private String text;
    private double x, y;
    private int fontSize;
    private Color color;

    public TextShape(DrawingTool tool, Color color, int fontSize) {
        super(tool,color,fontSize);
        this.color = color;
        this.fontSize = fontSize;
        this.text = "dfdfgd";
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        System.out.println("Pasted test here "+text);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(20));
        gc.fillText(text, x, y);
        gc.restore();
    }

    @Override
    public void setSelectionBound() {
        // Set selection bounds for the text, if necessary
    }

    @Override
    public void move(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
    }
}

