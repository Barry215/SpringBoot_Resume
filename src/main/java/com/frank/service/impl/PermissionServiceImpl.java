package com.frank.service.impl;

import com.frank.dao.PermissionMapper;
import com.frank.model.Permission;
import com.frank.service.PermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by frank on 17/4/25.
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public List<Permission> selectAllPermission() {
        return permissionMapper.selectAll();
    }
}
