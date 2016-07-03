package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.util.Duration;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;

public class Main extends Application {

    Stage currentStage;
    Group root;
    Label timeLabel, flagCountLabel;
    ImageView smile;
    int flagCount;
    boolean isEndOfGame = false;
    boolean isHints = true;
    boolean isFirstClickLose = false;
    boolean isFirstClick = true;
    Timeline timeline;
    String chosenLevel = "beginner";
    String resFolder = "resources/";

    public static final int CELL_SIZE = 35;
    public int CELLS_COUNT_HEIGHT = 10;
    public int CELLS_COUNT_WIDTH = 10;
    public static final double MENU_BAR_HEIGHT = 25;

    private double windowWidth = CELL_SIZE * CELLS_COUNT_WIDTH;
    private double windowHeight = CELL_SIZE * CELLS_COUNT_HEIGHT + MENU_BAR_HEIGHT + 45;

    IView[] imageViews;
    Cell[][] cells;

    @Override
    public void start(Stage primaryStage) throws Exception{
        currentStage = primaryStage;
        currentStage.setTitle("Minesweeper");
        currentStage.getIcons().add(new Image(new FileInputStream(resFolder + "icon.png")));
        initImageViews();
        currentStage.setScene(InitScene());
        currentStage.sizeToScene();
        currentStage.setResizable(false);
        currentStage.setOnCloseRequest(event -> {
            if (timeline != null) {
                timeline.stop();
                timeline = null;
            }
            Platform.exit();
            System.exit(0);
        });
        currentStage.show();
    }

    private Scene InitScene(){
        cells = new Cell[CELLS_COUNT_HEIGHT][CELLS_COUNT_WIDTH];
        root = new Group();
        Scene myScene = new Scene(root, windowWidth, windowHeight);
        myScene.setFill(Color.WHITESMOKE);
        initMenu();
        initBombs();
        isFirstClick = true;
        drawEmptyScene();
        return myScene;
    }

    //initialize menu bar and menus
    private void initMenu() {
        //general menu bar
        MenuBar menuBar = new MenuBar();
        menuBar.setPrefWidth(CELL_SIZE * CELLS_COUNT_WIDTH);

        //menus with game settings and choosing the game level
        Menu menuMain, menuInfo;
        menuMain = new Menu("Game");
        menuInfo = new Menu("Info");

        //items new game and exit
        MenuItem newGame, replayGame, exit;
        newGame = new MenuItem("New Game");
        newGame.setOnAction(event -> {
            newGame();
        });
        replayGame = new MenuItem("Replay Game");
        replayGame.setOnAction(event -> {
            replayGame();
        });
        exit = new MenuItem("Exit");
        exit.setOnAction(event -> {
            Platform.exit();
            System.exit(0);
        });

        Menu settings = new Menu("Settings");
        CheckMenuItem cmi_firstClickLose, cmi_hint;
        cmi_firstClickLose = new CheckMenuItem("Allow first click lose");
        cmi_firstClickLose.setSelected(false);
        cmi_firstClickLose.selectedProperty().addListener(listener -> {
            isFirstClickLose = cmi_firstClickLose.isSelected();
        });
        cmi_hint = new CheckMenuItem("Allow hints");
        cmi_hint.setSelected(true);
        cmi_hint.selectedProperty().addListener(listener -> {
            isHints = cmi_hint.isSelected();
        });
        settings.getItems().addAll(cmi_hint, cmi_firstClickLose);

        //choose level //radioMenu
        Menu choose = new Menu("Choose level");
        ToggleGroup toggle = new ToggleGroup();
        RadioMenuItem rmi_beginner = new RadioMenuItem("Beginner");
        rmi_beginner.setToggleGroup(toggle);
        rmi_beginner.selectedProperty().addListener(setChangeListener("beginner", 10, 10, rmi_beginner));

        RadioMenuItem rmi_intermediate = new RadioMenuItem("Intermediate");
        rmi_intermediate.setToggleGroup(toggle);
        rmi_intermediate.selectedProperty().addListener(setChangeListener("intermediate", 10, 20, rmi_intermediate));

        RadioMenuItem rmi_expert = new RadioMenuItem("Expert");
        rmi_expert.setToggleGroup(toggle);
        rmi_expert.selectedProperty().addListener(setChangeListener("expert", 15, 25, rmi_expert));

        //rmi_intermediate.setSelected(true);
        choose.getItems().addAll(rmi_beginner, rmi_intermediate, rmi_expert);
        menuMain.getItems().addAll(newGame, replayGame, choose, new SeparatorMenuItem(), settings, exit);

        menuBar.getMenus().addAll(menuMain);
        root.getChildren().addAll(menuBar);
    }

    private void replayGame() {
        switch (chosenLevel) {
            case "beginner": flagCount = 10; break;
            case "intermediate": flagCount = 25; break;
            case "expert": flagCount = 55; break;
        }
        for (int i = 0; i < CELLS_COUNT_HEIGHT; i++)
            for (int j = 0; j < CELLS_COUNT_WIDTH; j++)
                if (cells[i][j] != null)
                    if (!cells[i][j].name.equals("bomb")
                            && !(cells[i][j].name.equals("right_flag") && cells[i][j].isBombOnFlag)
                            && !cells[i][j].name.equals("explosion"))
                        cells[i][j] = null;
                    else
                        cells[i][j] = new Cell(cloneImage(getImageView("bomb").getImage()), "bomb");
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isEndOfGame = false;
        isFirstClick = true;
        drawEmptyScene();
    }

    //set game level
    private ChangeListener<Boolean> setChangeListener(String level, int height, int width, RadioMenuItem rmi) {
        return new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                CELLS_COUNT_HEIGHT = height;
                CELLS_COUNT_WIDTH = width;
                windowWidth = CELL_SIZE * CELLS_COUNT_WIDTH;
                windowHeight = CELL_SIZE * CELLS_COUNT_HEIGHT + MENU_BAR_HEIGHT + 45;
                chosenLevel = level;
                try {
                    if (timeline != null) {
                        timeline.stop();
                        timeline = null;
                    }
                    isEndOfGame = false;
                    currentStage.setScene(InitScene());
                    currentStage.centerOnScreen();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                rmi.setSelected(true);
            }
        };
    }

    //load pictures
    private void initImageViews() {
        String names[] = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "bomb", "explosion", "right_flag", "wrong_flag", "start", "oops", "win", "field" };
        imageViews = new IView[names.length];
        for (int i = 0; i < names.length; i++) {
            ImageView iv = null;
            try {
                iv = new ImageView(new Image(new FileInputStream(resFolder + names[i] + ".png"), CELL_SIZE, CELL_SIZE, true, true));
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "File '" + names[i] + ".png' not found");
                e.printStackTrace();
                System.exit(1);
            }
            imageViews[i] = new IView(iv, names[i]);
        }
    }

    private ImageView getImageView(String name) {
        for (IView iv : imageViews) {
            if (iv.name.equals(name))
                return iv.iv;
        }
        return null;
    }

    //draw empty scene in the beginning of the game
    private void drawEmptyScene() {
        for (int i = 0; i < CELLS_COUNT_HEIGHT; i++)
            for (int j = 0; j < CELLS_COUNT_WIDTH; j++) {
                root.getChildren().add(createImageView(getImageView("field"),i,j,false));
            }

        smile = cloneImage(getImageView("start").getImage());
        smile.setY(CELL_SIZE * CELLS_COUNT_HEIGHT + MENU_BAR_HEIGHT + 5);
        smile.setX((CELL_SIZE * CELLS_COUNT_WIDTH - smile.getImage().getWidth()) / 2);
        smile.setSmooth(true);
        smile.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            newGame();
        });

        flagCountLabel = LabelBuilder.create()
                .text("Flags:  " + flagCount)
                .prefWidth((CELL_SIZE * CELLS_COUNT_WIDTH - smile.getImage().getWidth()) / 2)
                .prefHeight(smile.getImage().getHeight() + 10)
                .alignment(Pos.CENTER)
                .layoutX(0)
                .layoutY(MENU_BAR_HEIGHT + (CELL_SIZE * CELLS_COUNT_HEIGHT))
                .style("-fx-background-color: whitesmoke")
                .font(new Font("Verdana", 20))
                .build();

        timeLabel = LabelBuilder.create()
                .text("Game time:  00:00")
                .prefWidth(flagCountLabel.getPrefWidth())
                .prefHeight(smile.getImage().getHeight() + 10)
                .alignment(Pos.CENTER)
                .layoutX(flagCountLabel.getPrefWidth() + smile.getImage().getWidth())
                .layoutY(MENU_BAR_HEIGHT + (CELL_SIZE * CELLS_COUNT_HEIGHT))
                .style("-fx-background-color: whitesmoke")
                .font(new Font("Verdana", 20))
                .build();

        if (chosenLevel.equals("beginner")) {
            flagCountLabel.setText("" + flagCount);
            timeLabel.setText("00:00");
        }

        root.getChildren().addAll(timeLabel, smile, flagCountLabel);
    }

    //initialize bombs
    private void initBombs() {
        switch (chosenLevel) {
            case "beginner": flagCount = 10; break;
            case "intermediate": flagCount = 25; break;
            case "expert": flagCount = 55; break;
        }

        Random r = new Random();
        for (int i = 0; i < flagCount; i++) {
            int x, y;
            do {
                x = 1 + r.nextInt(CELLS_COUNT_HEIGHT - 1);
                y = 1 + r.nextInt(CELLS_COUNT_WIDTH - 1);
                if (cells[x][y] == null)
                    break;
            } while (cells[x][y].name.equals("bomb"));
            cells[x][y] = new Cell(cloneImage(getImageView("bomb").getImage()), "bomb");
        }
    }

    //initialize numbers
    private void initNumbers(int i, int j) {
        if (cells[i][j] == null) {
            int count = 0;
            for (int k = i - 1; k <= i + 1; k++) {
                while (k < 0)
                    k++;
                if (k >= CELLS_COUNT_HEIGHT)
                    break;
                for (int l = j - 1; l <= j + 1; l++) {
                    while (l < 0)
                        l++;
                    if (l >= CELLS_COUNT_WIDTH)
                        break;
                    if (cells[k][l] != null) {
                        if (cells[k][l].name.equals("bomb") || cells[k][l].isBombOnFlag)
                            count++;
                    }
                }
            }
            cells[i][j] = setNumber(count);
            if (cells[i][j].name.equals("zero")) {
                for (int k = i - 1; k <= i + 1; k++) {
                    while (k < 0)
                        k++;
                    if (k >= CELLS_COUNT_HEIGHT)
                        break;
                    for (int l = j - 1; l <= j + 1; l++) {
                        while (l < 0)
                            l++;
                        if (l >= CELLS_COUNT_WIDTH)
                            break;
                        initNumbers(k, l);
                    }
                }
            }
        }
    }

    //repaint scene
    private void repaint() {
        for (int i = 0; i < CELLS_COUNT_HEIGHT; i++)
            for (int j = 0; j < CELLS_COUNT_WIDTH; j++) {
                if (cells[i][j] == null || cells[i][j].name.equals("bomb"))
                    root.getChildren().add(createImageView(getImageView("field"),i,j,false));
                else
                    root.getChildren().add(createImageView(cells[i][j].image,i,j,false));
            }
    }
//
//    //per-pixel comparison of images
//    private boolean compareImages(Image image_1, Image image_2) {
//        PixelReader pixelReader_1 = image_1.getPixelReader();
//        PixelReader pixelReader_2 = image_2.getPixelReader();
//
//        int width_1 = (int)image_1.getWidth();
//        int width_2 = (int)image_2.getWidth();
//        if (width_1 != width_2) {
//            return false;
//        }
//        int height_1 = (int)image_1.getHeight();
//        int height_2 = (int)image_2.getHeight();
//        if (height_1 != height_2) {
//            return false;
//        }
//
//        for (int y = 0; y < height_1; y++){
//            for (int x = 0; x < width_1; x++){
//                Color color_1 = pixelReader_1.getColor(x, y);
//                Color color_2 = pixelReader_2.getColor(x, y);
//                if (!color_1.equals(color_2)) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    //per-pixel copy images
    private ImageView cloneImage(Image image) {
        PixelReader pixelReader = image.getPixelReader();

        int width = (int)image.getWidth();
        int height = (int)image.getHeight();

        //Copy from source to destination pixel by pixel
        WritableImage writableImage
                = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }

        ImageView destImageView = new ImageView();
        destImageView.setImage(writableImage);

        return destImageView;
    }

    //set number on field
    private Cell setNumber(int count) {
        Cell cell = null;
        switch (count) {
            case 0: cell = new Cell(cloneImage(getImageView("zero").getImage()), "zero"); break;
            case 1: cell = new Cell(cloneImage(getImageView("one").getImage()), "one"); break;
            case 2: cell = new Cell(cloneImage(getImageView("two").getImage()), "two"); break;
            case 3: cell = new Cell(cloneImage(getImageView("three").getImage()), "three"); break;
            case 4: cell = new Cell(cloneImage(getImageView("four").getImage()), "four"); break;
            case 5: cell = new Cell(cloneImage(getImageView("five").getImage()), "five"); break;
            case 6: cell = new Cell(cloneImage(getImageView("six").getImage()), "six"); break;
            case 7: cell = new Cell(cloneImage(getImageView("seven").getImage()), "seven"); break;
            case 8: cell = new Cell(cloneImage(getImageView("eight").getImage()), "eight"); break;
        }
        return cell;
    }

    //create ImageView with coordinates
    private ImageView createImageView(ImageView im, int i, int j, boolean isDisabled) {
        ImageView image = cloneImage(im.getImage());
        image.setX(j * CELL_SIZE);
        image.setY((i * CELL_SIZE) + MENU_BAR_HEIGHT);
        image.setSmooth(true);
        image.setDisable(isDisabled);
        addTranslateListener(image);
        return image;
    }

    //end of game
    private void endOfGame(boolean isWin) {
        for (int i = 0; i < CELLS_COUNT_HEIGHT; i++)
            for (int j = 0; j < CELLS_COUNT_WIDTH; j++) {
                if (cells[i][j] == null) {
                    root.getChildren().add(createImageView(getImageView("field"), i, j, true));
                }
                else
                if (cells[i][j].name.equals("right_flag") && !cells[i][j].isBombOnFlag) {
                    root.getChildren().add(createImageView(getImageView("wrong_flag"), i, j, true));
                }
                else
                    root.getChildren().add(createImageView(cells[i][j].image, i, j, true));
            }
        isEndOfGame = true;
        if (isWin)
            smile.setImage(cloneImage(getImageView("win").getImage()).getImage());
        else
            smile.setImage(cloneImage(getImageView("oops").getImage()).getImage());
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void newGame() {
        for (int i = 0; i < CELLS_COUNT_HEIGHT; i++)
            for (int j = 0; j < CELLS_COUNT_WIDTH; j++) {
                cells[i][j] = null;
            }
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isEndOfGame = false;
        isFirstClick = true;
        smile = cloneImage(getImageView("start").getImage());
        initBombs();
        drawEmptyScene();
    }

    //check if it is available empty fields
    private boolean searchEmptyFields() {
        for (int i = 0; i < CELLS_COUNT_HEIGHT; i++)
            for (int j = 0; j < CELLS_COUNT_WIDTH; j++)
                if (cells[i][j] == null)
                    return true;
        return false;
    }

    //refresh all bombs
    private void refreshBombs(int k, int l) {
        for (int i = 0; i < CELLS_COUNT_HEIGHT; i++)
            for (int j = 0; j < CELLS_COUNT_WIDTH; j++) {
                cells[i][j] = null;
            }
        initBombs();
        initNumbers(k, l);
        System.out.println("I refresh bombs");
    }

    private boolean isUnopenedBombs(int i, int j) {
        int count = 0;
        for (int k = i - 1; k <= i + 1; k++) {
            while (k < 0) k++;
            if (k >= CELLS_COUNT_HEIGHT) break;
            for (int l = j - 1; l <= j + 1; l++) {
                while (l < 0) l++;
                if (l >= CELLS_COUNT_WIDTH) break;
                if (cells[k][l] != null
                        && (cells[k][l].name.equals("bomb")
                        || (cells[k][l].name.equals("right_flag") && cells[k][l].isBombOnFlag))
                        && cells[k][l].isOpen) {
                    count++;
                }
            }
        }
        if (count != getNumber(cells[i][j].name))
            return true;
        return false;
    }

    private int getNumber(String name) {
        switch (name) {
            case "one": return 1;
            case "two": return 2;
            case "three": return 3;
            case "four": return 4;
            case "five": return 5;
            case "six": return 6;
            case "seven": return 7;
            case "eight": return 8;
        }
        return 0;
    }

    private void hint(int i, int j) {
        for (int k = i - 1; k <= i + 1; k++) {
            while (k < 0)
                k++;
            if (k >= CELLS_COUNT_HEIGHT)
                break;
            for (int l = j - 1; l <= j + 1; l++) {
                while (l < 0)
                    l++;
                if (l >= CELLS_COUNT_WIDTH)
                    break;
                if (cells[k][l] == null) {
                    initNumbers(k, l);
                    repaint();
                }
            }
        }
    }

    //add listener to all fields
    private void addTranslateListener(final Node node) {
        node.setOnMousePressed(new EventHandler() {
            @Override
            public void handle(Event mouseEvent) {

                //indexes of cell
                int i = (int) ((((MouseEvent) mouseEvent).getSceneY() - MENU_BAR_HEIGHT) / CELL_SIZE);
                int j = (int) (((MouseEvent) mouseEvent).getSceneX() / CELL_SIZE);

                //System.out.println("Pressed i = " + i + " ; j = " + j);

                //if first click is on bomb start new game

                if (!isFirstClickLose) {
                    if (isFirstClick && cells[i][j] != null) {
                        while (cells[i][j].name.equals("bomb"))
                            refreshBombs(i, j);
                    }
                }
                isFirstClick = false;

                //if game is not ended
                if (!isEndOfGame) {
                    if (isHints && ((MouseEvent) mouseEvent).getButton() == MouseButton.PRIMARY
                            && ((MouseEvent) mouseEvent).getClickCount() == 2){
                        System.out.println("double click");
                        if (!isUnopenedBombs(i,j))
                            hint(i, j);
                        return;
                    }

                    if (timeline == null)
                        startTimer();
                    //if right click
                    if (((MouseEvent) mouseEvent).getButton() == MouseButton.SECONDARY) {
                        if (cells[i][j] != null && cells[i][j].name.equals("right_flag")) {
                            if (cells[i][j].isBombOnFlag == false)
                                cells[i][j] = null;
                            else {
                                cells[i][j].image = cloneImage(getImageView("bomb").getImage());
                                cells[i][j].isOpen = false;
                                cells[i][j].name = "bomb";
                            }
                            flagCount++;
                        }
                        else
                        if (flagCount > 0) {
                            //if cell is empty -> create new cell with flag
                            if (cells[i][j] == null) {
                                cells[i][j] = new Cell(createImageView(getImageView("right_flag"), i, j, false), "right_flag");
                                cells[i][j].isOpen = true;
                                flagCount--;
                            } else
                                //if cell isn`t empty and there is the bomb -> change image to flag
                                if (cells[i][j].name.equals("bomb")) {
                                    cells[i][j].image = createImageView(getImageView("right_flag"), i, j, false);
                                    cells[i][j].name = "right_flag";
                                    cells[i][j].isBombOnFlag = true;
                                    cells[i][j].isOpen = true;
                                    flagCount--;
                                }
                        }

                        if (chosenLevel.equals("beginner"))
                            flagCountLabel.setText("" + flagCount);
                        else
                            flagCountLabel.setText("Flags:  " + flagCount);
                        repaint();
                        return;
                    }

                    //if left click
                    if (cells[i][j] != null && cells[i][j].name.equals("bomb")) {
                        cells[i][j].image = cloneImage(getImageView("explosion").getImage());
                        endOfGame(false);
                    } else {
                        initNumbers(i, j);
                        repaint();
                    }

                    if (!searchEmptyFields()) {
                        if (!cells[i][j].name.equals("bomb"))
                            endOfGame(true);
                        else
                            endOfGame(false);
                    }
                }
            }
        });
    }

    //timer
    private void startTimer() {
        final int[] minute = {0};
        final int[] second = {0};
        timeline = new Timeline(new KeyFrame(
                Duration.millis(1000),
                ae -> {
                    second[0]++;
                    if (second[0] == 60) {
                        minute[0]++;
                        second[0] = 0;
                    }
                    String title = "";
                    if (!chosenLevel.equals("beginner"))
                        title = "Game time: ";
                    if (minute[0] < 10)
                        if (second[0] < 10)
                            timeLabel.setText(title + "0" + minute[0] + ":0" + second[0]);
                        else
                            timeLabel.setText(title + "0" + minute[0] + ":" + second[0]);
                    else
                    if (second[0] < 10)
                        timeLabel.setText(title + minute[0] + ":0" + second[0]);
                    else
                        timeLabel.setText(title + minute[0] + ":" + second[0]);
                }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public static void main(String... args){
        launch(args);
    }
}



