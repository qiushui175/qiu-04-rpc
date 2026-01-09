package com.qiu.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author qiu
 * @version 1.0
 * @className User
 * @packageName com.qiu.example.common.model
 * @Description
 * @date 2026/1/9 15:31
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 367606740032721277L;

    private String name;

    private Integer id;
}
