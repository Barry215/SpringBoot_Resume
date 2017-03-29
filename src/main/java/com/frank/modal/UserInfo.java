package com.frank.modal;

/**
 * Created by frank on 17/3/29.
 */
public class UserInfo {
    private int user_id;
    private String name;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "user_id=" + user_id +
                ", name='" + name + '\'' +
                '}';
    }
}
