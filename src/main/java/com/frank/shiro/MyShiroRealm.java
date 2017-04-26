package com.frank.shiro;

import com.frank.dao.RolePermissionMapper;
import com.frank.dao.UserMapper;

import com.frank.dao.UserRoleMapper;
import com.frank.model.RolePermission;
import com.frank.model.UserRole;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * shiro身份校验核心类
 * 
 * @author 作者: frank
 * @date 创建时间：2017年4月25日 下午18:19:48
 */

public class MyShiroRealm extends AuthorizingRealm {

    @Resource
    private UserMapper userMapper;

	@Resource
	private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;
	
	@Resource
    StringRedisTemplate stringRedisTemplate;

	private Logger log = Logger.getLogger(MyShiroRealm.class);

	//用户登录次数计数  redisKey 前缀
	private static final String SHIRO_LOGIN_COUNT = "shiro_login_count_";
	
	//用户登录是否被锁定    一小时 redisKey 前缀
	private static final String SHIRO_IS_LOCK = "shiro_is_lock_";

	/**
	 * 认证信息.(身份验证) : Authentication 是用来验证用户身份
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {

		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();

		UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authcToken;
		String name = usernamePasswordToken.getUsername();
		String password = String.valueOf(usernamePasswordToken.getPassword());

		if ("LOCK".equals(opsForValue.get(SHIRO_IS_LOCK + name))){
			throw new DisabledAccountException("由于密码输入错误次数大于5次，帐号已经禁止登录！");
		}

		//访问一次，计数一次
		opsForValue.increment(SHIRO_LOGIN_COUNT+name, 1);

        Integer user_id = userMapper.selectByNameAndPwd(name,password);

		if (user_id == null){
			//计数大于5时，设置用户被锁定一小时
			if(Integer.parseInt(opsForValue.get(SHIRO_LOGIN_COUNT+name))>=5){
                opsForValue.set(SHIRO_LOGIN_COUNT+name, "0");
                opsForValue.set(SHIRO_IS_LOCK+name, "LOCK");
				stringRedisTemplate.expire(SHIRO_IS_LOCK+name, 1, TimeUnit.HOURS);
			}
			throw new AccountException("帐号或密码不正确！");
		}else {
			opsForValue.set(SHIRO_LOGIN_COUNT+name, "0");
		}

		log.info("身份认证成功，登录用户：" + name);
		return new SimpleAuthenticationInfo(user_id, password, getName());
	}

	/**
	 * 授权
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        Integer user_id = (Integer)SecurityUtils.getSubject().getPrincipal();

		SimpleAuthorizationInfo info =  new SimpleAuthorizationInfo();

        UserRole userRole = userRoleMapper.selectByUserId(user_id);

//		Set<String> roleSet = new HashSet<>();
//		roleSet.add(userRole.getRole().getRoleName());
//		info.setRoles(roleSet);

        info.addRole(userRole.getRole().getRoleName());

		List<RolePermission> rolePermissionList = rolePermissionMapper.selectByRoleId(userRole.getId());
		Set<String> permissionSet = new HashSet<String>();
        for (RolePermission rolePermission : rolePermissionList){
            permissionSet.add(rolePermission.getPermission().getPmName());
        }
		info.setStringPermissions(permissionSet);
        return info;
	}
}
