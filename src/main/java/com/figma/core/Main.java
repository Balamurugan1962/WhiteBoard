package com.figma.core;

import com.figma.ui.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.util.ArrayList;

public class Main extends Application {

    Whiteboard whiteboard = new Whiteboard();
    ToolBarUI toolBarUI = new ToolBarUI(whiteboard);
    Stage primaryStage;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/core/main.fxml"));
        BorderPane root = loader.load();
        root.setLeft(toolBarUI.createToolBar ());
        root.setCenter(whiteboard.getCanvas());

        whiteboard.getCanvas().setOnMousePressed(this::handleMousePressed);
        whiteboard.getCanvas().setOnMouseDragged(this::handleMouseDragged);
        whiteboard.getCanvas().setOnMouseReleased(this::handleMouseReleased);
//        whiteboard.getCanvas().requestFocus();
        whiteboard.getCanvas().getGraphicsContext2D().fillRect(0, 0, whiteboard.getCanvas().getWidth(), whiteboard.getCanvas().getHeight());
        Scene scene = new Scene(root, 900, 600, Color.gray(1));
        stage.setTitle("Collaborative Whiteboard");
        stage.setScene(scene);
        stage.show();
    }

    private void handleMousePressed(MouseEvent e) {
        whiteboard.handleMousePressed(e);
    }

    private void handleMouseDragged(MouseEvent e) {
        whiteboard.handleMouseDragged(e);
    }

    private void handleMouseReleased(MouseEvent e) {
        whiteboard.handleMouseReleased(e);
    }

    public void saveToFile() {
        if (whiteboard == null) {
            System.err.println("Whiteboard object is null.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Whiteboard");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialized Files", "*.ser"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(whiteboard.getActionHistory());
                System.out.println("Whiteboard saved to " + file.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Error saving file");
            }
        }
    }

    @FXML
    public void loadFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Whiteboard");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialized Files", "*.ser"));
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                System.out.println("Deserialized object: " + obj.getClass().getName());

                if (obj instanceof ArrayList<?>) {
                    ArrayList<?> loadedActions = (ArrayList<?>) obj;
                    if (!loadedActions.isEmpty() && loadedActions.get(0) instanceof DrawingActions) {
                        whiteboard.setActionHistory((ArrayList<DrawingActions>) loadedActions);
                        System.out.println("Whiteboard loaded from " + file.getAbsolutePath());
                    } else {
                        System.err.println("The file does not contain valid drawing actions.");
                    }
                } else {
                    System.err.println("The file does not contain the expected data format. Found: " + obj.getClass().getName());
                }
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                System.out.println("Error loading file");
            }

        }
    }

    @FXML
    public void undo() {
        System.out.println("Undo");
        whiteboard.undo();
        whiteboard.drawCanvas();
    }

    @FXML
    public void redu() {
        System.out.println("redo");
        whiteboard.redo();
        whiteboard.drawCanvas();
    }
}