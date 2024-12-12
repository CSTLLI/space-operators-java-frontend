package com.example.space_operators_java.models.operation;

// Classes pour les éléments et résultats
public class Element {
    private String type;
    private int id;
    private String valueType;
    private Object value;

    public Element(String type, int id, String valueType, Object value) {
        this.type = type;
        this.id = id;
        this.valueType = valueType;
        this.value = value;
    }

    public String getValueType() {
        // Si valueType n'est pas défini, essayer de déduire le type
        if (valueType == null || valueType.isEmpty()) {
            if (value instanceof Number) {
                return "number";
            } else if (value instanceof String && ((String) value).startsWith("#")) {
                return "color";
            } else {
                return "string";
            }
        }
        return valueType;
    }

    // Getters
    public String getType() { return type; }
    public int getId() { return id; }
    public Object getValue() { return value; }
}
