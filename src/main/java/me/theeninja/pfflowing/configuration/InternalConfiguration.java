package me.theeninja.pfflowing.configuration;

import com.google.common.collect.ImmutableMap;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import me.theeninja.pfflowing.gui.LengthLimitType;
import me.theeninja.pfflowing.utils.Pair;

import java.text.SimpleDateFormat;
import java.util.logging.Level;

/**
 * Holds constants or options specified by the user. These constants will apply to all projects, and thus
 * are not project-specific.
 */
public class InternalConfiguration {
    /**
     * Represents the amount of pixels between each speech (on the actions grid, each column).
     * The intention is to provide a clearer UI if the user prefers seperation.
     */
    public static final double SPEECH_SEPERATION = 30;

    public static final int NUMBER_OF_SPEECHES_PER_DISPLAY = 8;


    /**
     * Represents the key used to quick-selectSpeech a card.
     */
    public static final String CARD_SELECTOR = "\\";

    /**
     * Represents the date format to use when displaying the dates of cards to the user.
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Represents the seperator to be used when merging the condensing of multiple actions regions.
     */
    public static final String MERGE_SEPERATOR = ". ";

    public static final LengthLimitType LENGTH_LIMIT_TYPE = LengthLimitType.WORD;
    public static final int LENGTH_LIMIT = 12;

    public static final ImmutableMap<Level, Pair<Color, Color>> LEVEL_COLORS = ImmutableMap.of(
        Level.INFO, new Pair<>(Color.BLACK, Color.LIGHTGREEN),
        Level.WARNING, new Pair<>(Color.BLACK, Color.YELLOW),
        Level.SEVERE, new Pair<>(Color.WHITE, Color.RED)
    );
}
