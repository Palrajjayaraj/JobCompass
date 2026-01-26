package com.jobcompass.storage.config;

import com.jobcompass.common.model.Source;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for Source record.
 * Converts Source record to String for database storage.
 * 
 * @author Palrajjayaraj
 */
@Converter(autoApply = true)
public class SourceConverter implements AttributeConverter<Source, String> {

    @Override
    public String convertToDatabaseColumn(Source source) {
        if (source == null) {
            return null;
        }
        return source.name();
    }

    @Override
    public Source convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return Source.of(dbData);
    }
}
