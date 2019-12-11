function [in_pts, res] = fitAlphaSurface3D(pts, radius, xyzConversion, verbose)

% Converting pts to double precision and one-indexing
pts = double(pts)+1;

% Fitting alpha shape
if verbose
    javaMethod('println',java.lang.System.out,'[Fit alpha shape] Fitting alpha shape');
end

% Getting coordinate limits
minX = min(pts(:,1));
maxX = max(pts(:,1));
minY = min(pts(:,2));
maxY = max(pts(:,2));
minZ = min(pts(:,3));
maxZ = max(pts(:,3));

if isnan(radius)
    shp = alphaShape(pts(:,1),pts(:,2),pts(:,3)*xyzConversion);
else
    shp = alphaShape(pts(:,1),pts(:,2),pts(:,3)*xyzConversion,radius);
end

% Getting alpha value
res.alpha = shp.Alpha;

if verbose
    javaMethod('println',java.lang.System.out,'[Fit alpha shape] Fitting complete');
end

% Testing if the alpha shape was formed correctly
if shp.volume == 0
    in_pts = [];
    return;
end

if verbose
    javaMethod('println',java.lang.System.out,'[Fit alpha shape] Extracting internal points');
end

% Creating an array to hold logical values of which pixels are inside the
% alpha shape
[xx,yy,zz] = meshgrid(minX:maxX,minY:maxY,minZ:maxZ);

% Testing for pixels inside the alpha shape
inside = shp.inShape(xx,yy,zz*xyzConversion);
[in_pts(:,2),in_pts(:,1),in_pts(:,3)] = ind2sub(size(inside),find(inside));
in_pts(:,1) = in_pts(:,1) + minX - 2;
in_pts(:,2) = in_pts(:,2) + minY - 2;
in_pts(:,3) = in_pts(:,3) + minZ - 2;

if verbose
    javaMethod('println',java.lang.System.out,'[Fit alpha shape] Adding measurements');
end

% Adding results
res.area = NaN;
res.perimeter = NaN;
res.volume = shp.volume;
res.surfaceArea = shp.surfaceArea;

if verbose
    javaMethod('println',java.lang.System.out,'[Fit alpha shape] Fitting complete');
end

clear pts radius verbose xyzConversion inside minX maxX minY maxY minZ maxZ

end