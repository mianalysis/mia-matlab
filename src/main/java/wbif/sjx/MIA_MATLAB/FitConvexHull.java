package wbif.sjx.MIA_MATLAB;

import ConvexHull.ConvexHull;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.MIA;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Process.ColourFactory;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Object.Point;

import java.util.HashMap;
import java.util.TreeSet;

public class FitConvexHull extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String TEMPLATE_IMAGE = "Template image";


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

    public static Obj getConvexHull(Obj inputObject, Image templateImage, String outputObjectsName, ObjCollection outputObjects) {
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        String calibratedUnits = inputObject.getCalibratedUnits();
        boolean is2D = inputObject.is2D();

        try {
            // Getting points in MathWorks format
            MWNumericArray points = convertPointsToMW(inputObject.getSurface());

            // Calculating the convex hull
            ConvexHull convexHull = new ConvexHull();
            Object[] output = convexHull.getContainedPoints(1,points);

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

        // Creating a collection for the convex hull objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Processing each object
        int count = 0;
        int nTotal = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            writeMessage("Processing object "+(++count)+" of "+nTotal);
            // Testing the object can be fit
            if (!testFittingValidity(inputObject)) continue;

            Obj convexHullObj = getConvexHull(inputObject,templateImage,outputObjectsName,outputObjects);
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
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(TEMPLATE_IMAGE,Parameter.INPUT_IMAGE,null));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationshipCollection) {

    }
}
