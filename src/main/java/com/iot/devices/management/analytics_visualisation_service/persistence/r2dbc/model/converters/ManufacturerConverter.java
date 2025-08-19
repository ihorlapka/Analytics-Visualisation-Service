package com.iot.devices.management.analytics_visualisation_service.persistence.r2dbc.model.converters;

import com.iot.devices.management.analytics_visualisation_service.persistence.enums.DeviceManufacturer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ManufacturerConverter implements Converter<String, DeviceManufacturer> {

    @Override
    public DeviceManufacturer convert(String source) {
        return DeviceManufacturer.valueOf(source);
    }
}
