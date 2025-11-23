package com.lpa.demon_slayer_api_service.model;

import java.util.List;

/**
  * Represents an element that has a unique identifier to allow generic sorting of objects based on their ID.
  * It is used in  {@link com.lpa.demon_slayer_api_service.utils.DemonSlayerApiUtils#sortById(List)}
  *
  */
public interface Identifiable {
    Long id();
}
