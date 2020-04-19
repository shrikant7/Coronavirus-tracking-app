package com.example.coronavirustracker.model;

/**
 * @author Shrikant Sharma
 */
public class GlobalStats {
	private int totalReportedCases;
	private int totalNewCases;
	private int totalRecoveredCases;
	private int totalDeathCases;

	public int getTotalReportedCases() {
		return totalReportedCases;
	}

	public GlobalStats setTotalReportedCases(int totalReportedCases) {
		this.totalReportedCases = totalReportedCases;
		return this;
	}

	public int getTotalRecoveredCases() {
		return totalRecoveredCases;
	}

	public GlobalStats setTotalRecoveredCases(int totalRecoveredCases) {
		this.totalRecoveredCases = totalRecoveredCases;
		return this;
	}

	public int getTotalNewCases() {
		return totalNewCases;
	}

	public GlobalStats setTotalNewCases(int totalNewCases) {
		this.totalNewCases = totalNewCases;
		return this;
	}

	public int getTotalDeathCases() {
		return totalDeathCases;
	}

	public GlobalStats setTotalDeathCases(int totalDeathCases) {
		this.totalDeathCases = totalDeathCases;
		return this;
	}
}
