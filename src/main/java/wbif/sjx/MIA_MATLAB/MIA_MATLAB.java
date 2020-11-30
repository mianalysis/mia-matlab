package wbif.sjx.MIA_MATLAB;

import wbif.sjx.MIA.MIA;

public class MIA_MATLAB {
    public static void main(String[] args) throws Exception {
        MIA.addPluginPackageName(FitAlphaSurface.class.getCanonicalName());
        MIA.addPluginPackageName(SortStack.class.getCanonicalName());
        MIA.main(new String[]{});

    }
}
