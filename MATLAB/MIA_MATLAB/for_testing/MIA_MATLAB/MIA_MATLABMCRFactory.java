/*
 * MATLAB Compiler: 7.0 (R2018b)
 * Date: Wed Apr  3 15:20:18 2019
 * Arguments: 
 * "-B""macro_default""-W""java:MIA_MATLAB,AlphaShape""-T""link:lib""-d""F:\\JavaMATLABProjects\\MIA_MATLAB\\MATLAB\\MIA_MATLAB\\for_testing""class{AlphaShape:F:\\JavaMATLABProjects\\MIA_MATLAB\\MATLAB\\AlphaShape\\fitAlphaSurface.m,F:\\JavaMATLABProjects\\MIA_MATLAB\\MATLAB\\AlphaShape\\fitAlphaSurfaceAuto.m}"
 */

package MIA_MATLAB;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;

/**
 * <i>INTERNAL USE ONLY</i>
 */
public class MIA_MATLABMCRFactory
{
   
    
    /** Component's uuid */
    private static final String sComponentId = "MIA_MATLAB_AA1FC8D4A37A256F41A82CBDE6DD6B64";
    
    /** Component name */
    private static final String sComponentName = "MIA_MATLAB";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(MIA_MATLABMCRFactory.class)
        );
    
    
    private MIA_MATLABMCRFactory()
    {
        // Never called.
    }
    
    public static MWMCR newInstance(MWComponentOptions componentOptions) throws MWException
    {
        if (null == componentOptions.getCtfSource()) {
            componentOptions = new MWComponentOptions(componentOptions);
            componentOptions.setCtfSource(sDefaultComponentOptions.getCtfSource());
        }
        return MWMCR.newInstance(
            componentOptions, 
            MIA_MATLABMCRFactory.class, 
            sComponentName, 
            sComponentId,
            new int[]{9,5,0}
        );
    }
    
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
