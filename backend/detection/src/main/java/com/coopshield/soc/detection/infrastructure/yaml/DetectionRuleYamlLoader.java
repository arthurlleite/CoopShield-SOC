package com.coopshield.soc.detection.infrastructure.yaml;

import com.coopshield.soc.detection.domain.DetectionRule;
import com.coopshield.soc.detection.domain.InvalidDetectionRuleException;
import com.coopshield.soc.detection.domain.Severity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Carrega as regras de deteccao de {@code classpath:detection-rules/**\/*.yaml}
 * (ver ADR-006 e ADR-014 sobre por que os arquivos vivem em
 * {@code src/main/resources} deste modulo, e nao em uma pasta de nivel
 * superior do repositorio: o contexto de build da imagem Docker do backend
 * e apenas {@code backend/}, entao qualquer YAML fora dele nao estaria
 * disponivel dentro do container).
 */
public class DetectionRuleYamlLoader {

    private static final String LOCATION_PATTERN = "classpath:detection-rules/**/*.yaml";

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory()).registerModule(new JavaTimeModule());

    public List<DetectionRule> loadAll() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resolver.getResources(LOCATION_PATTERN);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan " + LOCATION_PATTERN, e);
        }

        List<DetectionRule> rules = new ArrayList<>();
        for (Resource resource : resources) {
            rules.add(load(resource));
        }
        rules.sort(Comparator.comparing(DetectionRule::id));
        return rules;
    }

    private DetectionRule load(Resource resource) {
        String resourceName = resource.getFilename() == null ? resource.getDescription() : resource.getFilename();
        DetectionRuleYamlDocument document;
        try (InputStream input = resource.getInputStream()) {
            document = yamlMapper.readValue(input, DetectionRuleYamlDocument.class);
        } catch (IOException e) {
            throw new InvalidDetectionRuleException(resourceName, "could not parse YAML: " + e.getMessage());
        }

        requireNonBlank(resourceName, document.id(), "id");
        requireNonBlank(resourceName, document.name(), "name");
        requireNonBlank(resourceName, document.evaluatorType(), "evaluatorType");
        if (document.eventTypes() == null || document.eventTypes().isEmpty()) {
            throw new InvalidDetectionRuleException(resourceName, "eventTypes must not be empty");
        }
        if (document.aggregationWindow() == null) {
            throw new InvalidDetectionRuleException(resourceName, "aggregationWindow is required");
        }

        Severity severity;
        try {
            severity = Severity.valueOf(document.severity());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidDetectionRuleException(resourceName, "invalid severity: " + document.severity());
        }

        try {
            return new DetectionRule(
                    document.id(), document.name(), document.description(), document.enabled(),
                    document.eventTypes(), document.evaluatorType(), document.conditions(),
                    document.aggregationWindow(), document.threshold(), severity, document.baseRiskScore(),
                    document.mitreTactic(), document.mitreTechnique(), document.recommendedPlaybook(),
                    document.version(), document.falsePositiveNotes(), document.references(), document.author(),
                    document.createdAt(), document.updatedAt());
        } catch (RuntimeException e) {
            throw new InvalidDetectionRuleException(resourceName, e.getMessage());
        }
    }

    private void requireNonBlank(String resourceName, String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidDetectionRuleException(resourceName, field + " must not be blank");
        }
    }
}
