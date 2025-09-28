package com.onibiexchange.repository;

import com.onibiexchange.model.ShopItem;
import com.onibiexchange.model.User;
import com.onibiexchange.model.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    List<UserItem> findByUser(User user);

    Optional<UserItem> findByUserAndItem(User user, ShopItem item);

}
