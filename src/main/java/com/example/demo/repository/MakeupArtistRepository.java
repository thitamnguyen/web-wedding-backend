package com.example.demo.repository;

import com.example.demo.model.MakeupArtist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MakeupArtistRepository extends JpaRepository<MakeupArtist, Long> {
}
