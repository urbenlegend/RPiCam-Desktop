package com.rpicam.javafx.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

public interface Selectable {
    void select(SelectMode mode);
    
    boolean isSelected();
    BooleanProperty selectedProperty();
    
    SelectionGroup getSelectionGroup();
    void setSelectionGroup(SelectionGroup group);
    ObjectProperty<SelectionGroup> selectionGroupProperty();
}
