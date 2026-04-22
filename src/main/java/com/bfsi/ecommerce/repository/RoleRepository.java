package com.bfsi.ecommerce.repository;

import com.bfsi.ecommerce.entity.Role;
import com.bfsi.ecommerce.entity.Role.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
