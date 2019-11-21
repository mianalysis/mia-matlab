function [in_pts, res] = fitAlphaSurface(pts, radius, xyzConversion, verbose)

minZ = min(pts(:,3));
maxZ = max(pts(:,3));

if minZ == maxZ
    [in_pts, res] = fitAlphaSurface2D(pts, radius, verbose);
else
    [in_pts, res] = fitAlphaSurface3D(pts, radius, xyzConversion, verbose);
end

end