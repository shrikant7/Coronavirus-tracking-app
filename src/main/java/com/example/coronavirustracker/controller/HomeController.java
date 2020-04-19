package com.example.coronavirustracker.controller;

import com.example.coronavirustracker.model.GlobalStats;
import com.example.coronavirustracker.model.LocationStats;
import com.example.coronavirustracker.service.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

	@Autowired
	CoronaVirusDataService coronaVirusDataService;

	@GetMapping("/")
	public String home(Model model){
		List<LocationStats> allStats = coronaVirusDataService.getAllStats();
		GlobalStats globalStats = coronaVirusDataService.getGlobalStats();
		model.addAttribute("locationStats",allStats);
		model.addAttribute("globalStats",globalStats);
		//model.addAttribute("totalNewCases",totalNewCases);
		return "home";
	}
}
