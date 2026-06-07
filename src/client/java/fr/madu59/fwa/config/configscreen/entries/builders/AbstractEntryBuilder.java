package fr.madu59.fwa.config.configscreen.entries.builders;

import java.util.function.BooleanSupplier;

import fr.madu59.fwa.config.configscreen.MyConfigListWidget;

public abstract class AbstractEntryBuilder{
    protected MyConfigListWidget parent;
    protected String name;
    protected BooleanSupplier isEnabledSupplier = () -> true;
    
    protected String getDefaultIndent(){
        return " ⤷  ";
    }
}