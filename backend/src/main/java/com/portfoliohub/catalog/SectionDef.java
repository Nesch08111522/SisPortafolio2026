package com.portfoliohub.catalog;

public record SectionDef(
        String key,
        String title,
        String component,
        String variant,
        boolean recommended,
        String hint
) {
}
