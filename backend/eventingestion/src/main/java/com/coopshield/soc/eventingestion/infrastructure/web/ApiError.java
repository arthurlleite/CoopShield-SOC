package com.coopshield.soc.eventingestion.infrastructure.web;

import java.util.List;

public record ApiError(String error, String message, List<String> violations) {
}
