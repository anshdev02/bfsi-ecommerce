package com.bfsi.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true)
    private ERole name;

    public enum ERole {
        ROLE_USER,
        ROLE_ADMIN,
        ROLE_BANKER
    }

    public Role() {}

    public Role(Integer id, ERole name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId()  { return id; }
    public ERole getName()  { return name; }
    public void setId(Integer id)    { this.id = id; }
    public void setName(ERole name)  { this.name = name; }
}
