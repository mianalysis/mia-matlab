/*
 * MATLAB Compiler: 6.2 (R2016a)
 * Date: Fri Mar 01 18:54:21 2019
 * Arguments: "-B" "macro_default" "-W" "java:MIA_MATLAB,AlphaShape" "-T" "link:lib" "-d" 
 * "C:\\Users\\steph\\Documents\\Java 
 * Projects\\MIA_MATLAB\\MATLAB\\MIA_MATLAB\\for_testing" 
 * "class{AlphaShape:C:\\Users\\steph\\Documents\\Java 
 * Projects\\MIA_MATLAB\\MATLAB\\fitAlphaSurface.m,C:\\Users\\steph\\Documents\\Java 
 * Projects\\MIA_MATLAB\\MATLAB\\fitAlphaSurfaceAuto.m}" 
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
    private static final String sComponentId = "MIA_MATLAB_52A0070654D16C6433777C594F291BF3";
    
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
            new int[]{9,0,1}
        );
    }
    
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
