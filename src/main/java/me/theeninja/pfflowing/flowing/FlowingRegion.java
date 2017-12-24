package me.theeninja.pfflowing.flowing;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.Configuration;
import me.theeninja.pfflowing.gui.FlowingColumn;

public class FlowingRegion extends Label implements Identifiable {
    private static int currentID = 0;
    private int id;

    private final String representation;

    public FlowingRegion(String representation) {
        super(representation);
        this.representation = representation;
        this.id = currentID++;
        this.setFont(Configuration.FONT);
    }

    @Override
    public int getID() {
        return id;
    }

    public String getRepresentation() {
        return representation;
    }

    public FlowingColumn getFlowingColumn() {
        return (FlowingColumn) getContainer().getParent();
    }

    public VBox getContainer() {
        Parent parent = getParent();
        if (parent instanceof Group)
            return (VBox) parent.getParent();
        else
            return (VBox) parent;
    }
}
