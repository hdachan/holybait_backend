package com.holyhabit.holyhabit.controller.dto;

import com.holyhabit.holyhabit.entity.Item;
import lombok.Getter;

// 가방 화면 한 칸
@Getter
public class ItemResponse {

    private final Long id;
    private final String type;
    private final String itemKey;
    private final String name;
    private final String description;
    private final Integer priceKrw;   // null = 판매 안 함
    private final String productId;   // Play 상품 ID (null = 아직 결제 준비 안 됨)
    private final boolean owned;
    private final boolean equipped;
    private final int quantity;

    public ItemResponse(Item item, boolean owned, boolean equipped, int quantity) {
        this.id = item.getId();
        this.type = item.getType();
        this.itemKey = item.getItemKey();
        this.name = item.getName();
        this.description = item.getDescription();
        this.priceKrw = item.getPriceKrw();
        this.productId = item.getProductId();
        this.owned = owned;
        this.equipped = equipped;
        this.quantity = quantity;
    }
}
