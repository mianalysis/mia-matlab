package wbif.sjx.MIA_MATLAB;

import com.drew.lang.annotations.Nullable;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import MIA_MATLAB_Core.StackSorter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Deprecated.ChannelExtractor;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ExtractSubstack;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.MathFunc.Indexer;

/**
 * Created by sc13967 on 30/06/2017.
 */
public class SortStack extends CoreMATLABModule {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String SORT_SEPARATOR = "Sorting controls";
    public static final String SORT_AXIS = "Sort axis";
    public static final String CALCULATION_SOURCE = "Calculation source";
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CALCULATION_CHANNEL = "Calculation channel";

    public interface SortAxes {
        String TIME = "Time";
        String Z = "Z";

        String[] ALL = new String[] { TIME, Z };

    }

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[] { INTERNAL, EXTERNAL };

    }

    public SortStack(ModuleCollection modules) {
        super("Sort stack", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "";
    }

    boolean testReferenceValidity(Image referenceImage, String sortAxis, Image inputImage) {
        ImagePlus refIpl = referenceImage.getImagePlus();
        ImagePlus inputIpl = inputImage.getImagePlus();

        // Reference should have equal number of slices/frames as input in sorting
        // dimension and be single valued in other dimension
        switch (sortAxis) {
            case SortAxes.TIME:
                if (refIpl.getNSlices() > 1) {
                    MIA.log.writeWarning("Reference stack has too many slices (" + refIpl.getNSlices()
                            + ") when sorting along time axis.  Reference stack should only have 1 slice.");
                    return false;
                }

                if (refIpl.getNFrames() != inputIpl.getNFrames()) {
                    MIA.log.writeWarning(
                            "Reference stack has different number of frames to input image.  Reference has "
                                    + refIpl.getNFrames() + " frames, input has " + inputIpl.getNFrames() + " frames.");
                    return false;
                }

                break;

            case SortAxes.Z:
                if (refIpl.getNFrames() > 1) {
                    MIA.log.writeWarning("Reference stack has too many frames (" + refIpl.getNFrames()
                            + ") when sorting along Z axis.  Reference stack should only have 1 frame.");
                    return false;
                }

                if (refIpl.getNSlices() != inputIpl.getNSlices()) {
                    MIA.log.writeWarning(
                            "Reference stack has different number of slices to input image.  Reference has "
                                    + refIpl.getNSlices() + " slices, input has " + inputIpl.getNSlices() + " slices.");
                    return false;
                }

                break;
        }

        // Reference stack is valid
        return true;

    }

    MWNumericArray getReferenceArray(Image referenceImage, int calculationChannel) {
        Image referenceChannel = ExtractSubstack.extractSubstack(referenceImage, "Reference",
                String.valueOf(calculationChannel), "1-end", "1-end");

        return imageStackToMW(referenceChannel.getImagePlus().getImageStack());

    }

    int[] getStackOrder(MWNumericArray referenceArray) {
        try {
            // Getting optimised stack order
            StackSorter stackSorter = new StackSorter();
            Object[] output = stackSorter.getOptimisedOrder(1, referenceArray, false);
            stackSorter.dispose();
            referenceArray.dispose();

            // Extracting slice order
            MWNumericArray orderArray = (MWNumericArray) output[0];
            int nPoints = orderArray.getDimensions()[0];
            Indexer indexer = new Indexer(nPoints, 2);
            int[] data = orderArray.getIntData();
            int[] order = new int[nPoints];

            for (int i = 0; i < nPoints; i++) {
                order[i] = data[indexer.getIndex(new int[] { i, 0 })];
            }

            return order;

        } catch (MWException e) {
            e.printStackTrace();
            return null;
        }
    }

    void reorderStack(Image image, int[] order, String sortAxis) {
        switch (sortAxis) {
            case SortAxes.TIME:
                reorderStackTime(image, order);
                break;

            case SortAxes.Z:
                reorderStackZ(image, order);
                break;
        }
    }

    void reorderStackTime(Image image, int[] order) {
        // Iterating over all channels
        ImagePlus sourceIpl = image.getImagePlus().duplicate();
        ImagePlus targetIpl = image.getImagePlus();

        for (int c = 0; c < sourceIpl.getNChannels(); c++) {
            for (int z = 0; z < sourceIpl.getNSlices(); z++) {
                for (int t = 0; t < sourceIpl.getNFrames(); t++) {
                    if (sourceIpl.isHyperStack())
                        sourceIpl.setPosition(c + 1, z + 1, order[t]);
                    else
                        sourceIpl.setPosition(order[t]);

                    if (targetIpl.isHyperStack())
                        targetIpl.setPosition(c + 1, z + 1, t + 1);
                    else
                        targetIpl.setPosition(t + 1);

                    targetIpl.setProcessor(sourceIpl.getProcessor());

                }
            }
        }
    }

    void reorderStackZ(Image image, int[] order) {
        // Iterating over all channels
        ImagePlus sourceIpl = image.getImagePlus().duplicate();
        ImagePlus targetIpl = image.getImagePlus();

        for (int c = 0; c < sourceIpl.getNChannels(); c++) {
            for (int z = 0; z < sourceIpl.getNSlices(); z++) {
                for (int t = 0; t < sourceIpl.getNFrames(); t++) {
                    if (sourceIpl.isHyperStack())
                        sourceIpl.setPosition(c + 1, order[z], t + 1);
                    else
                        sourceIpl.setPosition(order[z]);

                    if (targetIpl.isHyperStack())
                        targetIpl.setPosition(c + 1, z + 1, t + 1);
                    else
                        targetIpl.setPosition(z + 1);

                    targetIpl.setProcessor(sourceIpl.getProcessor());

                }
            }
        }
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String sortAxis = parameters.getValue(SORT_AXIS);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE);
        String externalSourceName = parameters.getValue(EXTERNAL_SOURCE);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL);

        Image inputImage = workspace.getImage(inputImageName);

        if (applyToInput)
            outputImageName = inputImageName;
        else
            inputImage = new Image(outputImageName, inputImage.getImagePlus().duplicate());

        // Getting reference image
        Image referenceImage = calculationSource.equals(CalculationSources.EXTERNAL)
                ? workspace.getImage(externalSourceName)
                : inputImage;

        // Verifying reference stack is only 3D. If it isn't valid, skip sorting, but
        // all analysis to continue.
        if (testReferenceValidity(referenceImage, sortAxis, inputImage)) {
            // Convert reference image to MWArray
            MWNumericArray referenceArray = getReferenceArray(referenceImage, calculationChannel);

            // Getting optimal stack order
            int[] order = getStackOrder(referenceArray);
            if (order == null)
                return Status.FAIL;

            // Applying order to stack
            reorderStack(inputImage, order, sortAxis);

        } else {
            MIA.log.writeWarning("Input stack has not been sorted");
        }

        if (!applyToInput)
            workspace.addImage(inputImage);

        if (showOutput)
            inputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(SORT_SEPARATOR, this));
        parameters.add(new ChoiceP(SORT_AXIS, this, SortAxes.TIME, SortAxes.ALL));
        parameters.add(new ChoiceP(CALCULATION_SOURCE, this, CalculationSources.INTERNAL, CalculationSources.ALL));
        parameters.add(new InputImageP(EXTERNAL_SOURCE, this));
        parameters.add(new IntegerP(CALCULATION_CHANNEL, this, 1));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(SORT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SORT_AXIS));
        returnedParameters.add(parameters.getParameter(CALCULATION_SOURCE));
        switch ((String) parameters.getValue(CALCULATION_SOURCE)) {
            case CalculationSources.EXTERNAL:
                returnedParameters.add(parameters.getParameter(EXTERNAL_SOURCE));
                break;
        }

        returnedParameters.add(parameters.getParameter(CALCULATION_CHANNEL));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {

    }
}
