package hudson.plugins.codecover;

import java.io.Serializable;

/**
 * Holds the configuration details for {@link hudson.model.HealthReport} generation
 *
 * @author Stephen Connolly
 * @since 1.7
 */
public class CodeCoverHealthReportThresholds implements Serializable {
    private int minStatement;
    private int maxStatement;
    private int minBranch;
    private int maxBranch;
    private int minLoop;
    private int maxLoop;
    private int minCondition;
    private int maxCondition;

    public CodeCoverHealthReportThresholds() {
    }

    public CodeCoverHealthReportThresholds(int minStatement, int maxStatement, int minBranch, int maxBranch, int minLoop, int maxLoop, int minCondition, int maxCondition) {
        this.minStatement = minStatement;
        this.maxStatement = maxStatement;
        this.minBranch = minBranch;
        this.maxBranch = maxBranch;
        this.minLoop = minLoop;
        this.maxLoop = maxLoop;
        this.minCondition = minCondition;
        this.maxCondition = maxCondition;
        ensureValid();
    }

    private int applyRange(int min , int value, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public void ensureValid() {
        maxStatement = applyRange(0, maxStatement, 100);
        minStatement = applyRange(0, minStatement, maxStatement);
        maxBranch = applyRange(0, maxBranch, 100);
        minBranch = applyRange(0, minBranch, maxBranch);
        maxLoop = applyRange(0, maxLoop, 100);
        minLoop = applyRange(0, minLoop, maxLoop);
        maxCondition = applyRange(0, maxCondition, 100);
        minCondition = applyRange(0, minCondition, maxCondition);
    }

    public int getMinStatement() {
        return minStatement;
    }

    public void setMinStatement(int minStatement) {
        this.minStatement = minStatement;
    }

    public int getMaxStatement() {
        return maxStatement;
    }

    public void setMaxStatement(int maxStatement) {
        this.maxStatement = maxStatement;
    }

    public int getMinBranch() {
        return minBranch;
    }

    public void setMinBranch(int minBranch) {
        this.minBranch = minBranch;
    }

    public int getMaxBranch() {
        return maxBranch;
    }

    public void setMaxBranch(int maxBranch) {
        this.maxBranch = maxBranch;
    }

    public int getMinLoop() {
        return minLoop;
    }

    public void setMinLoop(int minLoop) {
        this.minLoop = minLoop;
    }

    public int getMaxLoop() {
        return maxLoop;
    }

    public void setMaxLoop(int maxLoop) {
        this.maxLoop = maxLoop;
    }

    public int getMinCondition() {
        return minCondition;
    }

    public void setMinCondition(int minCondition) {
        this.minCondition = minCondition;
    }

    public int getMaxCondition() {
        return maxCondition;
    }

    public void setMaxCondition(int maxCondition) {
        this.maxCondition = maxCondition;
    }
}
