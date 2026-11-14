package com.portfoliohub.catalog;

import java.util.List;

public record Blueprint(
        boolean studentSafe,
        String disclaimer,
        List<String> competencies,
        List<String> examples,
        List<SectionDef> sections
) {
}
