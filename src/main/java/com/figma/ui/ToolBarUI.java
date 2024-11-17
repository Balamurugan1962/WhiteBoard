package com.figma.ui;

import com.figma.core.*;
import com.figma.core.Mode;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import javafx.scene.text.Font;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;

public class ToolBarUI {
    private VBox toolBar;
    private ArrayList<ToggleButton> toolButtons = new ArrayList<>();
    private Whiteboard whiteboard;

    public ToolBarUI(Whiteboard whiteboard) {
        this.whiteboard = whiteboard;
        toolBar = new VBox(10); // Set spacing between elements in the toolbar
        toolBar.setStyle("-fx-padding: 5px;"); // Add padding around the toolbar
        toolBar.setAlignment(javafx.geometry.Pos.TOP_LEFT); // Align items to the top left
    }

    public VBox createToolBar() {
        // Tool buttons for drawing tools (e.g., Pen, Eraser, etc.)
        for (DrawingTool tool : DrawingTool.values()) {
            ToggleButton toolButton = new ToggleButton(tool.name());

            toolButton.setOnAction(e -> {
                if (toolButton.isSelected()) {
                    whiteboard.setCurrentTool(tool);
                    whiteboard.setCurrentAction(false);
                    System.out.println("Switched to tool: " + tool.name());
                    for (ToggleButton button : toolButtons) {
                        if (button != toolButton) {
                            button.setSelected(false);
                        }
                    }
                } else {
                    // Default behavior when no tool is selected
                    whiteboard.setCurrentTool(null);
                    whiteboard.setCurrentMode(Mode.SELECT);
                    System.out.println("Switched to tool: " + whiteboard.getCurrentMode());
                }
            });

            toolButtons.add(toolButton);
        }

        // Add tool buttons to the toolbar with margins
        for (ToggleButton button : toolButtons) {
            VBox.setMargin(button, new Insets(0, 0, 10, 0)); // Set margin between buttons
            toolBar.getChildren().add(button);
        }

        // Color Picker for tool color selection
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(Color.BLACK); // Default color is black
        colorPicker.setOnAction(e -> {
            Color selectedColor = colorPicker.getValue();
            whiteboard.setCurrentColor(selectedColor);  // Set the current drawing color in Whiteboard
            System.out.println("Selected color: " + selectedColor);
        });

        // Add color picker to the toolbar
        VBox.setMargin(colorPicker, new Insets(0, 0, 10, 0)); // Set margin for color picker
        toolBar.getChildren().add(new Label("Color:"));
        toolBar.getChildren().add(colorPicker);

        // Slider for stroke width adjustment
        Slider strokeSlider = new Slider(1, 10, 2);  // Min, Max, Default value
        strokeSlider.setBlockIncrement(1);
        strokeSlider.setMajorTickUnit(1);
        strokeSlider.setMinorTickCount(0);
        strokeSlider.setSnapToTicks(true);
        strokeSlider.setShowTickLabels(true);
        strokeSlider.setShowTickMarks(true);
        strokeSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return String.format("%.0f", object); // Display stroke size as an integer
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string);
            }
        });

        strokeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            whiteboard.setCurrentStrokeWidth(newValue.doubleValue());  // Set the stroke width in Whiteboard
            System.out.println("Selected stroke width: " + newValue);
        });

        // Add stroke slider to the toolbar
        VBox.setMargin(strokeSlider, new Insets(0, 0, 10, 0)); // Set margin for stroke slider
        toolBar.getChildren().add(new Label("Stroke Width:"));
        toolBar.getChildren().add(strokeSlider);

        // Text input field for adding text
        TextField textField = new TextField();
        textField.setPromptText("Enter text here");
        textField.setMinWidth(150);
        textField.setOnKeyPressed((KeyEvent e) -> {
            // Logic for handling text input can be added here if needed
        });

        // VBox.setMargin(textField, new Insets(0, 0, 10, 0));
        // toolBar.getChildren().add(new Label("Text:"));
        // toolBar.getChildren().add(textField);

        // ComboBox for font size selection
        ComboBox<Integer> fontSizeComboBox = new ComboBox<>();
        fontSizeComboBox.getItems().addAll(10, 12, 14, 16, 18, 20, 24, 28, 32);
        fontSizeComboBox.setValue(14); // Default font size
        fontSizeComboBox.setOnAction(e -> {
            Integer selectedSize = fontSizeComboBox.getValue();
            whiteboard.setTextFontSize(selectedSize); // Set the selected font size in Whiteboard
            System.out.println("Selected font size: " + selectedSize);
        });

        // VBox.setMargin(fontSizeComboBox, new Insets(0, 0, 10, 0));
        // toolBar.getChildren().add(new Label("Font Size:"));
        // toolBar.getChildren().add(fontSizeComboBox);

        return toolBar;
    }
}
