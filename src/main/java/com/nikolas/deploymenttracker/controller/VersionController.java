package com.nikolas.deploymenttracker.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

  @GetMapping("/api/version")
  public Map<String, String> version() {
    return Map.of("version", "1.1");
  }
}
