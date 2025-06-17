function [contour] = fitActiveContour(input_im, mask, n, method, smooth_factor, contraction_bias)

% Fitting active contour
contour = activecontour(input_im, mask, n , method, 'SmoothFactor', smooth_factor, 'ContractionBias', contraction_bias);

clear input_im mask n method smooth_factor contraction_bias

end