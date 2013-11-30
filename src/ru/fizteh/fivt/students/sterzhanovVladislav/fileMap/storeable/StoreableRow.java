package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable;

import java.util.List;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;

public class StoreableRow implements Storeable {
    
    private Object[] row;
    private List<Class<?>> signature;

    @Override
    public void setColumnAt(int columnIndex, Object value)
            throws ColumnFormatException, IndexOutOfBoundsException {
        if (columnIndex < 0 || columnIndex >= row.length) {
            throw new IndexOutOfBoundsException();
        }
        if (value == null) {
            row[columnIndex] = null;
        } else {
            if (signature.get(columnIndex) != value.getClass()) {
                throw new ColumnFormatException();
            } else {
                row[columnIndex] = value;
            }
        }
    }

    @Override
    public Object getColumnAt(int columnIndex) throws IndexOutOfBoundsException {
        if (columnIndex < 0 || columnIndex >= row.length) {
            throw new IndexOutOfBoundsException();
        }
        return row[columnIndex];
    }
    
    private Object getObjAt(int columnIndex, Class<?> type) 
            throws IndexOutOfBoundsException, ColumnFormatException {
        Object obj = getColumnAt(columnIndex);
        if (!signature.get(columnIndex).equals(type)) {
            throw new ColumnFormatException("wrong type (expected " + type.toString() 
                    + ", but got " + signature.get(columnIndex).toString() + ")");
        }
        return obj;
    }

    @Override
    public Integer getIntAt(int columnIndex) throws ColumnFormatException,
            IndexOutOfBoundsException {
        return (Integer) getObjAt(columnIndex, Integer.class);
    }

    @Override
    public Long getLongAt(int columnIndex) throws ColumnFormatException,
            IndexOutOfBoundsException {
        return (Long) getObjAt(columnIndex, Long.class);
    }

    @Override
    public Byte getByteAt(int columnIndex) throws ColumnFormatException,
            IndexOutOfBoundsException {
        return (Byte) getObjAt(columnIndex, Byte.class);
    }

    @Override
    public Float getFloatAt(int columnIndex) throws ColumnFormatException,
            IndexOutOfBoundsException {
        return (Float) getObjAt(columnIndex, Float.class);
    }

    @Override
    public Double getDoubleAt(int columnIndex) throws ColumnFormatException,
            IndexOutOfBoundsException {
        return (Double) getObjAt(columnIndex, Double.class);
    }

    @Override
    public Boolean getBooleanAt(int columnIndex) throws ColumnFormatException,
            IndexOutOfBoundsException {
        return (Boolean) getObjAt(columnIndex, Boolean.class);
    }

    @Override
    public String getStringAt(int columnIndex) throws ColumnFormatException,
            IndexOutOfBoundsException {
        return (String) getObjAt(columnIndex, String.class);
    }
    
    @Override
    public boolean equals(Object object) {
        if (!Storeable.class.isAssignableFrom(object.getClass())) {
            return false;
        }
        Storeable other = (Storeable) object;
        for (int elementID = 0; elementID < this.row.length; ++elementID) {
            try {
                Object thisValue = this.row[elementID];
                Object otherValue = other.getColumnAt(elementID);
                if ((thisValue == null && otherValue != null) 
                        || (thisValue != null && otherValue == null)) {
                    return false;
                }
                if (thisValue == null && otherValue == null) {
                    continue;
                }
                if (!thisValue.equals(otherValue)) {
                    return false;
                }
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hashcode = 0;
        for (Object field : row) {
            if (field != null) {
                hashcode ^= field.hashCode();
            } else {
                hashcode ^= 0;
            }
        }
        return hashcode;
    }
    
    public StoreableRow(List<Class<?>> signature) {
        if (signature == null) {
            throw new IllegalArgumentException("Got null signature");
        }
        this.row = new Object[signature.size()];
        this.signature = signature;
    }

    public StoreableRow(List<Class<?>> signature, List<?> values) {
        this(signature);
        if (signature.size() != values.size()) {
            throw new IndexOutOfBoundsException();
        }
        for (int valueID = 0; valueID < values.size(); ++valueID) {
            Object nextValue = values.get(valueID);
            if (nextValue != null && nextValue.getClass() != signature.get(valueID)) {
                throw new ColumnFormatException();
            }
            row[valueID] = nextValue;
        }
    }
    
    @Override
    public String toString() {
        String result = "";
        result += this.getClass().getSimpleName();
        result += "[";             
        for (Object item : row) {
            if (item != null) {
                result += item;
            }
            result += ",";
        }
        result = result.substring(0, result.length() - 1);
        result += "]";
        return result;
    }

}
