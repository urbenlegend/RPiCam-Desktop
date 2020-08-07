package com.rpicam.javafx.util;

import java.util.LinkedList;
import java.util.List;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class SelectionGroup {
    private SimpleListProperty<Selectable> selectedItems = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<Selectable>()));
    
    public List<Selectable> getSelectedItems() {
        return selectedItems.get();
    }
    
    public void setSelectedItems(Selectable... items) {
        for (var i : items) {
            i.selectedProperty().set(true);
        }
        selectedItems.setAll(items);
    }
    
    public ListProperty<Selectable> selectedItemsProperty() {
        return selectedItems;
    }
    
    public void select(Selectable item, SelectMode mode) {
        switch (mode) {
            case OFF -> {
                selectedItems.remove(item);
            }
            case SINGLE -> {
                unselectAll();
                
                item.selectedProperty().set(true);
                selectedItems.add(item);
            }
            case APPEND -> {
                if (selectedItems.contains(item)) {
                    item.selectedProperty().set(false);
                    selectedItems.remove(item);
                }
                else {
                    item.selectedProperty().set(true);
                    selectedItems.add(item);
                }
            }
        }
    }
    
    public void unselectAll() {
        selectedItems.forEach(i -> {
            i.selectedProperty().set(false);
        });
        selectedItems.clear();
    }
}
