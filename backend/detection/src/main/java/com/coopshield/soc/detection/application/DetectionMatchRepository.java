package com.coopshield.soc.detection.application;

import com.coopshield.soc.detection.domain.DetectionMatch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DetectionMatchRepository {

    void save(DetectionMatch match);

    List<DetectionMatch> findByCorrelationId(String correlationId);

    List<DetectionMatch> findAll();

    Optional<DetectionMatch> findById(UUID matchId);
}
