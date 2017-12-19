package me.theeninja.pfflowing.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.Utils;
import me.theeninja.pfflowing.card.*;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.utils.Pair;

import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static me.theeninja.pfflowing.gui.KeyCodeCombinationUtils.*;

/**
 * The controller for the actual flowing area on the application. This controller
 * is responsible for managing the relations between all flowing regions that exist.
 *
 * @author TheeNinja
 */
public class FlowingColumnsController implements Initializable, SingleViewController<FlowingColumns>, EventHandler<KeyEvent> {

    /**
     * The {@link HBox} that contains eight colums (one for each speech).
     * This provides an access bridge between multiple speeches, as this
     * is the parent of all {@link VBox}es responsible for managing their
     * associated speeches.
     */
    @FXML public FlowingColumns flowingColumns;

    /**
     * The an instance of {@link ColorUseManager} that manages what {@link Pair}
     * of a {@link Color} and {@link Background} is used for each relationship between
     * a {@link List<FlowingRegion>} and the associated offensive {@link FlowingRegion}
     */
    private ColorUseManager colorUseManager;

    /**
     *
     */
    private SpeechListManager speechListManager;

    /**
     * Houses a list of all flowing regions that are currently selected. This serves
     * as input for many actions that occur throughout the flowing columns that require
     * target flowing regions, i.e, merging/refuting/speech constructor/etc. A listener
     * is added to provide visual changes to the GUI when something is selected (it is
     * important to inform the user what they have selected currently in a visual
     * manner).
     */
    private ObservableList<FlowingRegion> selectedFlowingRegions =
            FXCollections.observableArrayList();

    /**
     * The last selected flowing region will always be the last element of
     * {@code selectedFlowingRegions}. As for why it is useful to keep track of the
     * last selected {@link FlowingRegion}, the arrow functions used to navigate
     * through the entire flowing area and select multiple things at once require
     * knowledge of the last selected {@link FlowingRegion}.
     */
    private FlowingRegion lastSelected;


    /**
     * The selected flowing column is important to keep track of since many actions used
     * to modify individual flowing columns require knowledge of which flowing column they are
     * modifying (in order to access the children, change them, etc).
     */
    private FlowingColumn selectedFlowingColumn;

    Map<KeyCodeCombination, Runnable> keyCodeCombinationMap = new HashMap<>();

    private void populateKeyCodeCombinationMap() {
        keyCodeCombinationMap.put(REFUTE, this::refute);
        keyCodeCombinationMap.put(EXTEND, this::extend);
        keyCodeCombinationMap.put(NEXT, this::nextSpeech);
        keyCodeCombinationMap.put(SELECT_LEFT_ONLY, () -> handleSelection(this::getLeft, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_RIGHT_ONLY, () -> handleSelection(this::getRight, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_DOWN_ONLY, () -> handleSelection(this::getDown, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_UP_ONLY, () -> handleSelection(this::getUp, getLastSelected(), false));
        keyCodeCombinationMap.put(SELECT_LEFT_TOO, () -> handleSelection(this::getLeft, getLastSelected(), true));
        keyCodeCombinationMap.put(SELECT_RIGHT_TOO, () -> handleSelection(this::getRight, getLastSelected(), true));
        keyCodeCombinationMap.put(SELECT_DOWN_TOO, () -> handleSelection(this::getDown, getLastSelected(), true));
        keyCodeCombinationMap.put(SELECT_UP_TOO, () -> handleSelection(this::getUp, getLastSelected(), true));
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxmlInstance = this;

        initializeListeners();
        populateKeyCodeCombinationMap();

        colorUseManager = new ColorUseManager();
        speechListManager = new SpeechListManager(flowingColumns);

        // Set up the flowing pane for aff 1 speech
        speechListManager.selectAffSpeechMap();
        speechListManager.getVisibleSelectedSpeech().getBinded().addFlowingRegionWriter();
    }

    public ObservableList<FlowingRegion> getSelectedFlowingRegions() {
        return selectedFlowingRegions;
    }

    public FlowingRegion getLastSelected() {
        return lastSelected;
    }

    private void setLastSelected(FlowingRegion flowingRegion) {
        logger.log(Level.INFO,
                "Set last selected Flowing Region to {0}", flowingRegion);
        this.lastSelected = flowingRegion;
    }

    private static FlowingColumnsController fxmlInstance;

    public static FlowingColumnsController getFXMLInstance() {
        return fxmlInstance;
    }

    /**
     * Serves a list of all offensive cards currently on the flowing pane. It is observable,
     * hence when an offensive card is added, {@code colorUseManager} provides a {@link Pair}
     * of a {@link Color} and {@link Background} use to illustrate the relationship between the
     * offensive card and its targeted flowing regions.
     */
    ObservableList<OffensiveCard> offensiveCards = FXCollections.observableArrayList();

    /**
     * Serves a list of all offensive reasonings currently on the flowing pane. It is observable,
     * hence when an offensive card is added, {@code colorUseManager} provides a {@link Pair}
     * of a {@link Color} and {@link Background} use to illustrate the relationship between the
     * offensive reasonings and its targeted flowing regions.
     */
    ObservableList<OffensiveReasoning> offensiveReasonings = FXCollections.observableArrayList();

    @Deprecated
    ObservableList<Line> lineLinks = FXCollections.observableArrayList();

    private static final Logger logger = Logger.getLogger(FlowingColumnsController.class.getSimpleName());

    public void addCardSelectorSupport(TextArea textArea) {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.SEMICOLON) {

            }
        });
    }

    /**
     * Styles the given flowing region with the color and background of the given {@link Pair}. In addition,
     * the link element is given a font weight of bold in order to make its indivudal text color more prominent
     * and distinguishable from link elements of other links.
     *
     * @param flowingRegion The flowing region to style with the provided pair's color and background.
     * @param pair The tuple consisting of a {@link Color} and {@link Background} used to style the
     *             provided flowing region.
     */
    private void styleLinkElement(FlowingRegion flowingRegion, Pair<Color, Background> pair) {
        flowingRegion.setTextFill(pair.getFirst());
        flowingRegion.setBackground(pair.getSecond());
        flowingRegion.setStyle("-fx-font-weight: bold");
    }

    /**
     * When called, a visual link will be constructed between the provided offensive flowing region
     * and the flowing regions that it targets. The visual link is displayed by assigning a text color
     * and background color to each of the components involved in the link. The text color and background color
     * are distinct for each link.
     *
     * @param offensiveFlowingRegion The offensive flowing region subject to the requested link
     *                               with its targeted flowing regions.
     * @param <T> The type of offensive flowing region (such as an {@link OffensiveCard},
     *           {@link OffensiveReasoning}, etc.)
     */
    private <T extends FlowingRegion & Offensive> void link(T offensiveFlowingRegion) {
        System.out.println("calling link");

        List<FlowingRegion> targetFlowingRegions = offensiveFlowingRegion.getTargetRegions();

        if (colorUseManager.hasNext()) {
            Pair<Color, Background> pair = colorUseManager.next();
            Color color = pair.getFirst();
            Background background = pair.getSecond();

            for (FlowingRegion flowingRegion : targetFlowingRegions) {
                styleLinkElement(flowingRegion, pair);
                unselectedBackgrounds.put(flowingRegion, flowingRegion.getBackground());
            }

            // The offensive flowing region itself must be styled in addition to its targets
            styleLinkElement(offensiveFlowingRegion, pair);
            unselectedBackgrounds.put(offensiveFlowingRegion, offensiveFlowingRegion.getBackground());
        }
        else {
            System.out.println("Ran out of colors");
        }
    }


    private <T extends FlowingRegion & Offensive> void delink(T offensiveFlowingRegion) {

    }

    private <K, V> Map<V, K> reverseMap(Map<K, V> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    private boolean areSameSpeech(List<FlowingRegion> flowingRegions) {
        FlowingColumn firstFlowingColumn = ((FlowingColumn) flowingRegions.get(0).getParent());
        Speech firstElementSpeech =
            speechListManager.getSelectedSpeechList().findFirstSpeech(speech -> speech.getBinded() == firstFlowingColumn).orElse(null);
        return flowingRegions.stream().allMatch(flowingRegion ->
            ((FlowingColumn) flowingRegion.getParent().getParent()).getBinded() == firstElementSpeech);
    }

    private void merge() {
        if (areSameSpeech(selectedFlowingRegions)) {
            VBox baseContainer = (VBox) selectedFlowingRegions.get(0).getParent();
            System.out.println("1st: " + selectedFlowingRegions.get(0));
            System.out.println(baseContainer);
            StringBuilder labelTexts = new StringBuilder();
            for (FlowingRegion flowingRegion : selectedFlowingRegions) {
                labelTexts.append(flowingRegion.getText());
                if (!Utils.isLastElement(selectedFlowingRegions, flowingRegion)) {
                    labelTexts.append(System.lineSeparator());
                }
            }
            selectedFlowingRegions.get(0).setText(labelTexts.toString());

            baseContainer.getChildren().removeAll(selectedFlowingRegions
                    .stream()
                    .filter(element -> element != selectedFlowingRegions.get(0)).collect(Collectors.toList()));

            selectedFlowingRegions.removeAll(selectedFlowingRegions
                    .stream()
                    .filter(element -> element != selectedFlowingRegions.get(0)).collect(Collectors.toList()));

            setLastSelected(selectedFlowingRegions.get(0));
        }
        else {
            System.out.println("not same speech");
        }
    }

    private TextArea currentFlowingRegionWriter;

    public void implementListeners(FlowingRegion flowingRegion) {
        flowingRegion.setOnMousePressed(mouseEvent ->
                handleSelection(Optional.of(flowingRegion), mouseEvent.isControlDown()));
    }

    private Map<FlowingRegion, Background> unselectedBackgrounds = new HashMap<>();

    private void removeSelectionStyling(FlowingRegion flowingRegion) {
        if (flowingRegion != null) {
            logger.log(Level.INFO,
                    "Removing selection styling of {0}", flowingRegion);
            flowingRegion.setBackground(unselectedBackgrounds.get(flowingRegion));
        }
    }

    private void addSelectionStyling(FlowingRegion flowingRegion) {
        if (flowingRegion != null) {
            logger.log(Level.INFO,
                    "Add selection styling of {0}", flowingRegion);
            flowingRegion.setBackground(Utils.generateBackgroundOfColor(Color.LIGHTBLUE));
        }
    }

    public void select(Optional<FlowingRegion> flowingRegion, boolean isCtrlDown) {

        if (!flowingRegion.isPresent()) {
            System.out.println("Not present.");
            return;
        }

        setLastSelected(flowingRegion.get());

        if (!isCtrlDown)
            selectedFlowingRegions.clear();
        selectedFlowingRegions.add(getLastSelected());
    }

    public void unselect(Optional<FlowingRegion> flowingRegion, boolean isCtrlDown) {
        if (!flowingRegion.isPresent()) {
            System.out.println("Nothing to unseelect");
        }

        setLastSelected(flowingRegion.get());

        selectedFlowingRegions.clear();
        selectedFlowingRegions.add(getLastSelected());
    }

    private void handleSelection(Optional<FlowingRegion> flowingRegion, boolean isCtrlDown) {
        if (selectedFlowingRegions.contains(flowingRegion)){
            logger.log(Level.INFO,
                    "Unselecting flowing region {0}.", flowingRegion);
            unselect(flowingRegion, isCtrlDown);
        }
        else {
            logger.log(Level.INFO,
                    "Adding {0} to selected flowing regions", flowingRegion);
            select(flowingRegion, isCtrlDown);
        }
    }

    @Override
    public FlowingColumns getCorrelatingView() {
        return flowingColumns;
    }

    @Override
    public void handle(KeyEvent keyEvent) {
        logger.log(Level.INFO, "{0} key pressed", keyEvent.getCode().getName());
        for (KeyCodeCombination keyCodeCombination : keyCodeCombinationMap.keySet())
            if (keyCodeCombination.match(keyEvent))
                keyCodeCombinationMap.get(keyCodeCombination).run();

        /*
          Handles the following keys:

          Alt + Up Arrow, Alt + Left Arrow, Alt + Down Arrow, Alt + Right Arrow
          (All with the optional Control modifier to preserve previous selections)
         */
        if (key.isArrowKey()) {
            // With the shift key held down, the user intends to cycle the selected speech,
            // and all other logic is disregarded.
            if (keyEvent.isShiftDown() && keyEvent.isControlDown()) {
                int offset;

                switch (keyEvent.getCode()) {
                    case LEFT: {
                        offset = -1;
                        break;
                    }
                    case RIGHT: {
                        offset = 1;
                        break;
                    }
                    // Handles up and down arrow keys
                    default: {
                        offset = 0;
                        break;
                    }
                }

                Speech toBeSelectedSpeech = Utils.getRelativeElement(speechListManager.getSelectedSpeechList().getSpeeches(), speechListManager.getVisibleSelectedSpeech(), offset);
                speechListManager.getSelectedSpeechList().setSelectedSpeech(toBeSelectedSpeech);

                return;
            }

            // Declared as Optional since the Flowing Region may be on the border
            // and there may be no other FlowingRegion in the direction specified.
            Optional<FlowingRegion> selectedFlowingRegion = Optional.empty();

            // The user must select a FlowingRegion with the mouse before
            // they utlize the arrows.
            if (lastSelected == null)
                return;

            switch (keyEvent.getCode()) {
                case UP: {
                    selectedFlowingRegion = getUp(lastSelected);
                    break;
                }
                case DOWN: {
                    selectedFlowingRegion = getDown(lastSelected);
                    break;
                }
                case LEFT: {
                    selectedFlowingRegion = getLeft(lastSelected);
                    break;
                }
                case RIGHT: {
                    selectedFlowingRegion = getRight(lastSelected);
                    break;
                }
            }

            // If getUp, getDown, etc. has no FlowingRegion to return (as it is
            // on the edge), no action is taken.
            if (!selectedFlowingRegion.isPresent()) {
                logger.log(Level.INFO,
                        "No flowing region present in {0} direction.",
                        keyEvent.getCode());
                return;
            }

            // It has been confirmed that there is a newly selected FlowingRegion,
            // hence update the last selected FlowingRegion.
            setLastSelected(selectedFlowingRegion.get());

            handleSelection(selectedFlowingRegion.get(), keyEvent.isControlDown());
        }

        else if (keyEvent.isControlDown())
            switch (keyEvent.getCode()) {
                case M: { // Merge
                    merge();
                    break;
                }
                case N: { // Next speech

                }
                case R: { // Refute

                }
                case P: { // Present

                }
                case E: { // Extend

                }
                case SLASH: { // Ask in Cross X

                }
                case T: { // Time

                }
            }

        // Remove focus from the textfield and rewire the focus to the general pane
        if (key == KeyCode.ESCAPE) {
            getCorrelatingView().requestFocus();
        }
    }

    private void refute() {

    }

    private void extend() {

    }

    private void nextSpeech() {
        speechListManager.getSelectedSpeechList().
                getSelectedSpeech().getBinded().removeAllFlowingRegionWriters();
        speechListManager.getSelectedSpeechList().
                setSelectedSpeech(Utils.getRelativeElement(
                        speechListManager.getSelectedSpeechList().getSpeeches(), speechListManager.getVisibleSelectedSpeech(), 1));
        speechListManager.getVisibleSelectedSpeech().getBinded().addFlowingRegionWriter();
    }

    private void handleSelection(Function<FlowingRegion, Optional<FlowingRegion>> function, FlowingRegion flowingRegion, boolean isCtrlDown) {
        handleSelection(function.apply(flowingRegion), isCtrlDown);
    }

    public void setCurrentFlowingRegionWriter(TextArea currentFlowingRegionWriter) {
        this.currentFlowingRegionWriter = currentFlowingRegionWriter;
    }

    public void generateLineLinksListener() {
        BorderPane parent = PFFlowingApplicationController
                .getFXMLInstance().getCorrelatingView();

        lineLinks.addListener(Utils.generateListChangeListener(
                parent.getChildren()::add,
                parent.getChildren()::remove
        ));
    }

    private void initializeListeners() {
        selectedFlowingRegions.addListener(Utils.generateListChangeListener(
                this::addSelectionStyling,
                this::removeSelectionStyling
        ));

        offensiveCards.addListener(Utils.generateListChangeListener(
                this::link,
                this::delink
        ));

        offensiveReasonings.addListener(Utils.generateListChangeListener(
                this::link,
                this::delink
        ));
    }

    //// Family of functions used for getting relative flowing regions through provided base flowing region

    // Internal functions

    /**
     *
     * @param flowingRegion The flowing region to base all directions upon.
     * @param offset How far the relative flowing region is in the up/down direction (+ -> up, - -> down)
     * @return Thee flowing region assuming the offset is within the currently utilized part of the flowing pane,
     * otherwise an empty {@link Optional}
     */
    private Optional<FlowingRegion> getVerticallyRelativeFlowingRegion(FlowingRegion flowingRegion, int offset) {
        VBox contentContainer = (VBox) flowingRegion.getParent();

        System.out.println(flowingRegion.getParent());

        int baseIndex = contentContainer.getChildren().indexOf(flowingRegion);

        // It makes more sense to the user for a positive offset to yield a higher node rather than
        // a lower node, hence the subtraction rather than the addition.
        int finalIndex = baseIndex - offset;

        if (finalIndex >= contentContainer.getChildren().size() || finalIndex < 0)
            return Optional.empty();

        Node node =  contentContainer.getChildren().get(finalIndex);

        if (node == null)
            return Optional.empty();

        if (!(node instanceof FlowingRegion))
            return Optional.empty();

        return Optional.of((FlowingRegion) node);
    }

    /**
     *
     * @param flowingRegion The flowing region to base all directions upon.
     * @param offset How far the relative flowing region is in the right/left direction (+ -> right, - -> left)
     * @return Thee flowing region assuming the offset is within the currently utilized part of the flowing pane,
     * otherwise an empty {@link Optional}
     */
    private Optional<FlowingRegion> getHorizontallyRelativeFlowingRegion(FlowingRegion flowingRegion, int offset) {
        VBox baseContentContainer = (VBox) flowingRegion.getParent();
        int indexInParent = baseContentContainer.getChildren().indexOf(flowingRegion);
        Speech baseSpeech = speechListManager.getVisibleSelectedSpeech();

        Speech relativeSpeech;

        try {
            relativeSpeech = Utils.getRelativeElement(speechListManager.getSelectedSpeechList().getSpeeches(), baseSpeech, offset);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        System.out.println(relativeSpeech.toString());

        VBox relativeContentContainer = relativeSpeech.getBinded();

        if (relativeContentContainer.getChildren().size() == 0)
            return Optional.empty();

        /*
        By now the parent of the flowing region the user seeks relative to the parent of the
        base flowing region exists. However, the node itself may not exist at the same index,
        as the parents' children have different sizes. Therefore, we adjust the index of the node
        to be the last element in the relative parent if it exceeds or equals the children's size.
         */
        if (indexInParent >= relativeContentContainer.getChildren().size())
            indexInParent  = relativeContentContainer.getChildren().size() - 1;

        Node node = relativeContentContainer.getChildren().get(indexInParent);

        if (!(node instanceof FlowingRegion)) {

            throw new Error("Unexpected error here. Node in this case should always be a flowing region.");
        }

        return Optional.of((FlowingRegion) node);
    }

    // External functions

    /**
     * @param flowingRegion The flowing region to base all directions upon.
     * @return The flowing region located directly above the parameter (hence in
     * the same flowing column/speech).
     */
    private Optional<FlowingRegion> getUp(FlowingRegion flowingRegion) {
        return getVerticallyRelativeFlowingRegion(flowingRegion, 1);
    }

    /**
     * @param flowingRegion The flowing region to base all directions upon.
     * @return The flowing region located directly below the parameter (hence in
     * the same flowing column/speech).
     */
    private Optional<FlowingRegion> getDown(FlowingRegion flowingRegion) {
        return getVerticallyRelativeFlowingRegion(flowingRegion, -1);
    }

    /**
     * @param flowingRegion The flowing region to base all directions upon.
     * @return The flowing region located directly to the right of the parameter (hence in
     * the right flowing column/speech). If the base flowing region is in the right-most column,
     * an empty {@link Optional} is returned.
     */
    private Optional<FlowingRegion> getRight(FlowingRegion flowingRegion) {
        return getHorizontallyRelativeFlowingRegion(flowingRegion, 1);
    }

    /**
     * @param flowingRegion The flowing region to base all directions upon.
     * @return The flowing region located directly to the left of the parameter (hence in
     * the left flowing column/speech). If the base flowing region is in the left-most column,
     * an empty {@link Optional} is returned.
     */
    private Optional<FlowingRegion> getLeft(FlowingRegion flowingRegion) {
        return getHorizontallyRelativeFlowingRegion(flowingRegion, -1);
    }

    public SpeechListManager getSpeechListManager() {
        return speechListManager;
    }
}