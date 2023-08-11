% stack_in - The stack to apply z-drift correction to

% verbose - Boolean controlling if messages are displayed during execution.

function slice_idx = matchImageInStack(stack_in, reference_slice)
% The number of images in the stack
nIm = size(stack_in,3);

max_corr = 0;
max_corr_idx = -1;

% javaMethod('writeStatus',io.github.mianalysis.mia.MIA.log,string(size(stack_in,1)));
% javaMethod('writeStatus',io.github.mianalysis.mia.MIA.log,string(size(reference_slice,1)));
% javaMethod('writeStatus',io.github.mianalysis.mia.MIA.log,string(size(stack_in,3)));
% javaMethod('writeStatus',io.github.mianalysis.mia.MIA.log,string(size(reference_slice,3)));

count = 0;
total = ((nIm-1)*(nIm))/2;
for i = 1:nIm
    corr_im = normxcorr2(stack_in(:,:,i),reference_slice(:,:,1));
    if max(corr_im(:)) > max_corr
        max_corr = max(corr_im(:));
        max_corr_idx = i;
    end
end

% Returning to 0-based value for Java
slice_idx = max_corr_idx -1;

end