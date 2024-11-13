package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.SocialLogin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {

}
