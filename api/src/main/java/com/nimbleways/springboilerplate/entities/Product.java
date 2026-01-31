package com.nimbleways.springboilerplate.entities;

import lombok.*;

import java.time.LocalDate;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "available")
    private Integer available;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ProductCateg type;

    @Column(name = "name")
    private String name;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "season_start_date")
    private LocalDate seasonStartDate;

    @Column(name = "season_end_date")
    private LocalDate seasonEndDate;

    public boolean isInStock() {
        return available != null && available > 0;
    }

    public boolean isExpired() {
        return expiryDate != null && !expiryDate.isAfter(LocalDate.now());
    }

    public boolean isInSeason() {
        LocalDate now = LocalDate.now();
        return seasonStartDate != null && seasonEndDate != null
                && !now.isBefore(seasonStartDate) && now.isBefore(seasonEndDate);
    }


    public void decrementAvailable() {
        if (available != null && available > 0) {
            available -= 1;
        }
    }

}
