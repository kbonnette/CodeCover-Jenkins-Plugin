package hudson.plugins.codecover;

import hudson.model.AbstractBuild;
import hudson.util.IOException2;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Root object of the coverage report.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class CoverageReport extends CoverageObject<CoverageReport> {
    private final CodeCoverBuildAction action;
	
	private String name;
	
	public CoverageReport(CodeCoverBuildAction action) {
        this.action = action;
		this.statement = action.statement;
		this.branch = action.branch;
		this.loop = action.loop;
		this.condition = action.condition;
		
        setName("CodeCover");
    }
	
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return name;
    }

    @Override
    public CoverageReport getPreviousResult() {
        CodeCoverBuildAction prev = action.getPreviousResult();
        if(prev!=null)
            return prev.getResult();
        else
            return null;
    }

    @Override
    public AbstractBuild<?,?> getBuild() {
        return action.owner;
    }

}
