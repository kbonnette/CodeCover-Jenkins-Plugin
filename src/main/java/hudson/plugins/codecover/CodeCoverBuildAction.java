package hudson.plugins.codecover;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.util.IOException2;
import hudson.util.NullStream;
import hudson.util.StreamTaskListener;

import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerProxy;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Build view extension by CodeCover plugin.
 *
 * As {@link CoverageObject}, it retains the overall coverage report.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CodeCoverBuildAction extends CoverageObject<CodeCoverBuildAction> implements HealthReportingAction, StaplerProxy {
	
    public final AbstractBuild<?,?> owner;

    private transient WeakReference<CoverageReport> report;


    /**
     * The thresholds that applied when this build was built.
     */
    private final CodeCoverHealthReportThresholds thresholds;

    public CodeCoverBuildAction(AbstractBuild<?,?> owner, Rule rule, Ratio statementCoverage, Ratio branchCoverage, Ratio loopCoverage, Ratio conditionCoverage, CodeCoverHealthReportThresholds thresholds) {
        this.owner = owner;
        this.statement = statementCoverage;
        this.branch = branchCoverage;
        this.loop = loopCoverage;
        this.condition = conditionCoverage;
        this.thresholds = thresholds;
    }

    public String getDisplayName() {
        return Messages.BuildAction_DisplayName();
    }

    public String getIconFileName() {
        return "graph.gif";
    }

    public String getUrlName() {
        return "codecover";
    }

    /**
     * Get the coverage {@link hudson.model.HealthReport}.
     *
     * @return The health report or <code>null</code> if health reporting is disabled.
     * @since 1.7
     */
    public HealthReport getBuildHealth() {
        if (thresholds == null) {
            // no thresholds => no report
            return null;
        }
        thresholds.ensureValid();
        int score = 100, percent;
        ArrayList<Localizable> reports = new ArrayList<Localizable>(5);
        if (statement != null && thresholds.getMaxStatement() > 0) {
            percent = statement.getPercentage();
            if (percent < thresholds.getMaxStatement()) {
                reports.add(Messages._BuildAction_Statements(statement, percent));
            }
            score = updateHealthScore(score, thresholds.getMinStatement(),
                                      percent, thresholds.getMaxStatement());
        }
        if (branch != null && thresholds.getMaxBranch() > 0) {
            percent = branch.getPercentage();
            if (percent < thresholds.getMaxBranch()) {
                reports.add(Messages._BuildAction_Branches(branch, percent));
            }
            score = updateHealthScore(score, thresholds.getMinBranch(),
                                      percent, thresholds.getMaxBranch());
        }
        if (loop != null && thresholds.getMaxLoop() > 0) {
            percent = loop.getPercentage();
            if (percent < thresholds.getMaxLoop()) {
                reports.add(Messages._BuildAction_Loops(loop, percent));
            }
            score = updateHealthScore(score, thresholds.getMinLoop(),
                                      percent, thresholds.getMaxLoop());
        }
        if (condition != null && thresholds.getMaxCondition() > 0) {
            percent = condition.getPercentage();
            if (percent < thresholds.getMaxCondition()) {
                reports.add(Messages._BuildAction_Conditions(condition, percent));
            }
            score = updateHealthScore(score, thresholds.getMinCondition(),
                                      percent, thresholds.getMaxCondition());
        }
        if (score == 100) {
            reports.add(Messages._BuildAction_Perfect());
        }
        // Collect params and replace nulls with empty string
        Object[] args = reports.toArray(new Object[5]);
        for (int i = 4; i >= 0; i--) if (args[i]==null) args[i] = ""; else break;
        return new HealthReport(score, Messages._BuildAction_Description(
                args[0], args[1], args[2], args[3], args[4]));
    }

    private static int updateHealthScore(int score, int min, int value, int max) {
        if (value >= max) return score;
        if (value <= min) return 0;
        assert max != min;
        final int scaled = (int) (100.0 * ((float) value - min) / (max - min));
        if (scaled < score) return scaled;
        return score;
    }

    public Object getTarget() {
        return getResult();
    }

    @Override
    public AbstractBuild<?,?> getBuild() {
        return owner;
    }
    
	protected static File getCodeCoverReport(File file) throws IOException, InterruptedException {
		if (file.isDirectory()) {
			File report = null;
			File[] files = file.listFiles();
			for(int i = 0; i < files.length; i++){
				if (files[i].getName().equals("report.html")) {
					report = files[i];
					break;
				}
			}
			return report;
		} else {
			return file;
		}
	}

    /**
     * Obtains the detailed {@link CoverageReport} instance.
     */
    public synchronized CoverageReport getResult() {

        if(report!=null) {
            final CoverageReport r = report.get();
            if(r!=null)     return r;
        }

		// Generate the report
		CoverageReport r = new CoverageReport(this);

		report = new WeakReference<CoverageReport>(r);
		return r;
    }

    @Override
    public CodeCoverBuildAction getPreviousResult() {
        return getPreviousResult(owner);
    }

    /**
     * Gets the previous {@link CodeCoverBuildAction} of the given build.
     */
    /*package*/ static CodeCoverBuildAction getPreviousResult(AbstractBuild<?,?> start) {
        AbstractBuild<?,?> b = start;
        while(true) {
            b = b.getPreviousBuild();
            if(b==null)
                return null;
            if(b.getResult()== Result.FAILURE)
                continue;
            CodeCoverBuildAction r = b.getAction(CodeCoverBuildAction.class);
            if(r!=null)
                return r;
        }
    }

    /**
     * Constructs the object from codecover XML report files.
     *
     * @throws IOException
     *      if failed to parse the file.
     */
    public static CodeCoverBuildAction load(AbstractBuild<?,?> owner, Rule rule, CodeCoverHealthReportThresholds thresholds, FilePath... files) throws IOException {
        Ratio ratios[] = null;
        for (FilePath f: files ) {
            InputStream in = f.read();
            try {
                ratios = loadRatios(in, ratios);
            } finally {
                in.close();
            }
        }
        return new CodeCoverBuildAction(owner,rule,ratios[0],ratios[1],ratios[2],ratios[3],thresholds);
    }

    public static CodeCoverBuildAction load(AbstractBuild<?,?> owner, Rule rule, CodeCoverHealthReportThresholds thresholds, InputStream... streams) throws IOException {
        Ratio ratios[] = null;
        for (InputStream in: streams) {
          ratios = loadRatios(in, ratios);
        }
        return new CodeCoverBuildAction(owner,rule,ratios[0],ratios[1],ratios[2],ratios[3],thresholds);
    }

    private static Ratio[] loadRatios(InputStream in, Ratio[] r) throws IOException {
	
        if (r == null || r.length < 4) 
            r = new Ratio[4];
	
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
        try {
			String line = null;
			while((line = reader.readLine()) != null) {
				if(line.contains("&nbsp;/&nbsp;")) {
					line = line.trim();
					String[] parts = line.split("&nbsp;/&nbsp;");
					int numerator = 0;
					int denominator = 0;
					try {
						numerator = Integer.parseInt(parts[0]);
						denominator = Integer.parseInt(parts[1]);
					} catch (NumberFormatException e) {
						numerator = -1;
						denominator = -1;
					}
					if (r[0] == null) {
						r[0] = new Ratio((float) numerator, (float) denominator);
					} else if (r[1] == null) {
						r[1] = new Ratio((float) numerator, (float) denominator);
					} else if (r[2] == null) {
						r[2] = new Ratio((float) numerator, (float) denominator);
					} else if (r[3] == null) {
						r[3] = new Ratio((float) numerator, (float) denominator);
						break;
					}
				}
			}
			reader.close();
		} finally {
                reader.close();
        }
        return r;

    }

    private static final Logger logger = Logger.getLogger(CodeCoverBuildAction.class.getName());
}
