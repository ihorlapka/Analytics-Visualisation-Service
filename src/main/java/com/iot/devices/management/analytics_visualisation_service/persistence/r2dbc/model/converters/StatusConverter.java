package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.converters;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StatusConverter implements Converter<String, DeviceStatus> {

    @Override
    public DeviceStatus convert(String source) {
        return DeviceStatus.valueOf(source);
    }
}
