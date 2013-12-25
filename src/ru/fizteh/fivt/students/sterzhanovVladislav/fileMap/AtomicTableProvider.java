package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.IOException;

import ru.fizteh.fivt.storage.structured.TableProvider;

public interface AtomicTableProvider extends TableProvider, AutoCloseable {

    /**
     * Закрывает таблицу, если в ней нет несохранённых изменений.
     */
    void closeTableIfNotModified(String name) throws IllegalStateException, IOException;

}
