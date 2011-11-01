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
package hudson.plugins.codecover.portlet;

import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.codecover.CodeCoverBuildAction;
import hudson.plugins.codecover.portlet.bean.CodeCoverCoverageResultSummary;
import hudson.plugins.codecover.portlet.utils.Utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.LocalDate;

/**
 * Load data of CodeCover coverage results used by chart or grid.
 */
public final class CodeCoverLoadData {

  /**
   * Private constructor avoiding this class to be used in a non-static way.
   */
  private CodeCoverLoadData() {
  }

  /**
   * Get CodeCover coverage results of all jobs and store into a sorted
   * HashMap by date.
   *
   * @param jobs
   *        jobs of Dashboard view
   * @param daysNumber
   *          number of days
   * @return Map The sorted summaries
   */
  public static Map<LocalDate, CodeCoverCoverageResultSummary> loadChartDataWithinRange(List<Job> jobs, int daysNumber) {

    Map<LocalDate, CodeCoverCoverageResultSummary> summaries = new HashMap<LocalDate, CodeCoverCoverageResultSummary>();

    // Get the last build (last date) of the all jobs
    LocalDate lastDate = Utils.getLastDate(jobs);

    // No builds
    if (lastDate == null) {
      return null;
    }

    // Get the first date from last build date minus number of days
    LocalDate firstDate = lastDate.minusDays(daysNumber);

    // For each job, get CodeCover coverage results according with
    // date range (last build date minus number of days)
    for (Job job : jobs) {

      Run run = job.getLastBuild();

      if (null != run) {
        LocalDate runDate = new LocalDate(run.getTimestamp());

        while (runDate.isAfter(firstDate)) {

          summarize(summaries, run, runDate, job);

          run = run.getPreviousBuild();

          if (null == run) {
            break;
          }

          runDate = new LocalDate(run.getTimestamp());

        }
      }
    }

    // Sorting by date, ascending order
    Map<LocalDate, CodeCoverCoverageResultSummary> sortedSummaries = new TreeMap(summaries);

    return sortedSummaries;

  }

  /**
   * Summarize CodeCover converage results.
   *
   * @param summaries
   *          a Map of CodeCoverCoverageResultSummary objects indexed by
   *          dates
   * @param run
   *          the build which will provide information about the
   *          coverage result
   * @param runDate
   *          the date on which the build was performed
   * @param job
   *          job from the DashBoard Portlet view
   */
  private static void summarize(Map<LocalDate, CodeCoverCoverageResultSummary> summaries, Run run, LocalDate runDate, Job job) {

    CodeCoverCoverageResultSummary codecoverCoverageResult = getResult(run);

    // Retrieve CodeCover information for informed date
    CodeCoverCoverageResultSummary codecoverCoverageResultSummary = summaries.get(runDate);

    // Consider the last result of each
    // job date (if there are many builds for the same date). If not
    // exists, the CodeCover coverage data must be added. If exists
    // CodeCover coverage data for the same date but it belongs to other
    // job, sum the values.
    if (codecoverCoverageResultSummary == null) {
      codecoverCoverageResultSummary = new CodeCoverCoverageResultSummary();
      codecoverCoverageResultSummary.addCoverageResult(codecoverCoverageResult);
      codecoverCoverageResultSummary.setJob(job);
    } else {

      // Check if exists CodeCover data for same date and job
      List<CodeCoverCoverageResultSummary> listResults = codecoverCoverageResultSummary.getCodeCoverCoverageResults();
      boolean found = false;

      for (CodeCoverCoverageResultSummary item : listResults) {
        if ((null != item.getJob()) && (null != item.getJob().getName()) && (null != job)) {
          if (item.getJob().getName().equals(job.getName())) {
            found = true;
            break;
          }
        }
      }

      if (!found) {
        codecoverCoverageResultSummary.addCoverageResult(codecoverCoverageResult);
        codecoverCoverageResultSummary.setJob(job);
      }
    }

    summaries.put(runDate, codecoverCoverageResultSummary);
  }

  /**
   * Get the CodeCover coverage result for a specific run.
   *
   * @param run
   *          a job execution
   * @return CodeCoverCoverageTestResult the coverage result
   */
  private static CodeCoverCoverageResultSummary getResult(Run run) {
    CodeCoverBuildAction codecoverAction = run.getAction(CodeCoverBuildAction.class);

    float statementCoverage = 0.0f;
    float branchCoverage = 0.0f;
    float loopCoverage = 0.0f;
    float conditionCoverage = 0.0f;

    if (codecoverAction != null) {
      if (null != codecoverAction.getStatementCoverage()) {
        statementCoverage = codecoverAction.getStatementCoverage().getPercentageFloat();
      }
      if (null != codecoverAction.getBranchCoverage()) {
        branchCoverage = codecoverAction.getBranchCoverage().getPercentageFloat();
      }
      if (null != codecoverAction.getLoopCoverage()) {
        loopCoverage = codecoverAction.getLoopCoverage().getPercentageFloat();
      }
      if (null != codecoverAction.getConditionCoverage()) {
        conditionCoverage = codecoverAction.getConditionCoverage().getPercentageFloat();
      }
    }
    return new CodeCoverCoverageResultSummary(run.getParent(), statementCoverage, branchCoverage, loopCoverage, conditionCoverage);
  }

  /**
   * Summarize the last coverage results of all jobs. If a job doesn't
   * include any coverage, add zero.
   *
   * @param jobs
   *          a final Collection of Job objects
   * @return CodeCoverCoverageResultSummary the result summary
   */
  public static CodeCoverCoverageResultSummary getResultSummary(final Collection<Job> jobs) {
    CodeCoverCoverageResultSummary summary = new CodeCoverCoverageResultSummary();

    for (Job job : jobs) {

      float statementCoverage = 0.0f;
      float branchCoverage = 0.0f;
      float loopCoverage = 0.0f;
      float conditionCoverage = 0.0f;

      Run run = job.getLastSuccessfulBuild();

      if (run != null) {

        CodeCoverBuildAction codecoverAction = job.getLastSuccessfulBuild().getAction(CodeCoverBuildAction.class);

        if (null != codecoverAction) {
          if (null != codecoverAction.getStatementCoverage()) {
            statementCoverage = codecoverAction.getStatementCoverage().getPercentageFloat();
            BigDecimal bigStatementCoverage = new BigDecimal(statementCoverage);
            bigStatementCoverage = bigStatementCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            statementCoverage = bigStatementCoverage.floatValue();
          }

          if (null != codecoverAction.getBranchCoverage()) {
            branchCoverage = codecoverAction.getBranchCoverage().getPercentageFloat();
            BigDecimal bigBranchCoverage = new BigDecimal(branchCoverage);
            bigBranchCoverage = bigBranchCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            branchCoverage = bigBranchCoverage.floatValue();
          }
          if (null != codecoverAction.getLoopCoverage()) {
            loopCoverage = codecoverAction.getLoopCoverage().getPercentageFloat();
            BigDecimal bigLoopCoverage = new BigDecimal(loopCoverage);
            bigLoopCoverage = bigLoopCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            loopCoverage = bigLoopCoverage.floatValue();
          }

          if (null != codecoverAction.getConditionCoverage()) {
            conditionCoverage = codecoverAction.getConditionCoverage().getPercentageFloat();
            BigDecimal getConditionCoverage = new BigDecimal(conditionCoverage);
            getConditionCoverage = getConditionCoverage.setScale(1, BigDecimal.ROUND_HALF_EVEN);
            conditionCoverage = getConditionCoverage.floatValue();
          }
        }
      }
      summary.addCoverageResult(new CodeCoverCoverageResultSummary(job, statementCoverage, branchCoverage, loopCoverage,
        conditionCoverage));
    }
    return summary;
  }
}
