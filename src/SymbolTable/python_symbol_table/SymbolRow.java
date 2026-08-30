package SymbolTable.python_symbol_table;

import java.util.HashMap;

public class SymbolRow {
    private final String name;
    private String type;
    private int line;
    private final HashMap<String, Object> attributes = new HashMap<>();

    public SymbolRow(String name) {
        this.name = name;
    }


    public String getName()              { return name; }
    public String getType()              { return type; }
    public void   setType(String type)   { this.type = type; }
    public int    getLine()              { return line; }
    public void   setLine(int line)      { this.line = line; }

    public void   setAttribute(String key, Object value) { attributes.put(key, value); }
    public Object getAttribute(String key)               { return attributes.get(key); }
    public HashMap<String, Object> getAttributes()       { return attributes; }

    @Override
    public String toString() {
        return String.format("%s (type=%s, line=%d, attrs=%s)", name, type, line, attributes);
    }
}