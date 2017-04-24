package com.frank.service;

import com.frank.dto.JsonResult;
import org.springframework.validation.BindingResult;

/**
 * Created by frank on 17/4/24.
 */
public interface ValidateService {
    JsonResult validate(BindingResult bindingResult);
}
