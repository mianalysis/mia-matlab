projectRoot = "F:\Java Projects\mia-matlab\MATLAB";

% Create target build options object, set build properties and build.
classmap = containers.Map;
classmap("AlphaShapeFitter") = [fullfile(projectRoot, "AlphaShape", "fitAlphaSurface.m"), fullfile(projectRoot, "AlphaShape", "fitAlphaSurface2D.m"), fullfile(projectRoot, "AlphaShape", "fitAlphaSurface3D.m"), fullfile(projectRoot, "AlphaShape", "fitAlphaSurfaceAuto.m")];
buildOpts = compiler.build.JavaPackageOptions(classmap);
buildOpts.AutoDetectDataFiles = true;
buildOpts.OutputDir = fullfile(projectRoot, "MIA_MATLAB", "for_testing");
buildOpts.ObfuscateArchive = false;
buildOpts.Verbose = true;
buildOpts.DebugBuild = false;
buildOpts.PackageName = "AlphaShapeFitter";
buildResult = compiler.build.javaPackage(buildOpts);


% Create package options object, set package properties and package.
packageOpts = compiler.package.InstallerOptions(buildResult);
packageOpts.ApplicationName = "AlphaShapeFitter";
packageOpts.AuthorName = "Stephen Cross";
packageOpts.AuthorEmail = "stephen.cross@bristol.ac.uk";
packageOpts.AuthorCompany = "University of Bristol";
packageOpts.DefaultInstallationDir = "%ProgramFiles%//University_of_Bristol/MIA_MATLAB_Core/";
packageOpts.InstallerName = "MyAppInstaller_web";
packageOpts.OutputDir = fullfile(projectRoot, "MIA_MATLAB", "for_redistribution");
packageOpts.Verbose = true;
packageOpts.Version = "1.6.0";
compiler.package.installer(buildResult, "Options", packageOpts);