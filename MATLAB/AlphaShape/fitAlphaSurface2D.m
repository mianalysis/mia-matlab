function [in_pts, res] = fitAlphaSurface2D(pts, radius, verbose)

% Converting pts to double precision and one-indexing
pts = double(pts)+1;

% Fitting alpha shape
if verbose
    javaMethod('writeStatus',io.github.mianalysis.mia.module.Module,'Fitting alpha shape','Fit alpha shape');
end

% Getting coordinate limits
minX = min(pts(:,1));
maxX = max(pts(:,1));
minY = min(pts(:,2));
maxY = max(pts(:,2));

if isnan(radius)
    shp = alphaShape(pts(:,1),pts(:,2));
else
    shp = alphaShape(pts(:,1),pts(:,2),radius);
end

% Getting alpha value
res.alpha = shp.Alpha;

if verbose
    javaMethod('writeStatus',io.github.mianalysis.mia.module.Module,'Fitting complete','Fit alpha shape');
end

% Testing if the alpha shape was formed correctly
if shp.area == 0
    in_pts = [];
    return;
end

if verbose
    javaMethod('writeStatus',io.github.mianalysis.mia.module.Module,'Extracting internal points','Fit alpha shape');
end

% Creating an array to hold logical values of which pixels are inside the
% alpha shape
[xx,yy] = meshgrid(minX:maxX,minY:maxY);

% Testing for pixels inside the alpha shape
inside = shp.inShape(xx,yy);
[in_pts(:,2),in_pts(:,1)] = ind2sub(size(inside),find(inside));
in_pts(:,1) = in_pts(:,1) + minX - 2;
in_pts(:,2) = in_pts(:,2) + minY - 2;
in_pts(:,3) = 0; % Z is always 0 in 2D

if verbose
    javaMethod('writeStatus',io.github.mianalysis.mia.module.Module,'Adding measurements','Fit alpha shape');
end

% Adding results
res.area = shp.area;
res.perimeter = shp.perimeter;
res.volume = NaN;
res.surfaceArea = NaN;

if verbose
    javaMethod('writeStatus',io.github.mianalysis.mia.module.Module,'Fitting complete','Fit alpha shape');
end

clear pts radius verbose inside minX maxX minY maxY

end