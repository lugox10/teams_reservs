package com.lugo.teams.reservs.infrastructure.percistence.adapters;

import com.lugo.teams.reservs.domain.model.ReservUser;
import com.lugo.teams.reservs.domain.repository.ReservUserRepository;
import com.lugo.teams.reservs.infrastructure.percistence.jpa.DataReservUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaReservUserRepositoryAdapter implements ReservUserRepository {

    private final DataReservUserRepository repo;

    public JpaReservUserRepositoryAdapter(DataReservUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public ReservUser save(ReservUser user) {
        return repo.save(user);
    }

    @Override
    public Optional<ReservUser> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Optional<ReservUser> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    @Override
    public Optional<ReservUser> findByPhone(String phone) {
        return repo.findByPhone(phone);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repo.existsByEmail(email);
    }

    @Override
    public Optional<ReservUser> findByUsernameOrEmailOrIdentification(String username, String email, String identification) {
        return repo.findByUsernameOrEmailOrIdentification(username, email, identification);
    }

    @Override
    public Optional<ReservUser> findByIdentification(String identification) {
        return repo.findByIdentification(identification);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repo.existsByUsername(username);
    }

    @Override
    public boolean existsByIdentification(String identification) {
        return repo.existsByIdentification(identification);
    }

    @Override
    public Optional<ReservUser> findByUsername(String username) {
        return repo.findByUsername(username);
    }
}
