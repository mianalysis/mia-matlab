/*
 * MATLAB Compiler: 6.2 (R2016a)
 * Date: Tue Mar 05 18:01:31 2019
 * Arguments: "-B" "macro_default" "-W" "java:MIA_MATLAB,AlphaShape" "-T" "link:lib" "-d" 
 * "C:\\Users\\steph\\Documents\\Java 
 * Projects\\MIA_MATLAB\\MATLAB\\MIA_MATLAB\\for_testing" 
 * "class{AlphaShape:C:\\Users\\steph\\Documents\\Java 
 * Projects\\MIA_MATLAB\\MATLAB\\AlphaShape\\fitAlphaSurface.m,C:\\Users\\steph\\Documents\\Java 
 * Projects\\MIA_MATLAB\\MATLAB\\AlphaShape\\fitAlphaSurfaceAuto.m}" 
 */

package MIA_MATLAB;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;
import java.util.*;

/**
 * The <code>AlphaShape</code> class provides a Java interface to the M-functions
 * from the files:
 * <pre>
 *  C:\\Users\\steph\\Documents\\Java Projects\\MIA_MATLAB\\MATLAB\\AlphaShape\\fitAlphaSurface.m
 *  C:\\Users\\steph\\Documents\\Java Projects\\MIA_MATLAB\\MATLAB\\AlphaShape\\fitAlphaSurfaceAuto.m
 * </pre>
 * The {@link #dispose} method <b>must</b> be called on a <code>AlphaShape</code> 
 * instance when it is no longer needed to ensure that native resources allocated by this 
 * class are properly freed.
 * @version 0.0
 */
public class AlphaShape extends MWComponentInstance<AlphaShape>
{
    /**
     * Tracks all instances of this class to ensure their dispose method is
     * called on shutdown.
     */
    private static final Set<Disposable> sInstances = new HashSet<Disposable>();

    /**
     * Maintains information used in calling the <code>fitAlphaSurface</code> M-function.
     */
    private static final MWFunctionSignature sFitAlphaSurfaceSignature =
        new MWFunctionSignature(/* max outputs = */ 2,
                                /* has varargout = */ false,
                                /* function name = */ "fitAlphaSurface",
                                /* max inputs = */ 4,
                                /* has varargin = */ false);
    /**
     * Maintains information used in calling the <code>fitAlphaSurfaceAuto</code> 
     *M-function.
     */
    private static final MWFunctionSignature sFitAlphaSurfaceAutoSignature =
        new MWFunctionSignature(/* max outputs = */ 2,
                                /* has varargout = */ false,
                                /* function name = */ "fitAlphaSurfaceAuto",
                                /* max inputs = */ 3,
                                /* has varargin = */ false);

    /**
     * Shared initialization implementation - private
     */
    private AlphaShape (final MWMCR mcr) throws MWException
    {
        super(mcr);
        // add this to sInstances
        synchronized(AlphaShape.class) {
            sInstances.add(this);
        }
    }

    /**
     * Constructs a new instance of the <code>AlphaShape</code> class.
     */
    public AlphaShape() throws MWException
    {
        this(MIA_MATLABMCRFactory.newInstance());
    }
    
    private static MWComponentOptions getPathToComponentOptions(String path)
    {
        MWComponentOptions options = new MWComponentOptions(new MWCtfExtractLocation(path),
                                                            new MWCtfDirectorySource(path));
        return options;
    }
    
    /**
     * @deprecated Please use the constructor {@link #AlphaShape(MWComponentOptions componentOptions)}.
     * The <code>com.mathworks.toolbox.javabuilder.MWComponentOptions</code> class provides API to set the
     * path to the component.
     * @param pathToComponent Path to component directory.
     */
    public AlphaShape(String pathToComponent) throws MWException
    {
        this(MIA_MATLABMCRFactory.newInstance(getPathToComponentOptions(pathToComponent)));
    }
    
    /**
     * Constructs a new instance of the <code>AlphaShape</code> class. Use this 
     * constructor to specify the options required to instantiate this component.  The 
     * options will be specific to the instance of this component being created.
     * @param componentOptions Options specific to the component.
     */
    public AlphaShape(MWComponentOptions componentOptions) throws MWException
    {
        this(MIA_MATLABMCRFactory.newInstance(componentOptions));
    }
    
    /** Frees native resources associated with this object */
    public void dispose()
    {
        try {
            super.dispose();
        } finally {
            synchronized(AlphaShape.class) {
                sInstances.remove(this);
            }
        }
    }
  
    /**
     * Invokes the first m-function specified by MCC, with any arguments given on
     * the command line, and prints the result.
     */
    public static void main (String[] args)
    {
        try {
            MWMCR mcr = MIA_MATLABMCRFactory.newInstance();
            mcr.runMain( sFitAlphaSurfaceSignature, args);
            mcr.dispose();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    /**
     * Calls dispose method for each outstanding instance of this class.
     */
    public static void disposeAllInstances()
    {
        synchronized(AlphaShape.class) {
            for (Disposable i : sInstances) i.dispose();
            sInstances.clear();
        }
    }

    /**
     * Provides the interface for calling the <code>fitAlphaSurface</code> M-function 
     * where the first input, an instance of List, receives the output of the M-function and
     * the second input, also an instance of List, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % pts must be double precision
     * </pre>
     * </p>
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
    public void fitAlphaSurface(List lhs, List rhs) throws MWException
    {
        fMCR.invoke(lhs, rhs, sFitAlphaSurfaceSignature);
    }

    /**
     * Provides the interface for calling the <code>fitAlphaSurface</code> M-function 
     * where the first input, an Object array, receives the output of the M-function and
     * the second input, also an Object array, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % pts must be double precision
     * </pre>
     * </p>
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
    public void fitAlphaSurface(Object[] lhs, Object[] rhs) throws MWException
    {
        fMCR.invoke(Arrays.asList(lhs), Arrays.asList(rhs), sFitAlphaSurfaceSignature);
    }

    /**
     * Provides the standard interface for calling the <code>fitAlphaSurface</code>
     * M-function with 4 input arguments.
     * Input arguments may be passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     *
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % pts must be double precision
     * </pre>
     * </p>
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     * @return Array of length nargout containing the function outputs. Outputs
     * are returned as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>. Each output array
     * should be freed by calling its <code>dispose()</code> method.
     * @throws MWException An error has occurred during the function call.
     */
    public Object[] fitAlphaSurface(int nargout, Object... rhs) throws MWException
    {
        Object[] lhs = new Object[nargout];
        fMCR.invoke(Arrays.asList(lhs), 
                    MWMCR.getRhsCompat(rhs, sFitAlphaSurfaceSignature), 
                    sFitAlphaSurfaceSignature);
        return lhs;
    }
    /**
     * Provides the interface for calling the <code>fitAlphaSurfaceAuto</code> M-function 
     * where the first input, an instance of List, receives the output of the M-function and
     * the second input, also an instance of List, provides the input to the M-function.
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
    public void fitAlphaSurfaceAuto(List lhs, List rhs) throws MWException
    {
        fMCR.invoke(lhs, rhs, sFitAlphaSurfaceAutoSignature);
    }

    /**
     * Provides the interface for calling the <code>fitAlphaSurfaceAuto</code> M-function 
     * where the first input, an Object array, receives the output of the M-function and
     * the second input, also an Object array, provides the input to the M-function.
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
    public void fitAlphaSurfaceAuto(Object[] lhs, Object[] rhs) throws MWException
    {
        fMCR.invoke(Arrays.asList(lhs), Arrays.asList(rhs), sFitAlphaSurfaceAutoSignature);
    }

    /**
     * Provides the standard interface for calling the <code>fitAlphaSurfaceAuto</code>
     * M-function with 3 input arguments.
     * Input arguments may be passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     *
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     * @return Array of length nargout containing the function outputs. Outputs
     * are returned as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>. Each output array
     * should be freed by calling its <code>dispose()</code> method.
     * @throws MWException An error has occurred during the function call.
     */
    public Object[] fitAlphaSurfaceAuto(int nargout, Object... rhs) throws MWException
    {
        Object[] lhs = new Object[nargout];
        fMCR.invoke(Arrays.asList(lhs), 
                    MWMCR.getRhsCompat(rhs, sFitAlphaSurfaceAutoSignature), 
                    sFitAlphaSurfaceAutoSignature);
        return lhs;
    }
}
