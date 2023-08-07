package io.github.mianalysis.mia.matlab.module;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import MIA_MATLAB_Core.StackMatcher;
import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.images.transform.FocusStackGlobal;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImageJ;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ZDriftCorrection extends CoreMATLABModule {
    private static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";

    private static final String DRIFT_CORRECTION_SEPARATOR = "Reference controls";
    public static final String CALCULATION_SOURCE = "Calculation source";
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CALCULATION_CHANNEL = "Calculation channel";
    public static final String REFERENCE_SOURCE = "Reference source";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String SMOOTH_TIMESERIES = "Smooth timeseries";
    public static final String SMOOTHING_RANGE = "Smoothing range (odd numbers)";

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[] { INTERNAL, EXTERNAL };

    }

    public interface ReferenceSources {
        String FIXED_IMAGE = "Fixed image";
        String PREVIOUS_IMAGE = "Previous image (specify first)";

        String[] ALL = new String[] { FIXED_IMAGE, PREVIOUS_IMAGE };

    }

    public static void main(String[] args) {
        // Creating a new instance of ImageJ
        new ij.ImageJ();

        // Launching MIA
        new ImageJ().command().run("io.github.mianalysis.mia.MIA", false);

        // Adding the current module to MIA's list of available modules.
        AvailableModules.addModuleName(ZDriftCorrection.class);

    }

    public ZDriftCorrection(Modules modules) {
        super("Z-drift correction", modules);
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE, workspace);
        String externalImageName = parameters.getValue(EXTERNAL_SOURCE, workspace);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL, workspace);
        String referenceSource = parameters.getValue(REFERENCE_SOURCE, workspace);
        String refImageName = parameters.getValue(REFERENCE_IMAGE, workspace);
        boolean smoothTimeseries = parameters.getValue(SMOOTH_TIMESERIES, workspace);
        int smoothingRange = parameters.getValue(SMOOTHING_RANGE, workspace);

        // Getting input images
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();
        Image externalImage = calculationSource.equals(CalculationSources.EXTERNAL)
                ? workspace.getImage(externalImageName)
                : null;
        Image refImage = workspace.getImage(refImageName);
        MWNumericArray refArray = imageStackToMW(refImage.getImagePlus().getStack());

        StackMatcher stackMatcher;
        try {
            stackMatcher = new StackMatcher();
        } catch (MWException e) {
            MIA.log.writeError(e);
            return Status.FAIL;
        }

        int[] bestSlices = new int[inputIpl.getNFrames()];
        Image substack = null;
        for (int t = 1; t <= inputIpl.getNFrames(); t++) {
            // If necessary, getting best match slice from previous run
            switch (referenceSource) {
                case ReferenceSources.PREVIOUS_IMAGE:
                    if (t > 1) {
                        refImage = ExtractSubstack.extractSubstack(substack, "Reference",
                                "1", String.valueOf(bestSlices[t - 2] + 1),
                                "1");
                        refArray = imageStackToMW(refImage.getImagePlus().getStack());
                    }
                    break;
            }

            // Getting current timepoint image stack
            switch (calculationSource) {
                case CalculationSources.EXTERNAL:
                    substack = ExtractSubstack.extractSubstack(externalImage, "Substack",
                            String.valueOf(calculationChannel), "1-end", String.valueOf(t));
                    break;

                case CalculationSources.INTERNAL:
                default:
                    substack = ExtractSubstack.extractSubstack(inputImage, "Substack",
                            String.valueOf(calculationChannel), "1-end", String.valueOf(t));
                    break;
            }
            MWNumericArray substackArray = imageStackToMW(substack.getImagePlus().getStack());

            try {
                Object[] output = stackMatcher.matchImageInStack(1, substackArray, refArray);
                bestSlices[t - 1] = ((MWNumericArray) output[0]).getInt();
                System.out.println(bestSlices[t - 1]);
            } catch (MWException e) {
                MIA.log.writeError(e);
                return Status.FAIL;
            }

            substackArray.dispose();

        }

        // Applying temporal smoothing of best focus slice index
        if (smoothTimeseries)
            bestSlices = FocusStackGlobal.rollingMedianFilter(bestSlices, smoothingRange);

        Image outputImage = FocusStackGlobal.extract(inputImage, 0, 0, bestSlices, outputImageName);

        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(DRIFT_CORRECTION_SEPARATOR, this));
        parameters.add(new ChoiceP(CALCULATION_SOURCE, this, CalculationSources.INTERNAL, CalculationSources.ALL));
        parameters.add(new InputImageP(EXTERNAL_SOURCE, this));
        parameters.add(new IntegerP(CALCULATION_CHANNEL, this, 1));
        parameters.add(new ChoiceP(REFERENCE_SOURCE, this, ReferenceSources.FIXED_IMAGE, ReferenceSources.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new BooleanP(SMOOTH_TIMESERIES, this, false));
        parameters.add(new IntegerP(SMOOTHING_RANGE, this, 5));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(DRIFT_CORRECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_SOURCE));
        switch ((String) parameters.getValue(CALCULATION_SOURCE, null)) {
            case CalculationSources.EXTERNAL:
                returnedParameters.add(parameters.getParameter(EXTERNAL_SOURCE));
                break;
        }
        returnedParameters.add(parameters.getParameter(CALCULATION_CHANNEL));
        returnedParameters.add(parameters.getParameter(REFERENCE_SOURCE));
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));

        returnedParameters.add(parameters.getParameter(SMOOTH_TIMESERIES));
        if ((boolean) parameters.getValue(SMOOTH_TIMESERIES, null)) {
            returnedParameters.add(parameters.getParameter(SMOOTHING_RANGE));
        }

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
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;

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
