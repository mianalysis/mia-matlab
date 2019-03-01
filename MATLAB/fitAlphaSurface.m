function in_pts = fitAlphaSurface(pts, radius)

% pts must be double precision
pts = double(pts);

% Fitting alpha shape
if isnan(radius)
    shp = alphaShape(pts(:,1),pts(:,2),pts(:,3));
else
    shp = alphaShape(pts(:,1),pts(:,2),pts(:,3),radius);
end

% Getting coordinate limits
minX = min(pts(:,1));
maxX = max(pts(:,1));
minY = min(pts(:,2));
maxY = max(pts(:,2));
minZ = min(pts(:,3));
maxZ = max(pts(:,3));

% Creating a regularly-spaced array of points
% [x,y,z] = meshgrid(minX:maxX,minY:maxY,minZ:maxZ);

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

in_pts = zeros(count,3);
count = 1;
for x=minX:maxX
    for y=minY:maxY
        for z=minZ:maxZ
            inside = shp.inShape(x,y,z);
            
            if inside
                in_pts(count,:) = [x,y,z];
                count = count + 1;
            end
        end
    end
end
end