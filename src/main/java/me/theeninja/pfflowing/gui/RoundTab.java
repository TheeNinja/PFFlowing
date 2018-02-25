package me.theeninja.pfflowing.gui;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import me.theeninja.pfflowing.tournament.Round;

public class RoundTab extends Tab {
    private final Round round;

    public RoundTab(Round round) {
        this.round = round;

        getRound().selectedControllerProperty().addListener(this::onSelectedControllerChanged);

        textProperty().bind(round.nameProperty());
    }

    public Round getRound() {
        return round;
    }

    private void onSelectedControllerChanged(ObservableValue<? extends FlowDisplayController> observable, FlowDisplayController oldValue, FlowDisplayController newValue) {
        setContent(newValue.getCorrelatingView());
    }
}
