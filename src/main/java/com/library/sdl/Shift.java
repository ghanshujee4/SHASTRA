package com.library.sdl;


import jakarta.persistence.*;


@Entity
@Table(name = "shift_records")
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;

    public Shift(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Shift() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
