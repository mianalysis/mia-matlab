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
import io.github.mianalysis.mia.object.image.ImageFactory;
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

    private static final String DRIFT_CORRECTION_SEPARATOR = "Drift correction controls";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String CALCULATION_CHANNEL = "Calculation channel";

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
        String refImageName = parameters.getValue(REFERENCE_IMAGE, workspace);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL, workspace);

        // Getting input images
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();
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
        for (int t = 1; t <= inputIpl.getNFrames(); t++) {
            Image substack = ExtractSubstack.extractSubstack(inputImage, "Substack", String.valueOf(calculationChannel),
                    "1-end", String.valueOf(t));
            MWNumericArray substackArray = imageStackToMW(substack.getImagePlus().getStack());

            try {
                Object[] output = stackMatcher.matchImageInStack(1, substackArray, refArray);
                bestSlices[t - 1] = (int) output[0];
                MIA.log.writeDebug(bestSlices[t - 1]);
            } catch (MWException e) {
                MIA.log.writeError(e);
                return Status.FAIL;
            }

            substackArray.dispose();

        }

        Image outputImage = FocusStackGlobal.extract(inputImage,0,0,bestSlices,outputImageName);
        
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
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new IntegerP(CALCULATION_CHANNEL, this, 1));

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

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
