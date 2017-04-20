package com.frank.service;

/**
 * Created by frank on 17/4/20.
 */
public interface TokenService {
    String createToken(String name, String password);

    String parseToken(String token);


}
