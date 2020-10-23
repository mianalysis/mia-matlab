% stack_in - The stack to order

% ref_stack - The stack to calculate the alignment for.  This should have a
% single channel and the same number of slices as stack_in.

% verbose - Boolean controlling if messages are displayed during execution.

function ord = getOptimisedOrder(stack, verbose)
% The number of images in the stack
nIm = size(stack,3);

% Creating forward order
if verbose
    javaMethod('println',java.lang.System.out,'[Sort stack] Sorting forward (0%)');
end

% Creating a store for the slice order
ord = zeros(nIm,2);
ord(:,1) = 1:nIm;

count = 0;
total = (nIm*(nIm+1))/2;
for i = 1:nIm
    idx = i+1:nIm;
    vals = zeros(1,numel(idx));
    
    for j = i+1:nIm
        corr_im = normxcorr2(stack(:,:,i),stack(:,:,j));
        vals(j-i) = max(corr_im(:));
    end
    
    max_pos = find(vals == max(vals),1);
    
    if max_pos == 1
        % If the best fit was the one immediately after the current frame
        % we assume the current frame was already in the optimal position
        ord(i,2) = i;
    elseif numel(max_pos) == 0
        % If no measurements were taken (i.e. the final frame), use the
        % current position
        ord(i,2) = i;
    else
        ord(i,2) = idx(max_pos);
    end
    
    if verbose
        count = count + 1;
        pc = sprintf('%0.2f',(100*count/total));
        str = ['[Sort stack] Sorting forward (',num2str(pc),'%)'];
        javaMethod('println',java.lang.System.out,str);
    end    
end

% Determining slice order based on minimum cost
ord = sortrows(ord,2);
stack = stack(:,:,ord(:,1));

% Creating reverse order
ord = flip(ord,1);
stack = flip(stack,3);

if verbose
    javaMethod('println',java.lang.System.out,'[Sort stack] Sorting backward (0%)');
end

count = 0;
ord(:,2) = 0;
for i = 1:nIm
    idx = i+1:nIm;
    vals = zeros(1,numel(idx));
    
    for j = i+1:nIm
        corr_im = normxcorr2(stack(:,:,i),stack(:,:,j));
        vals(j-i) = max(corr_im(:));
    end
    
    max_pos = find(vals == max(vals),1);
    
    if max_pos == 1
        % If the best fit was the one immediately after the current frame
        % we assume the current frame was already in the optimal position
        ord(i,2) = i;
    elseif numel(max_pos) == 0
        % If no measurements were taken (i.e. the final frame), use the
        % current position
        ord(i,2) = i;
    else
        ord(i,2) = idx(max_pos);
    end
    
    if verbose
        count = count + 1;
        pc = sprintf('%0.2f',(100*count/total));
        str = ['[Sort stack] Sorting backward (',num2str(pc),'%)'];
        javaMethod('println',java.lang.System.out,str);
    end
end

% Ordering based on backwards run, then reversing to put it back into the
% standard order
ord = sortrows(ord,2);
ord = flip(ord,1);

end