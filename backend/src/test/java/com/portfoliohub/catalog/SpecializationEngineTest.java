package com.portfoliohub.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecializationEngineTest {

    private final SpecializationEngine engine = new SpecializationEngine();

    @Test
    void systemsAndMedicineDoNotShareTheSameStructure() {
        Blueprint systems = engine.resolve("ING-SIS", "ENG");
        Blueprint medicine = engine.resolve("MED-HUM", "MED");
        assertTrue(systems.sections().stream().anyMatch(section -> "repositories".equals(section.key())));
        assertFalse(medicine.sections().stream().anyMatch(section -> "repositories".equals(section.key())));
        assertTrue(medicine.studentSafe());
        assertTrue(medicine.disclaimer().toLowerCase().contains("medicina"));
        assertFalse(systems.studentSafe());
    }

    @Test
    void lawAndArchitectureUseDifferentComponents() {
        Blueprint law = engine.resolve("DERECHO", "LAW");
        Blueprint architecture = engine.resolve("ARQ", "ARCH");
        assertTrue(law.sections().stream().anyMatch(section -> "legal_areas".equals(section.key())));
        assertTrue(architecture.sections().stream().anyMatch(section -> "gallery".equals(section.component()) || "projects".equals(section.key())));
        assertTrue(law.disclaimer().toLowerCase().contains("abogac"));
    }
}
