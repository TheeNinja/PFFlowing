package me.theeninja.pfflowing.flowing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import me.theeninja.pfflowing.Duplicable;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.configuration.InternalConfiguration;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.gui.*;
import me.theeninja.pfflowing.utils.Utils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.GraphicDecoration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FlowingRegion extends Label implements Duplicable<FlowingRegion> {

    private final Decoration hasCardsDecoration;
    public static final String QUESTION_KEY = "question";

    PopOver currentShownPopover;

    @Expose
    @SerializedName("fullText")
    private StringProperty fullText = new SimpleStringProperty();

    /**
     * The shortened version of full text. How short this is relative
     * to {@code fullText} is dependent on the limit specified in the
     * configuration. Since it is a dependent variable, no need to serialize.
     */
    private StringProperty shortenedText = new SimpleStringProperty();

    /**
     * Represents whether the flowing region is expanded on the flow grid.
     * Since this is set to a default value (false) when a flowing region is loaded on the
     * flow grid, no need to serialize.
     */
    private BooleanProperty expanded = new SimpleBooleanProperty();

    @Expose
    @SerializedName("associatedCards")
    private final ObservableList<Card> associatedCards;

    protected FlowingRegion(String text) {
        this(text, FXCollections.observableArrayList());
    }

    protected FlowingRegion(String text, ObservableList<Card> associatedCards) {
        super();

        int sideLength = FlowDisplayController.FLOWGRID_VERTICAL_GAP;

        Rectangle rectangle = new Rectangle(sideLength, sideLength);
        rectangle.fillProperty().bind(
            EFlow.getInstance().getConfiguration().getCardColor().valueProperty()
        );
        hasCardsDecoration = new GraphicDecoration(rectangle, Pos.BOTTOM_RIGHT);

        setWrapText(true);

        // Listener must be added first, before setting full text
        addFullTextListener();
        setFullText(text);

        fontProperty().bind(EFlow.getInstance().getConfiguration().getFont().valueProperty());

        setExpanded(false);

        this.associatedCards = associatedCards;

        getAssociatedCards().addListener(Utils.generateListChangeListener(this::onSizeChange));

        CardsDetailerController cardsDetailerController = getCardsDetailerController();

        expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                VBox popOverVBox = new VBox();
                if (hasCards()) {
                    popOverVBox.getChildren().add(cardsDetailerController.root);
                    cardsDetailerController.setCurrentCard(getAssociatedCards().get(0));
                }
                if (hasQuestion()) {
                    QuestionDetailerController questionDetailerController = getQuestionDetailerController();
                    popOverVBox.getChildren().add(questionDetailerController.questionText);
                }
                if (!popOverVBox.getChildren().isEmpty()) {
                    PopOver popOver = new PopOver(popOverVBox);
                    popOver.maxHeightProperty().bind(popOver.heightProperty());
                    popOver.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);

                    currentShownPopover = popOver;
                    currentShownPopover.show(this);
                }
            }
            else {
                if (currentShownPopover != null)
                    currentShownPopover.hide();
            }
        });
    }

    private boolean hasQuestion() {
        return getProperties().containsKey(QUESTION_KEY);
    }

    private boolean hasCards() {
        return !getAssociatedCards().isEmpty();
    }

    private boolean shouldShowPopOver() {
        boolean hasCards = !getAssociatedCards().isEmpty();

        return hasCards || hasQuestion();
    }

    public void addFullTextListener() {
        fullTextProperty().addListener((observable, oldValue, newValue) -> {
            String[] seperatedStrings = getFullText().split(InternalConfiguration.LENGTH_LIMIT_TYPE.getSplit());

            List<String> limitedSeperatedStrings = Arrays.stream(seperatedStrings).limit(InternalConfiguration.LENGTH_LIMIT).collect(Collectors.toList());

            String shortenedString = String.join(InternalConfiguration.LENGTH_LIMIT_TYPE.getSplit(), limitedSeperatedStrings);

            if (seperatedStrings.length > InternalConfiguration.LENGTH_LIMIT) {
                shortenedString += "...";
            }

            setShortenedText(shortenedString);
        });
    }

    @Override
    public FlowingRegion duplicate() {
        FlowingRegion duplicate = new FlowingRegion(getText());
        duplicate.setStyle(this.getStyle());
        FlowGrid.setColumnIndex(duplicate, FlowGrid.getColumnIndex(this));
        FlowGrid.setRowIndex(duplicate, FlowGrid.getRowIndex(this));
        return duplicate;
    }

    private LengthLimitType lengthLimitType;
    private int limit;

    public LengthLimitType getLengthLimitType() {
        return lengthLimitType;
    }

    public void setLengthLimitType(LengthLimitType lengthLimitType) {
        this.lengthLimitType = lengthLimitType;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getShortenedText() {
        return shortenedText.get();
    }

    public StringProperty shortenedTextProperty() {
        return shortenedText;
    }

    public void setShortenedText(String shortenedText) {
        this.shortenedText.set(shortenedText);
    }

    public String getFullText() {
        return fullText.get();
    }

    public StringProperty fullTextProperty() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText.set(fullText);
    }

    public boolean getExpanded() {
        return expanded.get();
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        if (expanded)
            textProperty().bind(fullTextProperty());
        else
            textProperty().bind(shortenedTextProperty());
        this.expanded.set(expanded);
    }

    public ObservableList<Card> getAssociatedCards() {
        return associatedCards;
    }

    private CardsDetailerController getCardsDetailerController() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/cardDisplay/card_details.fxml"));
            CardsDetailerController flowingRegionDetailController = new CardsDetailerController(this);
            fxmlLoader.setController(flowingRegionDetailController);
            fxmlLoader.load();

            return flowingRegionDetailController;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private QuestionDetailerController getQuestionDetailerController() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/flow/question_detailer.fxml"));
            String question = (String) getProperties().get(QUESTION_KEY);
            QuestionDetailerController questionDetailerController = new QuestionDetailerController(question);
            fxmlLoader.setController(questionDetailerController);
            fxmlLoader.load();

            return questionDetailerController;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onSizeChange() {
        if (!getAssociatedCards().isEmpty())
            Decorator.addDecoration(this, hasCardsDecoration);
        else
            Decorator.removeDecoration(this, hasCardsDecoration);
    }
}
