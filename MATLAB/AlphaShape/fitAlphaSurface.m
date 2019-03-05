function [in_pts, res] = fitAlphaSurface(pts, radius, xyzConversion, verbose)

% pts must be double precision
pts = double(pts);

% Fitting alpha shape
if verbose
    javaMethod('println',java.lang.System.out,'[Fit alpha shape] Fitting alpha shape');
end

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

% Getting coordinate limits
minX = min(pts(:,1));
maxX = max(pts(:,1));
minY = min(pts(:,2));
maxY = max(pts(:,2));
minZ = min(pts(:,3));
maxZ = max(pts(:,3));

% Testing if the alpha shape was formed correctly
if minZ == maxZ
    if shp.area == 0
        in_pts = [];
        return;
    end
else
    if shp.volume == 0
        in_pts = [];
        return;
    end
end

if verbose
    javaMethod('println',java.lang.System.out,'[Fit alpha shape] Estimating object size');
end

count = 1;
for x=minX:maxX
    for y=minY:maxY
        for z=minZ:maxZ
            inside = shp.inShape(x,y,z);
            
            if inside
                count = count + 1;
            end
        end
    end
end

if verbose
    javaMethod('println',java.lang.System.out,'[Fit alpha shape] Extracting internal points');
end
in_pts = zeros(count,3);
count = 1;
for x=minX:maxX
    for y=minY:maxY
        for z=minZ:maxZ
            inside = shp.inShape(x,y,z*xyzConversion);
            
            if inside
                in_pts(count,:) = [x,y,z];
                count = count + 1;
            end
        end
    end
end

if verbose
    javaMethod('println',java.lang.System.out,'[Fit alpha shape] Adding measurements');
end

% Adding results
if minZ == maxZ
    res.area = shp.area;
    res.perimeter = shp.perimeter;
    res.volume = NaN;
    res.surfaceArea = NaN;
else
    res.area = NaN;
    res.perimeter = NaN;
    res.volume = shp.volume;
    res.surfaceArea = shp.surfaceArea;
end

if verbose
    javaMethod('println',java.lang.System.out,'[Fit alpha shape] Fitting complete');
end

end