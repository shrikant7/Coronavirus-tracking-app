package com.example.coronavirustracker.service;

import com.example.coronavirustracker.model.GlobalStats;
import com.example.coronavirustracker.model.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class CoronaVirusDataService {

	private static final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	private static final String RECOVERED_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_recovered_global.csv";
	private static final String DEATHS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";

	private Logger logger = LoggerFactory.getLogger(CoronaVirusDataService.class);
	private List<LocationStats> allStats = new ArrayList<>();
	private GlobalStats globalStats = new GlobalStats();
	private long startTime;
	private long executionTime;

	public List<LocationStats> getAllStats() {
		return allStats;
	}

	public GlobalStats getGlobalStats() {
		return globalStats;
	}

	@PostConstruct
	@Scheduled(cron = "0 0 */6 * * *")
	//cron will schedule this service 0th sec,0th min of every 6th hour of every day
	public void fetchVirusData() throws IOException, InterruptedException, ExecutionException {
		startTime = System.nanoTime();
		List<LocationStats> newStats = new ArrayList<>();
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest virusDataRequest = getNewHttpRequest(VIRUS_DATA_URL);
		HttpRequest deathDataRequest = getNewHttpRequest(DEATHS_DATA_URL);
		HttpRequest recoveredDataRequest = getNewHttpRequest(RECOVERED_DATA_URL);

		CompletableFuture<HttpResponse<String>> virusDataResponseFuture = client.sendAsync(virusDataRequest, HttpResponse.BodyHandlers.ofString());
		CompletableFuture<HttpResponse<String>> deathDataResponseFuture = client.sendAsync(deathDataRequest, HttpResponse.BodyHandlers.ofString());
		CompletableFuture<HttpResponse<String>> recoveredDataResponseFuture = client.sendAsync(recoveredDataRequest, HttpResponse.BodyHandlers.ofString());

		HttpResponse<String> virusDataResponse = virusDataResponseFuture.get();
		HttpResponse<String> deathDataResponse = deathDataResponseFuture.get();
		HttpResponse<String> recoveredDataResponse = recoveredDataResponseFuture.get();

		Iterable<CSVRecord> virusRecords = getCsvRecords(virusDataResponse.body());
		Iterable<CSVRecord> deathRecords = getCsvRecords(deathDataResponse.body());
		Iterable<CSVRecord> recoveredRecords = getCsvRecords(recoveredDataResponse.body());


		int totalDeathCases = 0;
		for(CSVRecord record : deathRecords){
			totalDeathCases += Integer.parseInt(record.get(record.size()-1));
		}
		int totalRecoveredCases = 0;
		for(CSVRecord record : recoveredRecords){
			totalRecoveredCases += Integer.parseInt(record.get(record.size()-1));
		}

		int totalReportedCases = 0;
		int totalNewCases = 0;
		List<LocationStats> indiaStats = new ArrayList<>();
		for (CSVRecord record : virusRecords) {
			LocationStats stat = new LocationStats();
			stat.setState(record.get(0));
			stat.setCountry(record.get(1));
			int latestCases = Integer.parseInt(record.get(record.size()-1));
			int prevDayCases = Integer.parseInt(record.get(record.size()-2));
			stat.setLatestTotalCases(latestCases);
			stat.setDiffFromPrevDay(latestCases-prevDayCases);

			totalNewCases += (latestCases-prevDayCases);
			totalReportedCases += latestCases;

			//getting out indiaStats to show on top.
			if(!stat.getCountry().equalsIgnoreCase("india")) {
				newStats.add(stat);
			}else{
				indiaStats.add(stat);
			}
		}

		logger.info("India has "+indiaStats.size()+" coronaVirus hotspot");
		// adding india stats on top in list.
		newStats.addAll(0,indiaStats);
		this.allStats = newStats;
		globalStats = new GlobalStats()
				.setTotalReportedCases(totalReportedCases)
				.setTotalNewCases(totalNewCases)
				.setTotalRecoveredCases(totalRecoveredCases)
				.setTotalDeathCases(totalDeathCases);

		executionTime = (System.nanoTime() - startTime) / 1_000_000;
		logger.info("fetchVirusData and update took: "+executionTime+" mSecs");
	}

	private HttpRequest getNewHttpRequest(String url){
		return HttpRequest.newBuilder()
				.uri(URI.create(url))
				.build();
	}

	private Iterable<CSVRecord> getCsvRecords(String data) throws IOException {
		// auto closable try for StringReader
		StringReader virusCsvBodyReader = new StringReader(data);
			return CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(virusCsvBodyReader);

	}
}
