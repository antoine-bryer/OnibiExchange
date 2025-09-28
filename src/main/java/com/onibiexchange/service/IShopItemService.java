package com.onibiexchange.service;

import com.onibiexchange.model.ShopItem;
import com.onibiexchange.model.User;

import java.util.List;

public interface IShopItemService {

    public List<ShopItem> listItems();

    public String buyItem(User user, Long itemId);

}
