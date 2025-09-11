package com.victoruk.dicestore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "roles")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Size(max = 50)
    @NonNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<Customer> customers = new HashSet<>();

}
