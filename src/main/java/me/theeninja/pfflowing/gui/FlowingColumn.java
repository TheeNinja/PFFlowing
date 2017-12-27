package me.theeninja.pfflowing.gui;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import me.theeninja.pfflowing.Configuration;
import me.theeninja.pfflowing.Side;
import me.theeninja.pfflowing.card.Card;
import me.theeninja.pfflowing.card.CharacterFormatting;
import me.theeninja.pfflowing.card.CharacterStyle;
import me.theeninja.pfflowing.card.DefensiveReasoning;
import me.theeninja.pfflowing.flowing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class FlowingColumn extends VBox implements Bindable<Speech> {
    private final Speech speech;
    private final Label label;
    private final ContentContainer contentContainer;
    private Speech bindedSpeech;

    private Color color;

    private final boolean managesOpposite;

    public FlowingColumn(Speech speech) {
        this.speech = speech;

        this.label = new Label(speech.getLabelText());

        this.contentContainer = new ContentContainer();

        Bindable.bind(this, getSpeech());

        getChildren().add(getLabel());
        getChildren().add(getContentContainer());

        setPrefWidth(FlowingColumnsController.getFXMLInstance().getCorrelatingView().getPrefWidth() / 8);
        HBox.setHgrow(this, Priority.ALWAYS);

        managesOpposite = getBinded() instanceof DefensiveSpeech;
    }

    public static List<FlowingColumn> of(SpeechList speechList) {
        List<FlowingColumn> flowingColumns = new ArrayList<>();
        for (Speech speech : speechList.getSpeeches()) {
            FlowingColumn flowColumn = speech.getSide() == speechList.getSide() ?
                    new FlowingColumn(speech) :
                    new FlowingColumn(speech);
            flowingColumns.add(flowColumn);
        }
        return flowingColumns;
    }

    public Label getLabel() {
        return label;
    }

    public Speech getSpeech() {
        return speech;
    }


    /**
     * Adds a {@link TextArea} (the flowing region writer) to the flowing column. This flowing region writer
     * is designed so that on the user hitting enter, the text entered into the flowing region writer
     * would be used to create a flowing region representing what the user typed.
     */
    public void addFlowingRegionWriter(boolean createNewOne, boolean refMode, Consumer<String> postEnterAction) {
        if (refMode)
            addReactiveFlowingRegionWriter(createNewOne, postEnterAction);
        else
            addProactiveFlowingRegionWriter(createNewOne, postEnterAction);
    }

    private TextArea generateInputTextArea() {
        TextArea textArea = new TextArea();
        textArea.prefWidthProperty().bind(this.widthProperty());
        textArea.setWrapText(true);
        textArea.setFont(Configuration.FONT);

        return textArea;
    }

    private final KeyCodeCombination TEXTAREA_SUBMIT = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    private EventHandler<KeyEvent> generateHandler(TextArea textArea, Node removedNode, boolean createNewOne, boolean refMode, Consumer<String> postEnterAction) {
        return (KeyEvent keyEvent) -> {
            if (TEXTAREA_SUBMIT.match(keyEvent)) {
                postEnterAction.accept(textArea.getText());

                getChildren().remove(removedNode);

                if (createNewOne)
                    addFlowingRegionWriter(true, refMode, postEnterAction);
            }
        };
    }

    private void addReactiveFlowingRegionWriter(boolean createNewOne, Consumer<String> postEnterAction) {
        TextArea textArea = generateInputTextArea();

        Group group = new Group(textArea);
        group.setManaged(false);
        FlowingRegion firstElement = FlowingColumnsController.getFXMLInstance().getLastSelected();

        // Text area is being added to the whole parent, not just
        group.setLayoutY(firstElement.getLayoutY() + getLabel().getHeight());

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, generateHandler(textArea, group, createNewOne, false, postEnterAction));

        this.getChildren().add(group);
        textArea.requestFocus();

        FlowingColumnsController.getFXMLInstance().addCardSelectorSupport(textArea);
    }

    private void addProactiveFlowingRegionWriter(boolean createNewOne, Consumer<String> postEnterAction) {
        TextArea textArea = generateInputTextArea();

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, generateHandler(textArea, textArea, createNewOne, false, postEnterAction));

        this.getChildren().add(textArea);
        textArea.requestFocus();

        FlowingColumnsController.getFXMLInstance().addCardSelectorSupport(textArea);
    }

    /**
     * Defaul post-enter specification for the above method
     * @param createNewOne
     */
    public void addFlowingRegionWriter(boolean createNewOne) {
        addFlowingRegionWriter(createNewOne, false, text -> {
            DefensiveReasoning defensiveReasoning = new DefensiveReasoning(text);
            addDefensiveFlowingRegion(defensiveReasoning);
        });
    }

    public <T extends FlowingRegion & Offensive> void addOffensiveFlowingRegion(T offensiveRegion) {
        addFlowingRegion(true, offensiveRegion);
        // drawArrow(offensiveRegion);
    }

    private <T extends FlowingRegion & Offensive> void drawArrow(T offensiveRegion) {
        FlowingRegion starter = offensiveRegion.getTargetRegion();
            Bounds starterBounds = starter.localToScene(starter.getLayoutBounds());
            double startX = starterBounds.getMaxX() + Configuration.ARROW_MARGIN;
            double startY = (starterBounds.getMinY() + starterBounds.getMaxY()) / 2;

            Bounds finishBounds = offensiveRegion.localToScene(offensiveRegion.getLayoutBounds());
            double finishX = finishBounds.getMinX() - Configuration.ARROW_MARGIN;
            double finishY = (finishBounds.getMinY() + finishBounds.getMaxY()) / 2;

            Line line = new Line(startX, startY, finishX, finishY);

            PFFlowingApplicationController.getFXMLInstance().getCorrelatingView().getChildren().add(line);
    }

    public <T extends FlowingRegion & Defensive> void addDefensiveFlowingRegion(T defensiveRegion) {
        addFlowingRegion(false, defensiveRegion);
    }

    public <T extends FlowingRegion> void addFlowingRegion(boolean refMode, T flowingRegion) {
        FlowingColumnsController.getFXMLInstance().implementListeners(flowingRegion);
        if (flowingRegion instanceof Card) {
            Card flowingCard = (Card) flowingRegion;

            CharacterFormatting characterFormatting = new CharacterFormatting(Arrays.asList(
                Configuration.SPOKEN
            ));

            String tooltipText = flowingCard.getCardContent().getContent(characterFormatting);

            Tooltip flowingRegionTooltip = new Tooltip(tooltipText);

            characterFormatting.getCharacterStyles().stream()
                    .map(CharacterStyle::getCssClass)
                    .forEach(flowingRegionTooltip.getStyleClass()::add);

            Tooltip.install(flowingRegion, flowingRegionTooltip);
        }

        flowingRegion.setWrapText(true);
        flowingRegion.setTextFill(color);

        if (refMode) {
            Group group = new Group(flowingRegion);

            group.setManaged(false);
            FlowingRegion targetRegion = ((Offensive) flowingRegion).getTargetRegion();
            group.setLayoutY(targetRegion.getLayoutY());

            getContentContainer().getChildren().add(group);
        } else
            getContentContainer().getChildren().add(flowingRegion);

        // User actions
        flowingRegion.prefWidthProperty().bind(this.widthProperty());
    }


    public void removeAllFlowingRegionWriters() {
        getChildren().removeIf(node -> node instanceof TextArea);
    }

    public ContentContainer getContentContainer() {
        return contentContainer;
    }

    @Override
    public void setBinded(Speech speech) {
        this.bindedSpeech = speech;
        this.color = speech.getSide() == Side.AFFIRMATIVE ? Color.BLACK : Color.RED;
        this.label.setTextFill(this.color);
    }

    @Override
    public Speech getBinded() {
        return bindedSpeech;
    }

    private FlowingColumn getOpposingFlowingColumn() {
        FlowingColumns flowingColumns = getParentFlowingColumns();
        SpeechListManager speechListManager = flowingColumns.getBinded();
        return speechListManager.getSpeechList(this.getBinded()).getOpposite(this.getBinded()).getBinded();
    }

    public boolean isManagesOpposite() {
        return managesOpposite;
    }

    public FlowingColumns getParentFlowingColumns() {
        return (FlowingColumns) getParent();
    }
}
