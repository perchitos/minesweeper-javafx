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
    MainWindow mainWindow;

    public ModalWindow(MainWindow mainWindow) {
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
        Label bombsL = createLabel("Bombs:", 30, 100, Pos.CENTER_LEFT, margin, margin*1.5 + rowsL.getPrefHeight() + colsL.getPrefHeight(), null, new Font(15));

        TextField rowsTF = createTextField(rowsL, margin);  rowsTF.setText("10");
        TextField colsTF = createTextField(colsL, margin);  colsTF.setText("15");
        TextField bombsTF = createTextField(bombsL, margin);    bombsTF.setText("20");

        Label rowsL2 = createLabel("5..18", 30, 50, Pos.CENTER_LEFT, margin*2 + rowsL.getPrefWidth() + rowsTF.getPrefWidth(), rowsL.getLayoutY(), null, new Font(15));
        Label colsL2 = createLabel("5..38", 30, 50, Pos.CENTER_LEFT, margin*2 + colsL.getPrefWidth() + colsTF.getPrefWidth(), colsL.getLayoutY(), null, new Font(15));
        Label bombsL2 = createLabel("10.." + (10*15), 30, 50, Pos.CENTER_LEFT, margin*2 + bombsL.getPrefWidth() + bombsTF.getPrefWidth(), bombsL.getLayoutY(), null, new Font(15));

        rowsTF.focusedProperty().addListener(event -> {
            calculatePrefBombs(Integer.parseInt(rowsTF.getText()), Integer.parseInt(colsTF.getText()), bombsTF, bombsL2);
        });
        colsTF.focusedProperty().addListener(event -> {
            calculatePrefBombs(Integer.parseInt(rowsTF.getText()), Integer.parseInt(colsTF.getText()), bombsTF, bombsL2);
        });

        Button ok = new Button("OK");
        ok.setPrefWidth(60);
        ok.setPrefHeight(30);
        ok.setLayoutY(margin * 2 + rowsL.getPrefHeight() + colsL.getPrefHeight() + bombsL.getPrefHeight());
        ok.setLayoutX((margin * 3 + rowsL.getPrefWidth() + rowsTF.getPrefWidth() + rowsL2.getPrefWidth() - ok.getPrefWidth()) / 2);
        ok.setAlignment(Pos.CENTER);
        ok.setOnAction(event -> {
            if (rowsTF.getText().isEmpty() || colsTF.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Enter meshes");
                return;
            }
            if (Integer.parseInt(rowsTF.getText()) < 5 || Integer.parseInt(rowsTF.getText()) > 18) {
                JOptionPane.showMessageDialog(null, "Count of rows must be between 5 and 18");
                return;
            }
            if (Integer.parseInt(colsTF.getText()) < 5 || Integer.parseInt(colsTF.getText()) > 38) {
                JOptionPane.showMessageDialog(null, "Count of columns must be between 5 and 38");
                return;
            }
            if (Integer.parseInt(bombsTF.getText()) < 10 || Integer.parseInt(bombsTF.getText()) >
                    Integer.parseInt(rowsTF.getText()) * Integer.parseInt(colsTF.getText())) {
                JOptionPane.showMessageDialog(null, "Count of bombs must be between 10 and " +
                        Integer.parseInt(rowsTF.getText()) * Integer.parseInt(colsTF.getText()));
                return;
            }
            mainWindow.changeLevel("custom", Integer.parseInt(rowsTF.getText()), Integer.parseInt(colsTF.getText()), Integer.parseInt(bombsTF.getText()));
            stage.hide();
        });

        root.getChildren().addAll(rowsL, rowsTF, rowsL2, colsL, colsTF, colsL2, bombsL, bombsTF, bombsL2, ok);
        return new Scene(root, margin*3 + rowsL.getPrefWidth() + rowsTF.getPrefWidth() + rowsL2.getPrefWidth(),
                margin * 5 / 2 + rowsL.getPrefHeight() + colsL.getPrefHeight() + bombsL.getPrefHeight() + ok.getPrefHeight());
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

    private void calculatePrefBombs(int rows, int cols, TextField bombsTF, Label bombsL) {
        bombsTF.setText("" + (int)((double)(rows * cols) / 7));
        bombsL.setText("5.." + rows * cols);
    }
}
