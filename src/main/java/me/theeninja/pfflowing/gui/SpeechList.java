package me.theeninja.pfflowing.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;
import me.theeninja.pfflowing.utils.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpeechList extends ArrayList<SpeechPair> {
    private final Side side;
    private ObjectProperty<Speech> selectedSpeech = new SimpleObjectProperty<>();

    public static final Map<Side, String> SIDE_HEADERS = Map.of(
            Side.AFFIRMATIVE, "Aff",
            Side.NEGATION, "Neg"
    );
    public static final String REF_PREFIX = "AT";
    public static final String PREFIX_HEADER_SEPERATOR = "-";
    public static final String HEADER_NUMBER_SEPERATOR = " ";

    /**
     * GoogleDriveConnector subround refers to a specific exchange between speakers in a round excluding crossfires. There are 4:
     * <ol>
     *     <li>Constructive Speeches</li>
     *     <li>Rebuttal Speeches</li>
     *     <li>Summary Speeches</li>
     *     <li>Final Focus Speeches</li>
     * </ol>
     */
    public static final int NUMBER_OF_SUBROUNDS = 4;

    public SpeechList(Side side) {
        this.side = side;

        for (int subround = 0; subround < NUMBER_OF_SUBROUNDS; subround++) {
            String suffixWithSeperator = HEADER_NUMBER_SEPERATOR + (subround + 1);
            String defHead = SIDE_HEADERS.get(getSide()) + suffixWithSeperator;
            String refHead = REF_PREFIX + PREFIX_HEADER_SEPERATOR + SIDE_HEADERS.get(getSide()) + suffixWithSeperator;
            DefensiveSpeech defensiveSpeech =
                new DefensiveSpeech(getSide(), defHead, subround * 2);
            RefutationSpeech refutationSpeech =
                new RefutationSpeech(defensiveSpeech, getSide().getOpposite(), refHead, subround * 2 + 1);
            add(new SpeechPair(defensiveSpeech, refutationSpeech));
        }

        List<Speech> speeches = getSpeeches();

        for (int column = 1; column < Speech.SPEECH_SIZE; column++) {
            Speech previousSpeech = speeches.get(column - 1);
            Speech currentSpeech = speeches.get(column);

            speeches.get(column).availableRowProperty().bind(
                    previousSpeech.availableRowProperty().add(
                    currentSpeech.defensiveRegionsNumberProperty()));
        }

        Speech firstSpeech = speeches.get(0);
        firstSpeech.availableRowProperty().bind(firstSpeech.defensiveRegionsNumberProperty());
    }

    private Pair<DefensiveSpeech, RefutationSpeech> getPair(Speech speech) {
        for (SpeechPair pair : this) {
            if (pair.contains(speech))
                return pair;
        }
        return null;
    }

    public List<DefensiveSpeech> getDefensiveSpeeches() {
        return stream().map(Pair::getFirst).collect(Collectors.toList());
    }

    public List<RefutationSpeech> getRefutationSpeeches() {
        return stream().map(Pair::getSecond).collect(Collectors.toList());
    }

    public List<Speech> getSpeeches() {
        List<Speech> returnList = new ArrayList<>();
        for (Pair<DefensiveSpeech, RefutationSpeech> pair : this) {
            returnList.add(pair.getFirst());
            returnList.add(pair.getSecond());
        }
        return returnList;
    }
    public ObjectProperty<Speech> selectedSpeechProperty() {
        return selectedSpeech;
    }

    public Speech getSelectedSpeech() {
        return selectedSpeech.get();
    }

    public void setSelectedSpeech(Speech selectedSpeech) {
        this.selectedSpeech.set(selectedSpeech);
    }

    public Optional<Speech> findFirstSpeech(Predicate<Speech> predicate) {
        for (Speech speech : getSpeeches())
            if (predicate.test(speech))
                return Optional.of(speech);
        return Optional.empty();
    }

    public Speech getSpeech(FlowingRegion flowingRegion) {
        for (Speech speech : getSpeeches())
            if (speech.getColumn() == FlowGrid.getColumnIndex(flowingRegion))
                return speech;
        return null;
    }

    public Side getSide() {
        return side;
    }
}
