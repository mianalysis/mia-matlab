function [contour] = fitActiveContour(input_im, mask, n, method, smooth_factor, contraction_bias, verbose)

% Fitting active contour
if verbose
    javaMethod('writeStatus',io.github.mianalysis.mia.module.Module,'Fitting active contour','Fit active contour');
end

contour = activecontour(input_im, mask, n , method, 'SmoothFactor', smooth_factor, 'ContractionBias', contraction_bias);

clear input_im mask n method smooth_factor contraction_bias

end