package com.mohaymen.repository;

import com.mohaymen.model.entity.Block;
import com.mohaymen.model.supplies.ProfilePareId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, ProfilePareId> {
}
