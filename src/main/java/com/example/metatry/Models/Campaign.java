package com.example.metatry.Models;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;

    private int numberOfPosts;

    private LocalDateTime startDate;

    private int frequencyDays;

    private String platform;

    private boolean active;

    @OneToMany(mappedBy = "campaign")
    private List<Post> posts;
}
