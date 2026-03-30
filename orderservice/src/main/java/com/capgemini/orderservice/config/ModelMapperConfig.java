package com.capgemini.orderservice.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // Sets ModelMapper to STRICT mode so only exact matching fields are mapped, avoiding incorrect or unwanted mappings
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//        return new ModelMapper();
        return modelMapper;
    }
}
