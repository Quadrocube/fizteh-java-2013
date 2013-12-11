package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.tests;

import java.io.IOException;

import org.junit.*;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.AtomicTableProviderFactory;
import ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.FileMapProviderFactory;

public class FileMapProviderFactoryTester {
    public AtomicTableProviderFactory factory;
    
    @Before
    public void init() {
        factory = new FileMapProviderFactory();
    }
    
    @Test
    public void legalCreateTest() throws IOException {
        TableProvider provider = factory.create(System.getProperty("user.dir"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void nullCreateTestShouldFail() throws IOException {
        TableProvider provider = factory.create(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void emptyCreateTestShouldFail() throws IOException {
        TableProvider provider = factory.create("");
    }
}
