package com.agvahealthcare.ventilator_ext.model;

/**
 * Created by MOHIT MALHOTRA on 11-09-2018.
 */

// PrimaryObservedTile
public class ObservedParameterModel {

    private String label;
    private String labelSubscript;
    private String units;
    private String actualValue;
    private String setValue;
    private String lowerLimitValue;
    private String upperLimitValue;
    private boolean setValueRequired;
    private boolean isVisible;
    private boolean isValueValid = true;  // DEFAULT
    private boolean isWarningAvailable = false;  // DEFAULT
    private boolean isSelected = false;  // DEFAULT

    private boolean isSelectedAsSwappable = false;

    public ObservedParameterModel() {

    }

    public ObservedParameterModel(String label, String labelSubscript, String units, String actualValue, String setValue, String lowerLimitValue, String upperLimitValue) {
        this.label = label;
        this.labelSubscript = labelSubscript;
        this.units = units;
        this.actualValue = actualValue;
        this.setValue = setValue;
        this.lowerLimitValue = lowerLimitValue;
        this.upperLimitValue = upperLimitValue;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelSubscript() {
        return labelSubscript;
    }

    public void setLabelSubscript(String labelSubscript) {
        this.labelSubscript = labelSubscript;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public String getSetValue() {
        return setValue;
    }

    public void setSetValue(String setValue) {
        this.setValue = setValue;
        setValueRequired = !(setValue == null || setValue.trim().length() == 0);
    }

    public boolean isSetValueRequired() {
        return setValueRequired;
    }

    public void setSetValueRequired(boolean setValueRequired) {
        this.setValueRequired = setValueRequired;
    }

    public boolean isValueValid() {
        return isValueValid;
    }

    public void setValueValid(boolean valueValid) {
        this.isValueValid = valueValid;
    }

    public boolean isValueWarning() {
        return isWarningAvailable;
    }

    public void setValueWarning(boolean isWarningAvailable) {
        this.isWarningAvailable = isWarningAvailable;
    }

    public String getLowerLimitValue() {
        return lowerLimitValue;
    }

    public void setLowerLimitValue(String lowerLimitValue) {
        this.lowerLimitValue = lowerLimitValue;
    }

    public String getUpperLimitValue() {
        return upperLimitValue;
    }

    public void setUpperLimitValue(String upperLimitValue) {
        this.upperLimitValue = upperLimitValue;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelectedAsSwappable() {
        return isSelectedAsSwappable;
    }

    public void setSelectedAsSwappable(boolean selectedAsSwappable) {
        isSelectedAsSwappable = selectedAsSwappable;
    }
}
