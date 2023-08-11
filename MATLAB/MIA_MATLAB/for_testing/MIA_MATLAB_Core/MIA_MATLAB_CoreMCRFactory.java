/*
 * MATLAB Compiler: 8.6 (R2023a)
 * Date: Sat Aug  5 01:24:45 2023
 * Arguments: 
 * "-B""macro_default""-W""java:MIA_MATLAB_Core,AlphaShapeFitter""-T""link:lib""-d""F:\\Java 
 * Projects\\mia-matlab\\MATLAB\\MIA_MATLAB\\for_testing""class{AlphaShapeFitter:F:\\Java 
 * Projects\\mia-matlab\\MATLAB\\AlphaShape\\fitAlphaSurface.m,F:\\Java 
 * Projects\\mia-matlab\\MATLAB\\AlphaShape\\fitAlphaSurface2D.m,F:\\Java 
 * Projects\\mia-matlab\\MATLAB\\AlphaShape\\fitAlphaSurface3D.m,F:\\Java 
 * Projects\\mia-matlab\\MATLAB\\AlphaShape\\fitAlphaSurfaceAuto.m,F:\\Java 
 * Projects\\mia-matlab\\MATLAB\\StackMatcher\\matchImageInStack.m}""class{StackSorter:F:\\Java 
 * Projects\\mia-matlab\\MATLAB\\SortStack\\getOptimisedOrder.m}""class{StackMatcher:F:\\Java 
 * Projects\\mia-matlab\\MATLAB\\StackMatcher\\matchImageInStack.m}"
 */

package MIA_MATLAB_Core;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;
import java.io.Serializable;
/**
 * <i>INTERNAL USE ONLY</i>
 */
public class MIA_MATLAB_CoreMCRFactory implements Serializable 
{
    /** Component's uuid */
    private static final String sComponentId = "MIA_MATLAB_C_12d29241-21b1-4c8b-9b83-37fcac5db5c4";
    
    /** Component name */
    private static final String sComponentName = "MIA_MATLAB_Core";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(MIA_MATLAB_CoreMCRFactory.class)
        );
    
    
    private MIA_MATLAB_CoreMCRFactory()
    {
        // Never called.
    }
    
    /**
     * Create a MWMCR instance with the required options.
     * @param componentOptions Options applied to the component.
     * @return A shared MCR instance
     * @throws MWException An error has occurred during the function call.
     */
    public static MWMCR newInstance(MWComponentOptions componentOptions) throws MWException
    {
        if (null == componentOptions.getCtfSource()) {
            componentOptions = new MWComponentOptions(componentOptions);
            componentOptions.setCtfSource(sDefaultComponentOptions.getCtfSource());
        }
        return MWMCR.newInstance(
            componentOptions, 
            MIA_MATLAB_CoreMCRFactory.class, 
            sComponentName, 
            sComponentId,
            new int[]{9,14,0}
        );
    }
    
    /**
     * Create a MWMCR instance with the default options
     * @return A MCR instance
     * @throws MWException An error has occurred during the function call.
     */
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
