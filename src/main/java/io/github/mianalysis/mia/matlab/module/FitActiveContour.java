package io.github.mianalysis.mia.matlab.module;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWLogicalArray;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import MIA_MATLAB_Core.ActiveContourFitter;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.PointOutOfRangeException;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ParentChildRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FitActiveContour extends CoreMATLABModule {
    /**
    * 
    */
    public static final String IMAGE_SEPARATOR = "Image input";

    /**
     * Image from the workspace to which the contours will be fit. The intensity of
     * this image will contribute to the external forces applied to the contour. For
     * example, the contour will attempt to minimise the intensity along the path of
     * the contour.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
    * 
    */
    public static final String OBJECTS_SEPARATOR = "Objects input/output";

    /**
     * Objects from the workspace to which active contours will be fit. Active
     * contours are fit in 2D to the object points from the first slice. As such,
     * input objects can be stored in 3D space, but only a single slice will be fit.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * This is the name with which the output contour objects will be stored in the
     * workspace.
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String ACTIVE_CONTOUR_SEPARATOR = "Active contour controls";

    public static final String NUM_ITERATIONS = "Number of iterations";

    public static final String METHOD = "Method";

    public static final String SMOOTHING_FACTOR = "Smoothing factor";

    public static final String CONTRACTION_BIAS = "Contraction bias";

    public interface Methods {
        String CHAN_VESE = "Chan-Vese";
        String EDGE = "Edge";

        String[] ALL = new String[]{CHAN_VESE, EDGE};
   
    }


    public static void main(String[] args) {
        try {
            LegacyInjector.preinit();
        } catch (Exception e) {
        }

        try {
            new ij.ImageJ();
            new ImageJ().command().run("io.github.mianalysis.mia.MIA_", false);
            AvailableModules.addModuleName(FitActiveContour.class);

        } catch (Exception e) {
            MIA.log.writeError(e);
        }

    }

    public FitActiveContour(Modules modules) {
        super("Fit active contours (MATLAB)", modules);
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
    }

    public static ImagePlus getSliceImage(Obj inputObject, int slice) {
        ImagePlus ipl = IJ.createHyperStack("Object slice", inputObject.getSpatialCalibration().width,
                inputObject.getSpatialCalibration().height, 1, 1, 1, 8);
        ImageProcessor ipr = ipl.getProcessor();

        for (Point<Integer> point : inputObject.getCoordinateSet()) {
            ipr.set(point.getX(), point.getY(), 255);
        }

        return ipl;

    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImage(inputImageName);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting output image name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        // Getting parameters
        int numIterations = parameters.getValue(NUM_ITERATIONS, workspace);
        String method = parameters.getValue(METHOD, workspace);
        double smoothingFactor = parameters.getValue(SMOOTHING_FACTOR, workspace);
        double contractionBias = parameters.getValue(CONTRACTION_BIAS, workspace);

        // If there are no input objects, creating an empty collection
        if (inputObjects.getFirst() == null) {
            workspace.addObjects(outputObjects);
            return Status.PASS;
        }

        ActiveContourFitter fitter;
        try {
            fitter = new ActiveContourFitter();
        } catch (MWException e) {
            MIA.log.writeError(e);
            return Status.FAIL;
        }

        // Iterating over all objects
        int count = 1;
        int total = inputObjects.size();

        for (Obj inputObject : inputObjects.values()) {
            // Creating output object
            Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.QUADTREE);
            outputObject.setT(inputObject.getT());
            inputObject.addChild(outputObject);
            outputObject.addParent(inputObject);

            // Iterating over each slice of the input object
            double[][] extents = inputObject.getExtents(true, false);
            int zMin = (int) Math.round(extents[2][0]);
            int zMax = (int) Math.round(extents[2][1]);

            for (int z = zMin; z <= zMax; z++) {
                ImagePlus objectIpl = getSliceImage(inputObject, z);
                ImagePlus imageIpl = ExtractSubstack.extractSubstack(inputImage, "Image slice", "1",
                        String.valueOf(z + 1), String.valueOf(inputObject.getT() + 1)).getImagePlus();

                MWLogicalArray objectMW = imageStackToLogicalMW(objectIpl.getStack());
                MWNumericArray imageMW = imageStackToMW(imageIpl.getStack());

                Object[] output;
                try {
                    output = fitter.fitActiveContour(1, imageMW, objectMW, numIterations, method, smoothingFactor, contractionBias);
                } catch (MWException e) {
                    MIA.log.writeError(e);
                    return Status.FAIL;
                }

                MWLogicalArray contourMW = (MWLogicalArray) output[0];

                for (int row = 1; row <= contourMW.getDimensions()[0]; row++) {
                    for (int col = 1; col <= contourMW.getDimensions()[1]; col++) {
                        if ((Boolean) contourMW.get(new int[]{row, col}))
                            try {
                                outputObject.add(row-1, col-1, z);
                            } catch (PointOutOfRangeException e) {
                            }
                                                    
                    }
                }

            }

            writeProgressStatus(count++, total, "objects");

        }

        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        workspace.addObjects(outputObjects);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(IMAGE_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OBJECTS_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(ACTIVE_CONTOUR_SEPARATOR, this));
        parameters.add(new IntegerP(NUM_ITERATIONS, this, 100));
        parameters.add(new ChoiceP(METHOD, this, Methods.CHAN_VESE, Methods.ALL));
        parameters.add(new DoubleP(SMOOTHING_FACTOR, this, 0));
        parameters.add(new DoubleP(CONTRACTION_BIAS, this, 0));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(IMAGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(OBJECTS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(ACTIVE_CONTOUR_SEPARATOR));
        returnedParameters.add(parameters.getParameter(NUM_ITERATIONS));
        returnedParameters.add(parameters.getParameter(METHOD));
        returnedParameters.add(parameters.getParameter(SMOOTHING_FACTOR));
        returnedParameters.add(parameters.getParameter(CONTRACTION_BIAS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;

        ParentChildRefs returnedRefs = new ParentChildRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        ParentChildRef ref = returnedRefs.getOrPut(inputObjectsName, outputObjectsName);
        returnedRefs.add(ref);

        return returnedRefs;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from the workspace to which the contours will be fit.  The intensity of this image will contribute to the external forces applied to the contour.  For example, the contour will attempt to minimise the intensity along the path of the contour.");

        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects from the workspace to which active contours will be fit.  Active contours are fit in 2D to the object points from the first slice.  As such, input objects can be stored in 3D space, but only a single slice will be fit.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "This is the name with which the output contour objects will be stored in the workspace.");

    }

}
