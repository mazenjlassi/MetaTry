package com.example.metatry.Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 4000)
    private String description;

    @Column(length = 2000)
    private String mission;

    @ElementCollection
    private List<String> values;

    @ElementCollection
    private List<String> services;

    private String brandTone;

    private String targetAudience;
}