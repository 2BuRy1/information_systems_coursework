package ru.itmo.codetogether.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.itmo.codetogether.model.TaskStatus;

@Converter(autoApply = true)
public class TaskStatusConverter implements AttributeConverter<TaskStatus, String> {
    @Override
    public String convertToDatabaseColumn(TaskStatus attribute) {
        return attribute != null ? attribute.getValue() : TaskStatus.OPEN.getValue();
    }

    @Override
    public TaskStatus convertToEntityAttribute(String dbData) {
        return TaskStatus.fromString(dbData);
    }
}
