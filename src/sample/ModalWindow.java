package sample;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;

public class ModalWindow {
    private int rows, cols;
    MainWindow mainWindow;

    public ModalWindow(MainWindow mainWindow) {
        rows = 0;
        cols = 0;
        this.mainWindow = mainWindow;
        Stage stage = new Stage();
        stage.setScene(initScene(stage));
        stage.setTitle("Custom size");
        stage.sizeToScene();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private Scene initScene(Stage stage) {
        Group root = new Group();
        double margin = 40;

        Label rowsL = createLabel("Rows:", 30, 100, Pos.CENTER_LEFT, margin, margin / 2, null, new Font(15));
        Label colsL = createLabel("Columns:", 30, 100, Pos.CENTER_LEFT, margin, margin + rowsL.getPrefHeight(), null, new Font(15));

        TextField rowsTF = createTextField(rowsL, margin);
        TextField colsTF = createTextField(colsL, margin);

        Button ok = new Button("OK");
        ok.setPrefWidth(60);
        ok.setPrefHeight(30);
        ok.setLayoutY(margin * 1.5 + rowsL.getPrefHeight() + colsL.getPrefHeight());
        ok.setLayoutX((margin * 2 + rowsL.getPrefWidth() + rowsTF.getPrefWidth() - ok.getPrefWidth()) / 2);
        ok.setAlignment(Pos.CENTER);
        ok.setOnAction(event -> {
            if (rowsTF.getText().isEmpty() || colsTF.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Enter meshes");
                return;
            }
            if (Integer.parseInt(rowsTF.getText()) < 5 || Integer.parseInt(colsTF.getText()) < 5) {
                JOptionPane.showMessageDialog(null, "Enter meshes > 5");
                return;
            }
            if (Integer.parseInt(rowsTF.getText()) > 18 || Integer.parseInt(colsTF.getText()) > 38) {
                JOptionPane.showMessageDialog(null, "Enter meshes less than 20x40");
                return;
            }
            rows = Integer.parseInt(rowsTF.getText());
            cols = Integer.parseInt(colsTF.getText());
            mainWindow.changeLevel("custom", rows, cols);
            stage.hide();
        });

        root.getChildren().addAll(rowsL, rowsTF, colsL, colsTF, ok);
        return new Scene(root, margin*2 + rowsL.getPrefWidth() + rowsTF.getPrefWidth(),
                margin * 4 / 2 + rowsL.getPrefHeight() + colsL.getPrefHeight() + ok.getPrefHeight());
    }

    private Label createLabel(String name, double prefHeight, double prefWidth, Pos alignment, double x, double y, String style, Font font) {
        Label label = new Label(name);
        label.setPrefHeight(prefHeight);
        label.setPrefWidth(prefWidth);
        label.setAlignment(alignment);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setStyle(style);
        label.setFont(font);
        return label;
    }

    private TextField createTextField(Label label, double margin) {
        TextField textField = new TextField();
        textField.setFont(label.getFont());
        textField.setPrefHeight(label.getPrefHeight());
        textField.setPrefWidth(70);
        textField.setLayoutX(margin + label.getPrefWidth());
        textField.setLayoutY(label.getLayoutY());
        return textField;
    }
}
