package io.github.mianalysis.mia_matlab.module;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import ij.ImageStack;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.sjcross.common.object.Point;
import io.github.sjcross.common.object.volume.Volume;

public abstract class CoreMATLABModule extends Module {
   
    public CoreMATLABModule(String name, Modules modules) {
        super(name, modules);
    }
    
    public static MWNumericArray coordsToMW(Volume surface) {
        double[][] pointArray = new double[surface.size()][3];

        int i = 0;
        for (Point<Integer> point : surface.getCoordinateSet()) {
            pointArray[i][0] = point.getX();
            pointArray[i][1] = point.getY();
            pointArray[i++][2] = point.getZ();
        }

        return new MWNumericArray(pointArray, MWClassID.DOUBLE);

    }

    public static MWNumericArray imageStackToMW(ImageStack ist) {
        int w = ist.getWidth();
        int h = ist.getHeight();
        int nZ = ist.getSize();

        double[][][] imageArray = new double[w][h][nZ];

        for (int z = 0; z < nZ; z++) {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    imageArray[x][y][z] = ist.getVoxel(x, y, z);
                }
            }
        }

        return new MWNumericArray(imageArray, MWClassID.DOUBLE);
        
    }
}
