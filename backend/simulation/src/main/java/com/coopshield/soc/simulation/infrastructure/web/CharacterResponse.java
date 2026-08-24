package com.coopshield.soc.simulation.infrastructure.web;

import com.coopshield.soc.simulation.domain.Character;

public record CharacterResponse(String id, String displayName, String role, String unit) {

    public static CharacterResponse from(Character character) {
        return new CharacterResponse(character.id(), character.displayName(), character.role(), character.unit());
    }
}
