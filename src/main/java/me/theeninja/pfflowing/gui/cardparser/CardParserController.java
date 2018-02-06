package me.theeninja.pfflowing.gui.cardparser;

import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.card.CardContent;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class CardParserController implements SingleViewController<BorderPane>, Initializable {

    private Stage associatedStage;

    private String cardSelectionCSS;
    private String contentSelectionCSS;

    public GridPane optionsPane;

    @FXML public HBox parsedCardsDispaly;
    @FXML public VBox authorRequest;
    @FXML public VBox sourceRequest;
    @FXML public VBox dateRequest;
    @FXML public BorderPane cardParserArea;
    @FXML public VBox dateOptions;
    @FXML public VBox sourceOptions;
    @FXML public VBox authorOptions;
    @FXML public HBox cardOptions;
    @FXML public WebView documentDisplay;
    @FXML public ProgressBar progressBar;
    @FXML public VBox rightContainer;

    private HBox selectedRequest;

    private ObservableSet<Card> parsedCards;

    private List<HBox> requests;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.prefWidthProperty().bind(rightContainer.widthProperty());

        // Allows keyboard shortcuts to be used in order to navigate the document/fill the input boxes
        getCorrelatingView().addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            for (Map.Entry<KeyCodeCombination, Runnable> entry : PARSER_ACTIONS.entrySet())
                if (entry.getKey().match(keyEvent))
                    entry.getValue().run();
        });

        // Splits border pane width equally between left and right components (no center is used)
        rightContainer.prefWidthProperty().bind(getCorrelatingView().widthProperty().divide(2));
        optionsPane.prefWidthProperty().bind(getCorrelatingView().widthProperty().divide(2));

        parseNextCard();
    }

    private void useCardSelectionStyling() {
        String cardSelectionCSSLocation = getClass().getResource("/card_parser/card_selection.css").toExternalForm();
        documentDisplay.getEngine().setUserStyleSheetLocation(cardSelectionCSSLocation);
    }

    private void useContentSelectionStyling() {
        String contentSelectionCSSLocation = getClass().getResource("/card_parser/content_selection.css").toExternalForm();
        documentDisplay.getEngine().setUserStyleSheetLocation(contentSelectionCSSLocation);
    }

    private void generateOptionIndexLabel(int index) {
        Label label = new Label(String.valueOf(index));
        label.setBackground(Utils.generateBackgroundOfColor(Color.LIGHTGRAY));
        label.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    private Map<KeyCodeCombination, Runnable> PARSER_ACTIONS = Map.of(
        LOAD_FILE, () -> {
                try {
                    startParseProcess(askForFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    );

    private String askForFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Parse");
        Path selectedFile = fileChooser.showOpenDialog(getAssociatedStage()).toPath();
        return new String(Files.readAllBytes(selectedFile));
    }

    private void primeInputBoxes(String cardText) {
        CardPossibilitiesTask cardPossibilitiesTask = new CardPossibilitiesTask(cardText);
        CardPossibilities cardPossibilities = cardPossibilitiesTask.call();
        cardPossibilities.getAuthors().forEach(author -> {
            System.out.println(author.getFullName());
        });
        cardPossibilities.getDates().forEach(calendar -> {
            System.out.println(calendar.get(Calendar.YEAR));
        });
    }

    private void setOnKeySubmission(Node node, EventHandler<KeyEvent> eventHandler) {
        node.setOnKeyPressed(keyEvent -> {
            if (PARSE_SELECTED_TEXT.match(keyEvent))
                eventHandler.handle(keyEvent);
        });
    }

    private void clearSelection() {
        documentDisplay.getEngine().executeScript("window.getSelection().removeAllRanges()");
    }

    private void parseNextCard() {
        useCardSelectionStyling();
        setOnKeySubmission(documentDisplay, cardParseKeyEvent -> {
            Card toBeParsedCard = new Card();

            // use the stanford api + selected text here
            primeInputBoxes(getSelectedText());

            // Clear selection so the user can reselect the card content
            clearSelection();

            useContentSelectionStyling();
            setOnKeySubmission(documentDisplay, contentParseKeyEvent -> {
                toBeParsedCard.setCardContent(new CardContent(getSelectedText()));
            });
        });
    }

    public static final KeyCodeCombination LOAD_FILE = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination PARSE_SELECTED_TEXT = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_NEXT_LINE = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination UNSELECT_PREVIOUS_LINE = new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_NEXT_CHARACTER = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination UNSELECT_PREVIOUS_CHARACTER = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN);

    private String getSelectedText() {
        return (String) documentDisplay.getEngine().executeScript("window.getSelection().toString()");
    }

    private void generateOptionContentLabel(String string) {

    }

    private void setOptions(VBox optionBox, List<String> viableOptions) {
        for (int index = 0; index < viableOptions.size(); index++) {
            Label label = new Label(String.valueOf(index));
            HBox viableOptionContainer = new HBox();
        }
    }

    public void startParseProcess(String htmlString) {
        documentDisplay.getEngine().loadContent(htmlString);
    }

    private void resetRequests() {

    }

    private Label generateLabel(String string) {
        return new Label(string);
    }

    private TextField generateTextField(Consumer<String> postActionConsumer) {
        TextField textField = new TextField();
        textField.setOnAction(event -> {
            postActionConsumer.accept(textField.getText());
            if (Utils.isLastElement(getRequests(), getSelectedRequest())) {
                // getParsedCards().add(parsedCards);
                resetRequests();
            }
            else
                setSelectedRequest(Utils.getRelativeElement(getRequests(), getSelectedRequest(), 1));
        });
        return textField;
    }

    private HBox generateInputRequest(String string, Consumer<String> postActionConsumer) {
        HBox inputHBox = new HBox();
        inputHBox.getChildren().addAll(generateLabel(string), generateTextField(postActionConsumer));
        return inputHBox;
    }

    public HBox getSelectedRequest() {
        return selectedRequest;
    }

    public void setSelectedRequest(HBox selectedRequest) {
        this.selectedRequest = selectedRequest;
    }

    public ObservableSet<Card> getParsedCards() {
        return parsedCards;
    }

    public void setParsedCards(ObservableSet<Card> parsedCards) {
        this.parsedCards = parsedCards;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BorderPane getCorrelatingView() {
        return cardParserArea;
    }

    public void finish() {

    }

    public void skip() {

    }

    public List<HBox> getRequests() {
        return requests;
    }

    public void setRequests(List<HBox> requests) {
        this.requests = requests;
    }

    public Stage getAssociatedStage() {
        return associatedStage;
    }

    public void setAssociatedStage(Stage associatedStage) {
        this.associatedStage = associatedStage;
    }

}
