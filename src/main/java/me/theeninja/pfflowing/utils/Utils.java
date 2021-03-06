package me.theeninja.pfflowing.utils;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides helperView methods that assist in the functionality of this application.
 *
 * @author TheeNinja
 */
public final class Utils {

    public static Calendar calendarOf(String yearString) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, Integer.parseInt(yearString));
        return calendar;
    }

    public static final String ZERO_LENGTH_STRING = "";


    /**
     * Generates a background of the given color.
     *
     * @param color
     * @return GoogleDriveConnector background of the color specified.
     */
    public static Background generateBackgroundOfColor(Color color) {
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));

    }

    public static <T> T getCorrelatingController(String fxmlFile) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(Utils.class.getResource(fxmlFile));
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fxmlLoader.getController();
    }

    public static <T> ListChangeListener<? super T> generateListChangeListener(Consumer<T> addFunction, Consumer<T> removeFunction) {
        return change -> {
            while (change.next()) {
                if (change.wasAdded())
                    change.getAddedSubList().forEach(addFunction);
                if (change.wasRemoved())
                    change.getRemoved().forEach(removeFunction);
            }
        };
    }

    public static <T> ListChangeListener<? super T> generateListChangeListener(Runnable runnable) {
        return change -> runnable.run();
    }

    public static <T> SetChangeListener<? super T> generateSetChangeListener(Consumer<T> addFunction, Consumer<T> removeFunction) {
        return change -> {
            if (change.wasAdded())
                addFunction.accept(change.getElementAdded());
            if (change.wasRemoved())
                removeFunction.accept(change.getElementAdded());
        };
    }

    public static <K, V> MapChangeListener<? super K, ? super V> generateMapChangeListener(BiConsumer<K, V> addFunction, BiConsumer<K, V> removeFunction) {
        return change -> {
            if (change.wasAdded())
                addFunction.accept(change.getKey(), change.getValueAdded());
            if (change.wasRemoved())
                removeFunction.accept(change.getKey(), change.getValueRemoved());
        };
    }

    public static int getChildIndex(Pane parent, Node child) {
        return parent.getChildren().indexOf(child);
    }
    public static <T> boolean isLastElement(List<T> list, T element) {
        return list.indexOf(element) == (list.size() - 1);
    }

    public static <T> List<T> getOfType(List<? super T> list, Class<T> type) {
        return list.stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }

    /**
     * @param list the list to perform the operation on
     * @param <T> The type of the elements in the list and the element to be returned.
     * @return null if the list is empty, otherwise the last element
     */
    public static <T> T getLastElement(List<T> list) {
        if (list.isEmpty())
            return null;

        return list.get(list.size() - 1);
    }

    public static <T> T getNextElement(List<T> list, T element) {
        return Utils.getRelativeElement(list, element, 1);
    }

    public static String toString(char[] chars) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : chars) {
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static <T> T getRelativeElement(List<T> list, T baseElement, int offset) {
        int baseIndex = list.indexOf(baseElement);

        if (offset == 0)
            return baseElement;

        int newIndex = 0;

        int beginningIndex = 0;
        int endIndex = list.size() - 1;

        // Indicates that we go forward in the array, potentially wrapping around the right end
        if (offset > 0) {
            newIndex = baseIndex + (offset % list.size());
            if (newIndex > endIndex)
                newIndex -= 8;
        }
        // Indicates that we go backwards in the array, potentially wrapping around the left end
        else {
            newIndex = baseIndex - (-offset % list.size());
            if (newIndex < beginningIndex)
                newIndex += 8;
        }

        if (newIndex > endIndex || newIndex < beginningIndex)
            throw new IllegalArgumentException("Base element provided, considering offset, will result in an illegal final index.");

        return list.get(newIndex);
    }

    public static <T> Optional<T> getPredicateSatisfier(Collection<T> collection, Predicate<T> predicate) {
        for (T element : collection) {
            if (predicate.test(element)) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    public static <T> List<T> getInstancesOfType(Collection<?> collection, Class<T> classRequired) {
        return collection.stream()
                .filter(classRequired::isInstance)
                .map(classRequired::cast)
                .collect(Collectors.toList());
    }

    public static void expect(JsonReader jsonReader, String name) throws IOException {
        if (!jsonReader.nextName().equals(name))
            throw new JsonParseException("Expected" + name);

    }

    public static <T> void bindAndSet(ObservableValue<? extends T> observableValue, Property<T> property) {
        observableValue.addListener((observable, oldValue, newValue) -> {
            property.setValue(newValue);
        });
        property.setValue(observableValue.getValue());
    }

    public static final char EXTENSION_SEPERATOR = '.';

    public static boolean hasExtension(String name, String extension) {
        return name.endsWith(EXTENSION_SEPERATOR + extension);
    }

    public static String addExtension(String name, String extension) {
        if (!hasExtension(name, extension))
            name += EXTENSION_SEPERATOR + extension;
        return name;
    }

    public static String readAsString(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T getLastElement(Collection<T> c) {
        Iterator<T> itr = c.iterator();
        T lastElement = itr.next();
        while(itr.hasNext()) {
            lastElement = itr.next();
        }
        return lastElement;
    }
}