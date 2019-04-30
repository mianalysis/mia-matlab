package wbif.sjx.MIA_MATLAB;

import MIA_MATLAB_Core.AlphaShape;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import ij.ImagePlus;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Object.Point;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

public class FitAlphaSurface extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String TEMPLATE_IMAGE = "Template image";
    public static final String ALPHA_RADIUS_MODE = "Alpha radius mode";
    public static final String ALPHA_RADIUS = "Alpha radius";
    public static final String MEASUREMENT_MODE = "Measurement mode";


    public interface AlphaRadiusModes {
        String AUTOMATIC = "Automatic";
        String MANUAL = "Manual";

        String[] ALL = new String[]{AUTOMATIC,MANUAL};

    }

    public interface MeasurementModes {
        String NONE = "None";
        String TWOD = "2D measurements";
        String THREED = "3D measurements";

        String[] ALL = new String[]{NONE,TWOD,THREED};

    }

    public interface Measurements {
        String ALPHA_RADIUS = "ALPHA_SHAPE // ALPHA_RADIUS (PX)";
        String AREA_PX = "ALPHA_SHAPE // AREA (PX^2)";
        String AREA_CAL = "ALPHA_SHAPE // AREA (${CAL}^2)";
        String PERIMETER_PX = "ALPHA_SHAPE // PERIMETER (PX)";
        String PERIMETER_CAL = "ALPHA_SHAPE // PERIMETER (${CAL})";
        String VOLUME_PX = "ALPHA_SHAPE // VOLUME (PX^3)";
        String VOLUME_CAL = "ALPHA_SHAPE // VOLUME (${CAL}^3)";
        String SURFACE_AREA_PX = "ALPHA_SHAPE // SURFACE_AREA (PX^2)";
        String SURFACE_AREA_CAL = "ALPHA_SHAPE // SURFACE_AREA (${CAL}^2)";

    }


    static boolean testFittingValidity(Obj inputObject) {
        double[][] extents = inputObject.getExtents(true,false);
        return !(extents[0][0] == extents[0][1] || extents[1][0] == extents[1][1] || extents[2][0] == extents[2][1]);

    }

    static MWNumericArray coordsToMW(TreeSet<Point<Integer>> points) {
        double[][] pointArray = new double[points.size()][3];

        int i=0;
        for (Point<Integer> point:points) {
            pointArray[i][0] = point.getX();
            pointArray[i][1] = point.getY();
            pointArray[i++][2] = point.getZ();
        }

        return new MWNumericArray(pointArray,MWClassID.DOUBLE);

    }

    static TreeSet<Point<Integer>> convertMWToPoints(MWNumericArray array) {
        TreeSet<Point<Integer>> points = new TreeSet<>();

        int nPoints = array.getDimensions()[0];
        Indexer indexer = new Indexer(nPoints,3);
        int[] data = array.getIntData();
        for (int i=0;i<nPoints;i++) {
            int x = data[indexer.getIndex(new int[]{i,0})];
            int y = data[indexer.getIndex(new int[]{i,1})];
            int z = data[indexer.getIndex(new int[]{i,2})];

            // We can't have duplicate coordinates in the TreeSet, so this should automatically down-sample Z
            points.add(new Point<>(x,y,z));

        }

        return points;

    }

    static Object[] getAlphaSurface(Obj inputObject, double alphaRadius) {
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        double xyzConversion = dppZ/dppXY;

        try {
            // Getting points in MathWorks format
            TreeSet<Point<Integer>> surfacePoints = inputObject.getSurface();
            MWNumericArray points = coordsToMW(surfacePoints);

            // Calculating the alpha shape
            if (alphaRadius == -1) {
                return new AlphaShape().fitAlphaSurfaceAuto(2, points, xyzConversion, false);
            } else {
                return new AlphaShape().fitAlphaSurface(2, points, alphaRadius, xyzConversion, false);
            }
        } catch (MWException e) {
            e.printStackTrace();
            return null;
        }
    }

    static Obj createAlphaSurfaceObject(ObjCollection outputObjects, Obj inputObject, MWNumericArray points, Image templateImage) {
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        String calibratedUnits = inputObject.getCalibratedUnits();
        boolean is2D = inputObject.is2D();
        double xyzConversion = dppZ/dppXY;

        // Converting the output into a series of Point<Integer> objects
        Obj alphaShapeObject = new Obj(outputObjects.getName(),outputObjects.getAndIncrementID(),dppXY,dppZ,calibratedUnits,is2D);
        alphaShapeObject.setT(inputObject.getT());
        TreeSet<Point<Integer>> alphaShapePoints = convertMWToPoints(points);
        alphaShapeObject.setPoints(alphaShapePoints);

        // Removing any pixels outside the image area
        alphaShapeObject.cropToImageSize(templateImage);

        // Assigning relationship
        inputObject.addChild(alphaShapeObject);
        alphaShapeObject.addParent(inputObject);

        return alphaShapeObject;

    }

    static void addCommonMeasurements(Obj inputObject, MWStructArray results) {
        int idx = results.fieldIndex("alpha");
        double alpha = ((double[][]) results.get(idx+1))[0][0];
        inputObject.addMeasurement(new Measurement(Measurements.ALPHA_RADIUS,alpha));

    }

    static void add2DMeasurements(Obj inputObject, MWStructArray results) {
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();

        int idx = results.fieldIndex("area");
        double area = ((double[][]) results.get(idx+1))[0][0];
        inputObject.addMeasurement(new Measurement(Measurements.AREA_PX,area));
        if (Double.isNaN(area)) {
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.AREA_CAL), Double.NaN));
        } else {
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.AREA_CAL), area*dppXY*dppXY));
        }

        idx = results.fieldIndex("perimeter");
        double perimeter = ((double[][]) results.get(idx+1))[0][0];
        inputObject.addMeasurement(new Measurement(Measurements.PERIMETER_PX,perimeter));
        if (Double.isNaN(perimeter)) {
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.PERIMETER_CAL), Double.NaN));
        } else {
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.PERIMETER_CAL), perimeter*dppXY));
        }
    }

    static void add3DMeasurements(Obj inputObject, MWStructArray results) {
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();

        int idx = results.fieldIndex("volume");
        double volume = ((double[][]) results.get(idx+1))[0][0];
        inputObject.addMeasurement(new Measurement(Measurements.VOLUME_PX,volume));
        if (Double.isNaN(volume)) {
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.VOLUME_CAL), Double.NaN));
        } else {
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.VOLUME_CAL), volume*dppXY*dppXY*dppXY));
        }

        idx = results.fieldIndex("surfaceArea");
        double surfaceArea = ((double[][]) results.get(idx+1))[0][0];
        inputObject.addMeasurement(new Measurement(Measurements.SURFACE_AREA_PX,surfaceArea));
        if (Double.isNaN(surfaceArea)) {
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.SURFACE_AREA_CAL), Double.NaN));
        } else {
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.SURFACE_AREA_CAL), surfaceArea*dppXY*dppXY));
        }
    }


    @Override
    public String getTitle() {
        return "Fit alpha shape";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
        Image templateImage = workspace.getImage(templateImageName);
        String alphaRadiusMode = parameters.getValue(ALPHA_RADIUS_MODE);
        double alphaRadius = parameters.getValue(ALPHA_RADIUS);
        String measurementMode = parameters.getValue(MEASUREMENT_MODE);

        switch (alphaRadiusMode) {
            case AlphaRadiusModes.AUTOMATIC:
                alphaRadius = -1;
                break;
        }

        // Creating a collection for the alpha shape objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Processing each object
        int count = 0;
        int nTotal = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            writeMessage("Processing object "+(++count)+" of "+nTotal);
            // Testing the object can be fit
            if (!testFittingValidity(inputObject)) continue;

            // Getting surface
            TreeSet<Point<Integer>> surfacePoints = inputObject.getSurface();
            Obj surface = new Obj("Surface",inputObject.getID(),inputObject.getDistPerPxXY(),inputObject.getDistPerPxZ(),inputObject.getCalibratedUnits(),inputObject.is2D());
            surface.setPoints(surfacePoints);

            // Getting results of alpha shape fitting
            Object[] output = getAlphaSurface(surface,alphaRadius);
            if (output==null || output[0] == null) continue;

            MWStructArray results = (MWStructArray) output[1];

            // If the fitting fails, only the alpha value is returned
            if (results.fieldNames().length == 1) continue;

            // Creating object
            writeMessage("Creating alpha surface object");
            Obj alphaShapeObject = createAlphaSurfaceObject(outputObjects,inputObject,(MWNumericArray) output[0],templateImage);

            // Assigning measurements
            addCommonMeasurements(inputObject,(MWStructArray) output[1]);
            switch (measurementMode) {
                case MeasurementModes.TWOD:
                    add2DMeasurements(inputObject,(MWStructArray) output[1]);
                    break;
                case MeasurementModes.THREED:
                    add3DMeasurements(inputObject,(MWStructArray) output[1]);
                    break;
            }
            outputObjects.add(alphaShapeObject);

        }

        // Adding alpha shape objects to Workspace
        workspace.addObjects(outputObjects);

        if (showOutput) {
            HashMap<Integer,Float> hues = ColourFactory.getRandomHues(outputObjects);
            ImagePlus dispIpl = outputObjects.convertObjectsToImage("Objects",null,hues,8,false).getImagePlus();
            dispIpl.setLut(LUTs.Random(true));
            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();
        }

        if (showOutput) inputObjects.showMeasurements(this);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));
        parameters.add(new InputImageP(TEMPLATE_IMAGE,this));
        parameters.add(new ChoiceP(ALPHA_RADIUS_MODE,this,AlphaRadiusModes.AUTOMATIC,AlphaRadiusModes.ALL));
        parameters.add(new DoubleP(ALPHA_RADIUS,this,1));
        parameters.add(new ChoiceP(MEASUREMENT_MODE,this,MeasurementModes.NONE,MeasurementModes.ALL));

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

        returnedParameters.add(parameters.getParameter(MEASUREMENT_MODE));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        objectMeasurementRefs.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String measurementMode = parameters.getValue(MEASUREMENT_MODE);

        MeasurementRef measurementRef = new MeasurementRef(Measurements.ALPHA_RADIUS);
        measurementRef.setImageObjName(inputObjectsName);
        measurementRef.setCalculated(true);
        objectMeasurementRefs.add(measurementRef);

        switch (measurementMode) {
            case MeasurementModes.TWOD:
                measurementRef = new MeasurementRef(Measurements.AREA_PX);
                measurementRef.setImageObjName(inputObjectsName);
                measurementRef.setCalculated(true);
                objectMeasurementRefs.add(measurementRef);

                measurementRef = new MeasurementRef(Units.replace(Measurements.AREA_CAL));
                measurementRef.setImageObjName(inputObjectsName);
                measurementRef.setCalculated(true);
                objectMeasurementRefs.add(measurementRef);

                measurementRef = new MeasurementRef(Measurements.PERIMETER_PX);
                measurementRef.setImageObjName(inputObjectsName);
                measurementRef.setCalculated(true);
                objectMeasurementRefs.add(measurementRef);

                measurementRef = new MeasurementRef(Units.replace(Measurements.PERIMETER_CAL));
                measurementRef.setImageObjName(inputObjectsName);
                measurementRef.setCalculated(true);
                objectMeasurementRefs.add(measurementRef);
                break;

            case MeasurementModes.THREED:
                measurementRef = new MeasurementRef(Measurements.VOLUME_PX);
                measurementRef.setImageObjName(inputObjectsName);
                measurementRef.setCalculated(true);
                objectMeasurementRefs.add(measurementRef);

                measurementRef = new MeasurementRef(Units.replace(Measurements.VOLUME_CAL));
                measurementRef.setImageObjName(inputObjectsName);
                measurementRef.setCalculated(true);
                objectMeasurementRefs.add(measurementRef);

                measurementRef = new MeasurementRef(Measurements.SURFACE_AREA_PX);
                measurementRef.setImageObjName(inputObjectsName);
                measurementRef.setCalculated(true);
                objectMeasurementRefs.add(measurementRef);

                measurementRef = new MeasurementRef(Units.replace(Measurements.SURFACE_AREA_CAL));
                measurementRef.setImageObjName(inputObjectsName);
                measurementRef.setCalculated(true);
                objectMeasurementRefs.add(measurementRef);
                break;
        }

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        RelationshipCollection relationships = new RelationshipCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String alphaShapeObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        relationships.addRelationship(inputObjectsName,alphaShapeObjectsName);

        return relationships;

    }
}
