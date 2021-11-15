package io.github.mianalysis.mia_matlab.module;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import MIA_MATLAB_Core.AlphaShapeFitter;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ParentChildRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.sjcross.common.mathfunc.Indexer;
import io.github.sjcross.common.object.Point;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.Volume;
import io.github.sjcross.common.object.volume.VolumeType;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FitAlphaSurface extends CoreMATLABModule {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String ALPHA_SHAPE_SEPARATOR = "Alpha shape controls";
    public static final String ALPHA_RADIUS_MODE = "Alpha radius mode";
    public static final String ALPHA_RADIUS = "Alpha radius";
    public static final String MEASUREMENT_MODE = "Measurement mode";
    
    public FitAlphaSurface(Modules modules) {
        super("Fit alpha shape", modules);
    }

    @Override
    public String getDescription() {
        return "";
    }

    public interface AlphaRadiusModes {
        String AUTOMATIC = "Automatic";
        String MANUAL = "Manual";

        String[] ALL = new String[] { AUTOMATIC, MANUAL };

    }

    public interface MeasurementModes {
        String NONE = "None";
        String TWOD = "2D measurements";
        String THREED = "3D measurements";

        String[] ALL = new String[] { NONE, TWOD, THREED };

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
        double[][] extents = inputObject.getExtents(true, false);

        // Only testing XY extents as we can work with shapes in 2D
        return !(extents[0][0] == extents[0][1] || extents[1][0] == extents[1][1]);

    }

    static void addPoints(MWNumericArray array, Obj object) {
        int nPoints = array.getDimensions()[0];
        Indexer indexer = new Indexer(nPoints, 3);
        int[] data = array.getIntData();
        for (int i = 0; i < nPoints; i++) {
            int x = data[indexer.getIndex(new int[] { i, 0 })];
            int y = data[indexer.getIndex(new int[] { i, 1 })];
            int z = data[indexer.getIndex(new int[] { i, 2 })];

            // We can't have duplicate coordinates in the TreeSet, so this should
            // automatically down-sample Z
            try {
                object.add(new Point<>(x, y, z));
            } catch (PointOutOfRangeException e) {
            }
        }
    }

    static Object[] getAlphaSurface(Volume inputObject, double alphaRadius) {
        double xyzConversion = inputObject.getDppZ() / inputObject.getDppXY();

        try {
            // Getting points in MathWorks format
            MWNumericArray points = coordsToMW(inputObject);

            // Calculating the alpha shape
            AlphaShapeFitter alphaShape = new AlphaShapeFitter();
            if (alphaRadius == -1) {
                Object[] results = alphaShape.fitAlphaSurfaceAuto(2, points, xyzConversion, false);
                alphaShape.dispose();
                points.dispose();
                return results;
            } else {
                Object[] results = alphaShape.fitAlphaSurface(2, points, alphaRadius, xyzConversion, false);
                alphaShape.dispose();
                points.dispose();
                return results;
            }
        } catch (MWException e) {
            e.printStackTrace();
            return null;
        }
    }

    static Obj createAlphaSurfaceObject(Objs outputObjects, Obj inputObject, MWNumericArray points) {
        // Converting the output into a series of Point<Integer> objects
        Obj alphaShapeObject = new Obj(outputObjects, VolumeType.QUADTREE, outputObjects.getAndIncrementID());
        alphaShapeObject.setT(inputObject.getT());
        addPoints(points, alphaShapeObject);

        // Assigning relationship
        inputObject.addChild(alphaShapeObject);
        alphaShapeObject.addParent(inputObject);

        return alphaShapeObject;

    }

    static void addCommonMeasurements(Obj inputObject, MWStructArray results) {
        int idx = results.fieldIndex("alpha");
        double alpha = ((double[][]) results.get(idx + 1))[0][0];
        inputObject.addMeasurement(new Measurement(Measurements.ALPHA_RADIUS, alpha));

    }

    static void add2DMeasurements(Obj inputObject, MWStructArray results) {
        double dppXY = inputObject.getDppXY();

        int idx = results.fieldIndex("area");
        double area = ((double[][]) results.get(idx + 1))[0][0];
        inputObject.addMeasurement(new Measurement(Measurements.AREA_PX, area));
        if (Double.isNaN(area)) {
            inputObject.addMeasurement(new Measurement(SpatialUnit.replace(Measurements.AREA_CAL), Double.NaN));
        } else {
            inputObject.addMeasurement(new Measurement(SpatialUnit.replace(Measurements.AREA_CAL), area * dppXY * dppXY));
        }

        idx = results.fieldIndex("perimeter");
        double perimeter = ((double[][]) results.get(idx + 1))[0][0];
        inputObject.addMeasurement(new Measurement(Measurements.PERIMETER_PX, perimeter));
        if (Double.isNaN(perimeter)) {
            inputObject.addMeasurement(new Measurement(SpatialUnit.replace(Measurements.PERIMETER_CAL), Double.NaN));
        } else {
            inputObject.addMeasurement(new Measurement(SpatialUnit.replace(Measurements.PERIMETER_CAL), perimeter * dppXY));
        }
    }

    static void add3DMeasurements(Obj inputObject, MWStructArray results) {
        double dppXY = inputObject.getDppXY();

        int idx = results.fieldIndex("volume");
        double volume = ((double[][]) results.get(idx + 1))[0][0];
        inputObject.addMeasurement(new Measurement(Measurements.VOLUME_PX, volume));
        if (Double.isNaN(volume)) {
            inputObject.addMeasurement(new Measurement(SpatialUnit.replace(Measurements.VOLUME_CAL), Double.NaN));
        } else {
            inputObject.addMeasurement(
                    new Measurement(SpatialUnit.replace(Measurements.VOLUME_CAL), volume * dppXY * dppXY * dppXY));
        }

        idx = results.fieldIndex("surfaceArea");
        double surfaceArea = ((double[][]) results.get(idx + 1))[0][0];
        inputObject.addMeasurement(new Measurement(Measurements.SURFACE_AREA_PX, surfaceArea));
        if (Double.isNaN(surfaceArea)) {
            inputObject.addMeasurement(new Measurement(SpatialUnit.replace(Measurements.SURFACE_AREA_CAL), Double.NaN));
        } else {
            inputObject.addMeasurement(
                    new Measurement(SpatialUnit.replace(Measurements.SURFACE_AREA_CAL), surfaceArea * dppXY * dppXY));
        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String alphaRadiusMode = parameters.getValue(ALPHA_RADIUS_MODE);
        double alphaRadius = parameters.getValue(ALPHA_RADIUS);
        String measurementMode = parameters.getValue(MEASUREMENT_MODE);

        switch (alphaRadiusMode) {
            case AlphaRadiusModes.AUTOMATIC:
                alphaRadius = -1;
                break;
        }

        // Creating a collection for the alpha shape objects
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        // Processing each object
        int count = 0;
        int nTotal = inputObjects.size();
        for (Obj inputObject : inputObjects.values()) {
            writeStatus("Processing object " + (++count) + " of " + nTotal);
            // Testing the object can be fit
            if (!testFittingValidity(inputObject))
                continue;

            // Getting surface
            Volume surface = inputObject.getSurface();

            // Getting results of alpha shape fitting
            Object[] output = getAlphaSurface(surface, alphaRadius);
            if (output == null || output[0] == null)
                continue;

            MWStructArray results = (MWStructArray) output[1];

            // If the fitting fails, only the alpha value is returned
            if (results.fieldNames().length == 1)
                continue;

            // Creating object
            writeStatus("Creating alpha surface object");
            MWNumericArray points = (MWNumericArray) output[0];
            Obj alphaShapeObject = createAlphaSurfaceObject(outputObjects, inputObject, points);

            // Assigning measurements
            addCommonMeasurements(inputObject, (MWStructArray) output[1]);
            switch (measurementMode) {
                case MeasurementModes.TWOD:
                    add2DMeasurements(inputObject, (MWStructArray) output[1]);
                    break;
                case MeasurementModes.THREED:
                    add3DMeasurements(inputObject, (MWStructArray) output[1]);
                    break;
            }
            outputObjects.add(alphaShapeObject);

            points.dispose();
            results.dispose();
            output = null;

        }

        // Adding alpha shape objects to Workspace
        workspace.addObjects(outputObjects);

        if (showOutput) {
            Image dispImage = outputObjects.convertToImageRandomColours();
            dispImage.showImage();
        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new SeparatorP(ALPHA_SHAPE_SEPARATOR, this));
        parameters.add(new ChoiceP(ALPHA_RADIUS_MODE, this, AlphaRadiusModes.AUTOMATIC, AlphaRadiusModes.ALL));
        parameters.add(new DoubleP(ALPHA_RADIUS, this, 1));
        parameters.add(new ChoiceP(MEASUREMENT_MODE, this, MeasurementModes.NONE, MeasurementModes.ALL));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(ALPHA_SHAPE_SEPARATOR));
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
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String measurementMode = parameters.getValue(MEASUREMENT_MODE);

        ObjMeasurementRef measurementRef = objectMeasurementRefs.getOrPut(Measurements.ALPHA_RADIUS);
        measurementRef.setObjectsName(inputObjectsName);
        objectMeasurementRefs.add(measurementRef);

        switch (measurementMode) {
            case MeasurementModes.TWOD:
                measurementRef = objectMeasurementRefs.getOrPut(Measurements.AREA_PX);
                measurementRef.setObjectsName(inputObjectsName);
                returnedRefs.add(measurementRef);

                measurementRef = objectMeasurementRefs.getOrPut(SpatialUnit.replace(Measurements.AREA_CAL));
                measurementRef.setObjectsName(inputObjectsName);
                returnedRefs.add(measurementRef);

                measurementRef = objectMeasurementRefs.getOrPut(Measurements.PERIMETER_PX);
                measurementRef.setObjectsName(inputObjectsName);
                returnedRefs.add(measurementRef);

                measurementRef = objectMeasurementRefs.getOrPut(SpatialUnit.replace(Measurements.PERIMETER_CAL));
                measurementRef.setObjectsName(inputObjectsName);
                returnedRefs.add(measurementRef);
                break;

            case MeasurementModes.THREED:
                measurementRef = objectMeasurementRefs.getOrPut(Measurements.VOLUME_PX);
                measurementRef.setObjectsName(inputObjectsName);
                returnedRefs.add(measurementRef);

                measurementRef = objectMeasurementRefs.getOrPut(SpatialUnit.replace(Measurements.VOLUME_CAL));
                measurementRef.setObjectsName(inputObjectsName);
                returnedRefs.add(measurementRef);

                measurementRef = objectMeasurementRefs.getOrPut(Measurements.SURFACE_AREA_PX);
                measurementRef.setObjectsName(inputObjectsName);
                returnedRefs.add(measurementRef);

                measurementRef = objectMeasurementRefs.getOrPut(SpatialUnit.replace(Measurements.SURFACE_AREA_CAL));
                measurementRef.setObjectsName(inputObjectsName);
                returnedRefs.add(measurementRef);
                break;
        }

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        ParentChildRefs returnedRefs = new ParentChildRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String alphaShapeObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ParentChildRef ref = returnedRefs.getOrPut(inputObjectsName, alphaShapeObjectsName);
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
}
