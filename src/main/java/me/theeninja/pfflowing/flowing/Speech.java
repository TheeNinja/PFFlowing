package me.theeninja.pfflowing.flowing;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.speech.Side;

public abstract class Speech {
    public static final int SPEECH_SIZE = 8;

    private SimpleIntegerProperty availableRow = new SimpleIntegerProperty(0);
    private SimpleIntegerProperty defensiveRegionsNumber = new SimpleIntegerProperty(0);

    private final Side side;
    private final String labelText;
    private final int gridPaneColumn;

    Speech(Side side, String labelText, int flowingPaneColumn) {
        this.side = side;
        this.labelText = labelText;
        this.gridPaneColumn = flowingPaneColumn;
    }

    public Color getColor() {
        return getSide() == Side.AFFIRMATIVE ? Color.BLACK : Color.RED;
    }

    public Side getSide() {
        return side;
    }

    public String getLabelText() {
        return labelText;
    }

    public int getColumn() {
        return gridPaneColumn;
    }

    public SimpleIntegerProperty availableRowProperty() {
        return availableRow;
    }

    public void setAvailableRow(int availableRow) {
        this.availableRow.set(availableRow);
    }

    public int getAvailableRow() {
        return availableRow.get();
    }

    public int getDefensiveRegionsNumber() {
        return defensiveRegionsNumber.get();
    }

    public SimpleIntegerProperty defensiveRegionsNumberProperty() {
        return defensiveRegionsNumber;
    }

    public void setDefensiveRegionsNumber(int defensiveRegionsNumber) {
        this.defensiveRegionsNumber.set(defensiveRegionsNumber);
    }
}