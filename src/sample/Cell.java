package sample;

import javafx.scene.image.ImageView;

public class Cell {
    ImageView image;
    String name;
    boolean isBombOnFlag;
    boolean isOpen;

    Cell(ImageView image, String name) {
        this.image = image;
        this.name = name;
        isBombOnFlag = false;
        isOpen = false;
    }

}
