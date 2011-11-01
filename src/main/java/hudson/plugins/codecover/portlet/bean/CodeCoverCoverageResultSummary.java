/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

/**
 * @author Allyn Pierre (Allyn.GreyDeAlmeidaLimaPierre@sonyericsson.com)
 * @author Eduardo Palazzo (Eduardo.Palazzo@sonyericsson.com)
 * @author Mauro Durante (Mauro.DuranteJunior@sonyericsson.com)
 */
package hudson.plugins.codecover.portlet.bean;

import hudson.model.Job;
import hudson.plugins.codecover.portlet.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Summary of the CodeCover Coverage result.
 */
public class CodeCoverCoverageResultSummary {

  /**
   * The related job.
   */
  private Job job;

  /**
   * Statement coverage percentage.
   */
  private float statementCoverage;

  /**
   * Branch coverage percentage.
   */
  private float branchCoverage;

  /**
   * Loop coverage percentage.
   */
  private float loopCoverage;

  /**
   * Condition coverage percentage.
   */
  private float conditionCoverage;

  private List<CodeCoverCoverageResultSummary> coverageResults = new ArrayList<CodeCoverCoverageResultSummary>();

  /**
   * Default Constructor.
   */
  public CodeCoverCoverageResultSummary() {
  }

  /**
   * Constructor with parameters.
   *
   * @param job
   *          the related Job
   * @param statementCoverage
   *          statement coverage percentage
   * @param branchCoverage
   *          branch coverage percentage
   * @param loopCoverage
   *          loop coverage percentage
   * @param conditionCoverage
   *          condition coverage percentage
   */
  public CodeCoverCoverageResultSummary(Job job, float statementCoverage, float branchCoverage, float loopCoverage,
    float conditionCoverage) {
    this.job = job;
    this.statementCoverage = statementCoverage;
    this.branchCoverage = branchCoverage;
    this.loopCoverage = loopCoverage;
    this.conditionCoverage = conditionCoverage;
  }

  /**
   * Add a coverage result.
   *
   * @param coverageResult
   *          a coverage result
   * @return CodeCoverCoverageResultSummary summary of the CodeCover coverage
   *         result
   */
  public CodeCoverCoverageResultSummary addCoverageResult(CodeCoverCoverageResultSummary coverageResult) {

    this.setStatementCoverage(this.getStatementCoverage() + coverageResult.getStatementCoverage());
    this.setBranchCoverage(this.getBranchCoverage() + coverageResult.getBranchCoverage());
    this.setLoopCoverage(this.getLoopCoverage() + coverageResult.getLoopCoverage());
    this.setConditionCoverage(this.getConditionCoverage() + coverageResult.getConditionCoverage());

    getCoverageResults().add(coverageResult);

    return this;
  }

  /**
   * Get list of CodeCoverCoverageResult objects.
   *
   * @return List a List of CodeCoverCoverageResult objects
   */
  public List<CodeCoverCoverageResultSummary> getCodeCoverCoverageResults() {
    return this.getCoverageResults();
  }

  /**
   * Getter of the total of condition coverage.
   *
   * @return float the total of condition coverage.
   */
  public float getTotalConditionCoverage() {
    if (this.getCoverageResults().size() <= 0) {
      return 0.0f;
    } else {
      float totalCondition = this.getConditionCoverage() / this.getCoverageResults().size();
      totalCondition = Utils.roundFLoat(1, BigDecimal.ROUND_HALF_EVEN, totalCondition);
      return totalCondition;
    }
  }

  /**
   * Getter of the total of branch coverage.
   *
   * @return float the total of branch coverage.
   */
  public float getTotalBranchCoverage() {
    if (this.getCoverageResults().size() <= 0) {
      return 0.0f;
    } else {
      float totalBranch = this.getBranchCoverage() / this.getCoverageResults().size();
      totalBranch = Utils.roundFLoat(1, BigDecimal.ROUND_HALF_EVEN, totalBranch);
      return totalBranch;
    }
  }

  /**
   * Getter of the total of loop coverage.
   *
   * @return float the total of loop coverage.
   */
  public float getTotalLoopCoverage() {
    if (this.getCoverageResults().size() <= 0) {
      return 0.0f;
    } else {
      float totalLoop = this.getLoopCoverage() / this.getCoverageResults().size();
      totalLoop = Utils.roundFLoat(1, BigDecimal.ROUND_HALF_EVEN, totalLoop);
      return totalLoop;
    }
  }

  /**
   * Getter of the total of statement coverage.
   *
   * @return float the total of statement coverage.
   */
  public float getTotalStatementCoverage() {
    if (this.getCoverageResults().size() <= 0) {
      return 0.0f;
    } else {
      float totalStatement = this.getStatementCoverage() / this.getCoverageResults().size();
      totalStatement = Utils.roundFLoat(1, BigDecimal.ROUND_HALF_EVEN, totalStatement);
      return totalStatement;
    }
  }

  /**
   * @return Job a job
   */
  public Job getJob() {
    return job;
  }

  /**
   * @return the statementCoverage
   */
  public float getStatementCoverage() {
    return statementCoverage;
  }

  /**
   * @return the branchCoverage
   */
  public float getBranchCoverage() {
    return branchCoverage;
  }

  /**
   * @return the loopCoverage
   */
  public float getLoopCoverage() {
    return loopCoverage;
  }

  /**
   * @return the conditionCoverage
   */
  public float getConditionCoverage() {
    return conditionCoverage;
  }

  /**
   * @param job
   *          the job to set
   */
  public void setJob(Job job) {
    this.job = job;
  }

  /**
   * @param statementCoverage
   *          the statementCoverage to set
   */
  public void setStatementCoverage(float statementCoverage) {
    this.statementCoverage = statementCoverage;
  }

  /**
   * @param branchCoverage
   *          the branchCoverage to set
   */
  public void setBranchCoverage(float branchCoverage) {
    this.branchCoverage = branchCoverage;
  }

  /**
   * @param loopCoverage
   *          the loopCoverage to set
   */
  public void setLoopCoverage(float loopCoverage) {
    this.loopCoverage = loopCoverage;
  }

  /**
   * @param conditionCoverage
   *          the conditionCoverage to set
   */
  public void setConditionCoverage(float conditionCoverage) {
    this.conditionCoverage = conditionCoverage;
  }

  /**
   * @return a list of coverage results
   */
  public List<CodeCoverCoverageResultSummary> getCoverageResults() {
    return coverageResults;
  }

  /**
   * @param coverageResults
   *          the list of coverage results to set
   */
  public void setCoverageResults(List<CodeCoverCoverageResultSummary> coverageResults) {
    this.coverageResults = coverageResults;
  }
}
