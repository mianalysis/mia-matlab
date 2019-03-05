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

import com.mathworks.toolbox.javabuilder.pooling.Poolable;
import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The <code>AlphaShapeRemote</code> class provides a Java RMI-compliant interface to the 
 * M-functions from the files:
 * <pre>
 *  C:\\Users\\steph\\Documents\\Java Projects\\MIA_MATLAB\\MATLAB\\AlphaShape\\fitAlphaSurface.m
 *  C:\\Users\\steph\\Documents\\Java Projects\\MIA_MATLAB\\MATLAB\\AlphaShape\\fitAlphaSurfaceAuto.m
 * </pre>
 * The {@link #dispose} method <b>must</b> be called on a <code>AlphaShapeRemote</code> 
 * instance when it is no longer needed to ensure that native resources allocated by this 
 * class are properly freed, and the server-side proxy is unexported.  (Failure to call 
 * dispose may result in server-side threads not being properly shut down, which often 
 * appears as a hang.)  
 *
 * This interface is designed to be used together with 
 * <code>com.mathworks.toolbox.javabuilder.remoting.RemoteProxy</code> to automatically 
 * generate RMI server proxy objects for instances of MIA_MATLAB.AlphaShape.
 */
public interface AlphaShapeRemote extends Poolable
{
    /**
     * Provides the standard interface for calling the <code>fitAlphaSurface</code> 
     * M-function with 4 input arguments.  
     *
     * Input arguments to standard interface methods may be passed as sub-classes of 
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of any 
     * supported Java type (i.e. scalars and multidimensional arrays of any numeric, 
     * boolean, or character type, or String). Arguments passed as Java types are 
     * converted to MATLAB arrays according to default conversion rules.
     *
     * All inputs to this method must implement either Serializable (pass-by-value) or 
     * Remote (pass-by-reference) as per the RMI specification.
     *
     * M-documentation as provided by the author of the M function:
     * <pre>
     * % pts must be double precision
     * </pre>
     *
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     *
     * @return Array of length nargout containing the function outputs. Outputs are 
     * returned as sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>. 
     * Each output array should be freed by calling its <code>dispose()</code> method.
     *
     * @throws java.jmi.RemoteException An error has occurred during the function call or 
     * in communication with the server.
     */
    public Object[] fitAlphaSurface(int nargout, Object... rhs) throws RemoteException;
    /**
     * Provides the standard interface for calling the <code>fitAlphaSurfaceAuto</code> 
     * M-function with 3 input arguments.  
     *
     * Input arguments to standard interface methods may be passed as sub-classes of 
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of any 
     * supported Java type (i.e. scalars and multidimensional arrays of any numeric, 
     * boolean, or character type, or String). Arguments passed as Java types are 
     * converted to MATLAB arrays according to default conversion rules.
     *
     * All inputs to this method must implement either Serializable (pass-by-value) or 
     * Remote (pass-by-reference) as per the RMI specification.
     *
     * No usage documentation is available for this function.  (To fix this, the function 
     * author should insert a help comment at the beginning of their M code.  See the 
     * MATLAB documentation for more details.)
     *
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     *
     * @return Array of length nargout containing the function outputs. Outputs are 
     * returned as sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>. 
     * Each output array should be freed by calling its <code>dispose()</code> method.
     *
     * @throws java.jmi.RemoteException An error has occurred during the function call or 
     * in communication with the server.
     */
    public Object[] fitAlphaSurfaceAuto(int nargout, Object... rhs) throws RemoteException;
  
    /** Frees native resources associated with the remote server object */
    void dispose() throws RemoteException;
}
