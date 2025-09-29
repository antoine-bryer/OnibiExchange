package com.onibiexchange.service.impl;


import com.onibiexchange.model.ShopItem;
import com.onibiexchange.model.User;
import com.onibiexchange.model.UserItem;
import com.onibiexchange.repository.ShopItemRepository;
import com.onibiexchange.repository.UserItemRepository;
import com.onibiexchange.repository.UserRepository;
import com.onibiexchange.service.IShopItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopItemServiceImpl implements IShopItemService {

    private final ShopItemRepository shopItemRepository;
    private final UserRepository userRepository;
    private final UserItemRepository userItemRepository;

    @Override
    public List<ShopItem> listItems() {
        return shopItemRepository.findAll();
    }

    @Override
    public String buyItem(User user, Long itemId, int amount) {
        Optional<ShopItem> optItem = shopItemRepository.findById(itemId);
        if (optItem.isEmpty()) {
            return "❌ Item not found.";
        }

        ShopItem item = optItem.get();
        if (user.getBalance() < (item.getPrice() * amount)) {
            return "💸 Not enough coins to buy **" + item.getName() + "**!";
        }

        user.setBalance(user.getBalance() - (item.getPrice() * amount));
        userRepository.save(user);

        // Inventory
        Optional<UserItem> optInventory = userItemRepository.findByUserAndItem(user, item);
        UserItem inventoryItem;
        if (optInventory.isPresent()) {
            inventoryItem = optInventory.get();
            inventoryItem.setQuantity(inventoryItem.getQuantity() + amount);
        } else {
            inventoryItem = UserItem.builder()
                    .user(user)
                    .item(item)
                    .quantity(amount)
                    .build();
        }
        userItemRepository.save(inventoryItem);

        return "✅ You bought **" + item.getName() + "** for " + item.getPrice() + " coins!";
    }
}
