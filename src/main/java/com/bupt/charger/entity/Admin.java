package com.bupt.charger.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;


/**
 * 管理员实体类
 */
@Data
@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String adminName;

    @NonNull
    private String password;


    public Admin() {}

    public Admin(String adminName, String password) {
        setUsername(adminName);
        setPassword(password);
    }

    private void setUsername(String administratorName) {
        this.adminName=adminName;
    }


}
