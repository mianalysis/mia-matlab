package wbif.sjx.MIA_MATLAB;

import MIA_MATLAB.AlphaShape;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import ij.ImagePlus;
import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Process.ColourFactory;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Object.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class FitConvexHull extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String TEMPLATE_IMAGE = "Template image";
    public static final String ALPHA_RADIUS_MODE = "Alpha radius mode";
    public static final String ALPHA_RADIUS = "Alpha radius";

    public interface AlphaRadiusModes {
        String AUTOMATIC = "Automatic";
        String MANUAL = "Manual";

        String[] ALL = new String[]{AUTOMATIC,MANUAL};

    }

    public static void main(String[] args) throws Exception {
        MIA.addPluginPackageName(FitConvexHull.class.getCanonicalName());
        MIA.main(new String[]{});

    }

    public static boolean testFittingValidity(Obj inputObject) {
        double[][] extents = inputObject.getExtents(true,false);
        return !(extents[0][0] == extents[0][1] || extents[1][0] == extents[1][1] || extents[2][0] == extents[2][1]);

    }

    public static MWNumericArray convertPointsToMW(TreeSet<Point<Integer>> points) {
        double[][] pointArray = new double[points.size()][3];

        int i = 0;
        for (Point<Integer> point:points) {
            pointArray[i][0] = point.getX();
            pointArray[i][1] = point.getY();
            pointArray[i++][2] = point.getZ();
        }

        return new MWNumericArray(pointArray,MWClassID.DOUBLE);

    }

    public static TreeSet<Point<Integer>> convertMWToPoints(MWNumericArray array) {
        TreeSet<Point<Integer>> points = new TreeSet<>();

        int nPoints = array.getDimensions()[0];
        Indexer indexer = new Indexer(nPoints,3);
        for (int i=0;i<nPoints;i++) {
            int[] data = array.getIntData();
            int x = data[indexer.getIndex(new int[]{i,0})];
            int y = data[indexer.getIndex(new int[]{i,1})];
            int z = data[indexer.getIndex(new int[]{i,2})];

            points.add(new Point<>(x,y,z));

        }

        return points;

    }

    public static Obj getConvexHull(Obj inputObject, Image templateImage, String outputObjectsName, ObjCollection outputObjects, double alphaRadius) {
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        String calibratedUnits = inputObject.getCalibratedUnits();
        boolean is2D = inputObject.is2D();

        try {
            // Getting points in MathWorks format
            MWNumericArray points = convertPointsToMW(inputObject.getSurface());

            // Calculating the convex hull
            Object[] output = null;
            if (alphaRadius == -1) {
                output = new AlphaShape().fitAlphaSurfaceAuto(1, points);
            } else {
                output = new AlphaShape().fitAlphaSurface(1, points, alphaRadius);
            }

            // Converting the output into a series of Point<Integer> objects
            Obj convexHullObject = new Obj(outputObjectsName,outputObjects.getNextID(),dppXY,dppZ,calibratedUnits,is2D);
            TreeSet<Point<Integer>> convexHullPoints = convertMWToPoints((MWNumericArray) output[0]);
            convexHullObject.setPoints(convexHullPoints);

            return convexHullObject;

        } catch (MWException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getTitle() {
        return "Fit convex hull";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
        Image templateImage = workspace.getImage(templateImageName);
        String alphaRadiusMode = parameters.getValue(ALPHA_RADIUS_MODE);
        double alphaRadius = parameters.getValue(ALPHA_RADIUS);

        switch (alphaRadiusMode) {
            case AlphaRadiusModes.AUTOMATIC:
                alphaRadius = -1;
                break;
        }

        // Creating a collection for the convex hull objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Processing each object
        int count = 0;
        int nTotal = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            writeMessage("Processing object "+(++count)+" of "+nTotal);
            // Testing the object can be fit
            if (!testFittingValidity(inputObject)) continue;

            Obj convexHullObj = getConvexHull(inputObject,templateImage,outputObjectsName,outputObjects,alphaRadius);
            if (convexHullObj == null) continue;

            outputObjects.add(convexHullObj);

        }

        // Adding convex hull objects to Workspace
        workspace.addObjects(outputObjects);

        if (showOutput) {
            HashMap<Integer,Float> hues = ColourFactory.getRandomHues(outputObjects);
            String mode = ConvertObjectsToImage.ColourModes.RANDOM_COLOUR;
            ImagePlus dispIpl = outputObjects.convertObjectsToImage("Objects",null,hues,8,false).getImagePlus();
            dispIpl.setLut(LUTs.Random(true));
            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));
        parameters.add(new InputImageP(TEMPLATE_IMAGE,this));
        parameters.add(new ChoiceP(ALPHA_RADIUS_MODE,this,AlphaRadiusModes.AUTOMATIC,AlphaRadiusModes.ALL));
        parameters.add(new DoubleP(ALPHA_RADIUS,this,1));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(TEMPLATE_IMAGE));

        returnedParameters.add(parameters.getParameter(ALPHA_RADIUS_MODE));
        switch ((String) parameters.getValue(ALPHA_RADIUS_MODE)) {
            case AlphaRadiusModes.MANUAL:
                returnedParameters.add(parameters.getParameter(ALPHA_RADIUS));
                break;
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationshipCollection) {

    }
}
