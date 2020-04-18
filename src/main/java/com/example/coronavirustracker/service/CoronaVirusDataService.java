package com.example.coronavirustracker.service;

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

@Service
public class CoronaVirusDataService {

	private static final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	private Logger logger = LoggerFactory.getLogger(CoronaVirusDataService.class);
	private List<LocationStats> allStats = new ArrayList<>();
	private long startTime;
	private long executionTime;
	public List<LocationStats> getAllStats() {
		return allStats;
	}

	@PostConstruct
	@Scheduled(cron = "0 0 * * * *")
	//cron will schedule this service 0th sec,0th min of every hour of every day
	public void fetchVirusData() throws IOException, InterruptedException {
		startTime = System.nanoTime();
		List<LocationStats> newStats = new ArrayList<>();
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
									.uri(URI.create(VIRUS_DATA_URL))
									.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		StringReader csvBodyReader = new StringReader(response.body());
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvBodyReader);
		List<LocationStats> indiaStats = new ArrayList<>();

		for (CSVRecord record : records) {
			LocationStats stat = new LocationStats();
			stat.setState(record.get(0));
			stat.setCountry(record.get(1));
			int latestCases = Integer.parseInt(record.get(record.size()-1));
			int prevDayCases = Integer.parseInt(record.get(record.size()-2));
			stat.setLatestTotalCases(latestCases);
			stat.setDiffFromPrevDay(latestCases-prevDayCases);

			//getting out indiaStats to show on top.
			if(!stat.getCountry().equalsIgnoreCase("india")) {
				newStats.add(stat);
			}else{
				indiaStats.add(stat);
			}
		}

		logger.info("India has "+indiaStats.size()+" coronaVirus hotspot");
		newStats.addAll(0,indiaStats);
		this.allStats = newStats;
		executionTime = (System.nanoTime() - startTime) / 1_000_000;
		logger.info("fetchVirusData and update took: "+executionTime+"mSecs");
	}
}
