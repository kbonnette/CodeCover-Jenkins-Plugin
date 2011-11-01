package hudson.plugins.codecover;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * {@link Publisher} that captures CodeCover coverage reports.
 *
 * @author Kohsuke Kawaguchi
 */
public class CodeCoverPublisher extends Recorder {
    /**
     * Relative path to the CodeCover XML file inside the workspace.
     */
    public String includes;

    /**
     * Rule to be enforced. Can be null.
     *
     * TODO: define a configuration mechanism.
     */
    public Rule rule;

    /**
     * {@link hudson.model.HealthReport} thresholds to apply.
     */
    public CodeCoverHealthReportThresholds healthReports = new CodeCoverHealthReportThresholds();
    
    /**
     * look for codecover reports based in the configured parameter includes.
     * 'includes' is 
     *   - an Ant-style pattern
     *   - a list of files and folders separated by the characters ;:,  
     */
    protected static FilePath[] locateCoverageReports(FilePath workspace, String includes) throws IOException, InterruptedException {

    	// First use ant-style pattern
    	try {
        	FilePath[] ret = workspace.list(includes);
            if (ret.length > 0) { 
            	return ret;
            }
        } catch (Exception e) {
        }

        // If it fails, do a legacy search
        ArrayList<FilePath> files = new ArrayList<FilePath>();
		String parts[] = includes.split("\\s*[;:,]+\\s*");
		for (String path : parts) {
			FilePath src = workspace.child(path);
			if (src.exists()) {
				if (src.isDirectory()) {
					files.addAll(Arrays.asList(src.list("**/coverage*.xml")));
				} else {
					files.add(src);
				}
			}
		}
		return files.toArray(new FilePath[files.size()]);
	}
	
    /**
     * save codecover reports from the workspace to build folder  
     */
	protected static void saveCoverageReports(FilePath folder, FilePath[] files) throws IOException, InterruptedException {
		folder.mkdirs();
		for (int i = 0; i < files.length; i++) {
			String name = "coverage" + (i > 0 ? i : "") + ".xml";
			FilePath src = files[i];
			FilePath dst = folder.child(name);
			src.copyTo(dst);
		}
	}

    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        EnvVars env = build.getEnvironment(listener);
        env.overrideAll(build.getBuildVariables());
        
        includes = env.expand(includes);
        
        final PrintStream logger = listener.getLogger();

        FilePath[] reports;
        if (includes == null || includes.trim().length() == 0) {
            logger.println("CodeCover: looking for coverage reports in the entire workspace: " + build.getWorkspace().getRemote());
            reports = locateCoverageReports(build.getWorkspace(), "report.html");
        } else {
            logger.println("CodeCover: looking for coverage reports in the provided path: " + includes );
            reports = locateCoverageReports(build.getWorkspace(), includes);
        }
        
        if (reports.length == 0) {
            if(build.getResult().isWorseThan(Result.UNSTABLE))
                return true;
            
            logger.println("CodeCover: no coverage files found in workspace. Was any report generated?");
            build.setResult(Result.FAILURE);
            return true;
        } else {
        	String found = "";
        	for (FilePath f: reports) 
        		found += "\n          " + f.getRemote();
            logger.println("CodeCover: found " + reports.length  + " report files: " + found );
        }
        
        FilePath codecoverfolder = new FilePath(getCodeCoverReport(build));
        saveCoverageReports(codecoverfolder, reports);
        logger.println("CodeCover: stored " + reports.length + " report files in the build folder: "+ codecoverfolder);
        
        final CodeCoverBuildAction action = CodeCoverBuildAction.load(build, rule, healthReports, reports);
        
        logger.println("CodeCover: " + action.getBuildHealth().getDescription());

        build.getActions().add(action);

        final CoverageReport result = action.getResult();
        if (result == null) {
            logger.println("CodeCover: Could not parse coverage results. Setting Build to failure.");
            build.setResult(Result.FAILURE);
        } else if (result.isFailed()) {
            logger.println("CodeCover: code coverage enforcement failed. Setting Build to unstable.");
            build.setResult(Result.UNSTABLE);
        }

        return true;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new CodeCoverProjectAction(project);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    /**
     * Gets the directory to store report files
     */
    static File getCodeCoverReport(AbstractBuild<?,?> build) {
        return new File(build.getRootDir(), "codecover");
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final BuildStepDescriptor<Publisher> DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public DescriptorImpl() {
            super(CodeCoverPublisher.class);
        }

        public String getDisplayName() {
            return Messages.CodeCoverPublisher_DisplayName();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject json) throws FormException {
            CodeCoverPublisher pub = new CodeCoverPublisher();
            req.bindParameters(pub, "codecover.");
            req.bindParameters(pub.healthReports, "codecoverHealthReports.");
			//set max defaults
            if ("".equals(req.getParameter("codecoverHealthReports.maxStatement"))) {
                pub.healthReports.setMaxStatement(90);
            }
            if ("".equals(req.getParameter("codecoverHealthReports.maxBranch"))) {
                pub.healthReports.setMaxBranch(80);
            }
            if ("".equals(req.getParameter("codecoverHealthReports.maxLoop"))) {
                pub.healthReports.setMaxLoop(50);
            }
            if ("".equals(req.getParameter("codecoverHealthReports.maxCondition"))) {
                pub.healthReports.setMaxCondition(50);
            }
            // end ugly hack
            return pub;
        }
    }
}
