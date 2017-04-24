package com.frank.service.impl;

import com.frank.dto.JsonResult;
import com.frank.service.ValidateService;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by frank on 17/4/24.
 */
@Service
public class ValidateServiceImpl implements ValidateService {
    @Override
    public JsonResult validate(BindingResult bindingResult) {
        Map<String,String> map_errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()){
            map_errors.put(fieldError.getField(),fieldError.getDefaultMessage());
        }
        return new JsonResult<>(400,"参数错误",map_errors);
    }
}
