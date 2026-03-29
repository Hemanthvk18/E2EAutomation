package pages;

public class ColumnMeta {
    private String colId;
    private String elementType;
    private boolean editable;

    public ColumnMeta(String colId, String elementType, boolean editable) {
        this.colId = colId;
        this.elementType = elementType;
        this.editable = editable;
    }


    public boolean isEditable() {
        return editable;
    }


    public String getColId() {
        return colId;
    }


    public String getElementType() {
        return elementType;
    }

}