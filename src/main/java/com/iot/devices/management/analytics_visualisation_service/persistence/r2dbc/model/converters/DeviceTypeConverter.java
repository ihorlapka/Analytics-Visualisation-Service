package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.converters;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class DeviceTypeConverter implements Converter<String, DeviceType> {

    @Override
    public DeviceType convert(String source) {
        return DeviceType.valueOf(source);
    }
}
