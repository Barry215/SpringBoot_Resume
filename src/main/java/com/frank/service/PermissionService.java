package com.frank.service;

import com.frank.model.Permission;

import java.util.List;

/**
 * Created by frank on 17/4/25.
 */
public interface PermissionService {
    List<Permission> selectAllPermission();
}
