package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap;

import java.io.IOException;

public interface AtomicTableProviderFactory {
    /**
     * Возвращает объект для работы с базой данных.
     *
     * @param path Директория с файлами базы данных.
     * @return Объект для работы с базой данных, который будет работать в указанной директории.
     *
     * @throws IllegalArgumentException Если значение директории null или имеет недопустимое значение.
     * @throws java.io.IOException В случае ошибок ввода/вывода.
     */
    AtomicTableProvider create(String path) throws IOException;
}
