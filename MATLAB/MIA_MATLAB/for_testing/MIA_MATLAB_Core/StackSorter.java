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
import java.util.*;
import java.io.Serializable;

/**
 * The <code>StackSorter</code> class provides a Java interface to MATLAB functions. 
 * The interface is compiled from the following files:
 * <pre>
 *  F:\\Java Projects\\mia-matlab\\MATLAB\\SortStack\\getOptimisedOrder.m
 * </pre>
 * The {@link #dispose} method <b>must</b> be called on a <code>StackSorter</code> 
 * instance when it is no longer needed to ensure that native resources allocated by this 
 * class are properly freed.
 * @version 0.0
 */
public class StackSorter extends MWComponentInstance<StackSorter> implements Serializable
{
    /**
     * Tracks all instances of this class to ensure their dispose method is
     * called on shutdown.
     */
    private static final Set<Disposable> sInstances = new HashSet<Disposable>();

    /**
     * Maintains information used in calling the <code>getOptimisedOrder</code> MATLAB 
     *function.
     */
    private static final MWFunctionSignature sGetOptimisedOrderSignature =
        new MWFunctionSignature(/* max outputs = */ 1,
                                /* has varargout = */ false,
                                /* function name = */ "getOptimisedOrder",
                                /* max inputs = */ 2,
                                /* has varargin = */ false);

    /**
     * Shared initialization implementation - private
     * @throws MWException An error has occurred during the function call.
     */
    private StackSorter (final MWMCR mcr) throws MWException
    {
        super(mcr);
        // add this to sInstances
        synchronized(StackSorter.class) {
            sInstances.add(this);
        }
    }

    /**
     * Constructs a new instance of the <code>StackSorter</code> class.
     * @throws MWException An error has occurred during the function call.
     */
    public StackSorter() throws MWException
    {
        this(MIA_MATLAB_CoreMCRFactory.newInstance());
    }
    
    private static MWComponentOptions getPathToComponentOptions(String path)
    {
        MWComponentOptions options = new MWComponentOptions(new MWCtfExtractLocation(path),
                                                            new MWCtfDirectorySource(path));
        return options;
    }
    
    /**
     * @deprecated Please use the constructor {@link #StackSorter(MWComponentOptions componentOptions)}.
     * The <code>com.mathworks.toolbox.javabuilder.MWComponentOptions</code> class provides an API to set the
     * path to the component.
     * @param pathToComponent Path to component directory.
     * @throws MWException An error has occurred during the function call.
     */
    @Deprecated
    public StackSorter(String pathToComponent) throws MWException
    {
        this(MIA_MATLAB_CoreMCRFactory.newInstance(getPathToComponentOptions(pathToComponent)));
    }
    
    /**
     * Constructs a new instance of the <code>StackSorter</code> class. Use this 
     * constructor to specify the options required to instantiate this component.  The 
     * options will be specific to the instance of this component being created.
     * @param componentOptions Options specific to the component.
     * @throws MWException An error has occurred during the function call.
     */
    public StackSorter(MWComponentOptions componentOptions) throws MWException
    {
        this(MIA_MATLAB_CoreMCRFactory.newInstance(componentOptions));
    }
    
    /** Frees native resources associated with this object */
    public void dispose()
    {
        try {
            super.dispose();
        } finally {
            synchronized(StackSorter.class) {
                sInstances.remove(this);
            }
        }
    }
    
    /**
     * Calls dispose method for each outstanding instance of this class.
     */
    public static void disposeAllInstances()
    {
        synchronized(StackSorter.class) {
            for (Disposable i : sInstances) i.dispose();
            sInstances.clear();
        }
    }

    /**
     * Provides the interface for calling the <code>getOptimisedOrder</code> MATLAB function 
     * where the first argument, an instance of List, receives the output of the MATLAB function and
     * the second argument, also an instance of List, provides the input to the MATLAB function.
     * <p>
     * Description as provided by the author of the MATLAB function:
     * </p>
     * <pre>
     * {@literal
	 * % The number of images in the stack
	 * }
     * </pre>
     * @param lhs List in which to return outputs. Number of outputs (nargout) is
     * determined by allocated size of this List. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs List containing inputs. Number of inputs (nargin) is determined
     * by the allocated size of this List. Input arguments may be passed as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or
     * as arrays of any supported Java type. Arguments passed as Java types are
     * converted to MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void getOptimisedOrder(List lhs, List rhs) throws MWException
    {
        fMCR.invoke(lhs, rhs, sGetOptimisedOrderSignature);
    }

    /**
     * Provides the interface for calling the <code>getOptimisedOrder</code> MATLAB function 
     * where the first argument, an Object array, receives the output of the MATLAB function and
     * the second argument, also an Object array, provides the input to the MATLAB function.
     * <p>
     * Description as provided by the author of the MATLAB function:
     * </p>
     * <pre>
     * {@literal
	 * % The number of images in the stack
	 * }
	 * </pre>
     * @param lhs array in which to return outputs. Number of outputs (nargout)
     * is determined by allocated size of this array. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs array containing inputs. Number of inputs (nargin) is
     * determined by the allocated size of this array. Input arguments may be
     * passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void getOptimisedOrder(Object[] lhs, Object[] rhs) throws MWException
    {
        fMCR.invoke(Arrays.asList(lhs), Arrays.asList(rhs), sGetOptimisedOrderSignature);
    }

    /**
     * Provides the standard interface for calling the <code>getOptimisedOrder</code> MATLAB function with 
     * 2 comma-separated input arguments.
     * Input arguments may be passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     *
     * <p>
     * Description as provided by the author of the MATLAB function:
     * </p>
     * <pre>
     * {@literal
	 * % The number of images in the stack
	 * }
     * </pre>
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the MATLAB function.
     * @return Array of length nargout containing the function outputs. Outputs
     * are returned as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>. Each output array
     * should be freed by calling its <code>dispose()</code> method.
     * @throws MWException An error has occurred during the function call.
     */
    public Object[] getOptimisedOrder(int nargout, Object... rhs) throws MWException
    {
        Object[] lhs = new Object[nargout];
        fMCR.invoke(Arrays.asList(lhs), 
                    MWMCR.getRhsCompat(rhs, sGetOptimisedOrderSignature), 
                    sGetOptimisedOrderSignature);
        return lhs;
    }
}
