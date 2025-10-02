package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.converters;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.UserRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;

@ReadingConverter
public class UserRoleConverter  implements Converter<String, UserRole> {

    @Override
    public UserRole convert(@NonNull String source) {
        return UserRole.valueOf(source);
    }
}
