/*
 * MATLAB Compiler: 8.6 (R2023a)
 * Date: Tue Jun 17 13:00:40 2025
 * Arguments: 
 * "-B""macro_default""-W""java:MIA_MATLAB_Core,AlphaShapeFitter""-T""link:lib""-d""/Users/sc13967/Documents/Programming/Java/mia-matlab/MATLAB/MIA_MATLAB/for_testing""class{AlphaShapeFitter:/Users/sc13967/Documents/Programming/Java/mia-matlab/MATLAB/AlphaShape/fitAlphaSurface.m,/Users/sc13967/Documents/Programming/Java/mia-matlab/MATLAB/AlphaShape/fitAlphaSurface2D.m,/Users/sc13967/Documents/Programming/Java/mia-matlab/MATLAB/AlphaShape/fitAlphaSurface3D.m,/Users/sc13967/Documents/Programming/Java/mia-matlab/MATLAB/AlphaShape/fitAlphaSurfaceAuto.m,/Users/sc13967/Documents/Programming/Java/mia-matlab/MATLAB/StackMatcher/matchImageInStack.m}""class{StackSorter:/Users/sc13967/Documents/Programming/Java/mia-matlab/MATLAB/SortStack/getOptimisedOrder.m}""class{StackMatcher:/Users/sc13967/Documents/Programming/Java/mia-matlab/MATLAB/StackMatcher/matchImageInStack.m}""class{ActiveContourFitter:/Users/sc13967/Documents/Programming/Java/mia-matlab/MATLAB/ActiveContours/fitActiveContour.m}"
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
    private static final String sComponentId = "MIA_MATLAB_C_a1a22246-4204-4eb5-b90f-f2e9a56773ae";
    
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
