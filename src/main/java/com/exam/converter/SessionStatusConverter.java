package com.exam.converter;

import com.exam.entity.ExamSession.SessionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SessionStatusConverter implements AttributeConverter<SessionStatus, String> {

    @Override
    public String convertToDatabaseColumn(SessionStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public SessionStatus convertToEntityAttribute(String status) {
        if (status == null) {
            return null;
        }
        try {
            return SessionStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
