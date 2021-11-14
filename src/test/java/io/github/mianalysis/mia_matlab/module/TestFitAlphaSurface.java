package io.github.mianalysis.mia_matlab.module;

import static org.junit.Assert.assertNotNull;

public class TestFitAlphaSurface {
    public void testGetHelp() {
        assertNotNull(new FitAlphaSurface(null).getDescription());
    }
}
