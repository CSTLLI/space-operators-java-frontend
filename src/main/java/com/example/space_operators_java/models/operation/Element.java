package com.example.space_operators_java.models.operation;

public class Element {
    private String type;
    private int id;
    private String valueType;
    private Object value; // Changé de String à Object

    public Element() {}

    public Element(String type, int id, String valueType, Object value) {
        this.type = type;
        this.id = id;
        this.valueType = valueType;
        this.value = value;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    // Méthodes utilitaires pour faciliter l'utilisation
    public String getValueAsString() {
        return value != null ? value.toString() : "";
    }

    public boolean getValueAsBoolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        } else if (value instanceof String) {
            String str = (String) value;
            return "true".equalsIgnoreCase(str) || "on".equalsIgnoreCase(str) || "1".equals(str);
        }
        return false;
    }

    public int getValueAsInt() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Element{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", valueType='" + valueType + '\'' +
                ", value=" + value +
                " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")" +
                '}';
    }
}