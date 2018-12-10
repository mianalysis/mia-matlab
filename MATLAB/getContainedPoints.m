function in_pts = getContainedPoints(pts)
% Getting coordinate limits
minX = min(pts(:,1));
maxX = max(pts(:,1));
minY = min(pts(:,2));
maxY = max(pts(:,2));
minZ = min(pts(:,3));
maxZ = max(pts(:,3));

% Creating a regularly-spaced array of points
[x,y,z] = meshgrid(minX:maxX,minY:maxY,minZ:maxZ);

dela = delaunayn(pts,{'QJ','Pp'});
in = ~isnan(tsearchn(pts,dela,[x(:),y(:),z(:)]));

in_pts = [x(in),y(in),z(in)];

end