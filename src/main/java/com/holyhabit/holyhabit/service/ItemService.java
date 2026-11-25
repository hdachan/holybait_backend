package com.holyhabit.holyhabit.service;

import com.holyhabit.holyhabit.controller.dto.ItemResponse;
import com.holyhabit.holyhabit.entity.Item;
import com.holyhabit.holyhabit.entity.UserItem;
import com.holyhabit.holyhabit.repository.ItemRepository;
import com.holyhabit.holyhabit.repository.UserItemRepository;
import com.holyhabit.holyhabit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// 가방 (아이템 보유·착용)
// - 기본 아이템(is_default)은 user_items 행이 없어도 보유로 취급 → 기존 유저에게 따로 지급할 필요 없음
// - 이펙트는 하나만 착용. 착용한 행이 없으면 기본 이펙트를 착용한 것으로 봄
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ItemResponse> getMyItems(Long userId, String type) {
        List<Item> items = itemRepository.findAllByTypeAndIsActiveTrueOrderBySortOrderAscIdAsc(type);
        Map<Long, UserItem> mine = mineByItemId(userId, type);

        Long equippedId = Item.TYPE_EFFECT.equals(type)
                ? findEquippedEffect(items, mine).map(Item::getId).orElse(null)
                : null;

        return items.stream().map(item -> {
            UserItem ui = mine.get(item.getId());
            boolean owned = ui != null || Boolean.TRUE.equals(item.getIsDefault());
            int qty = ui != null ? ui.getQuantity() : (owned ? 1 : 0);
            return new ItemResponse(item, owned, item.getId().equals(equippedId), qty);
        }).toList();
    }

    // 이펙트 착용
    @Transactional
    public void equipEffect(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("아이템을 찾을 수 없습니다."));
        if (!Item.TYPE_EFFECT.equals(item.getType()) || !Boolean.TRUE.equals(item.getIsActive())) {
            throw new RuntimeException("착용할 수 없는 아이템입니다.");
        }
        UserItem target = userItemRepository.findByUserIdAndItemId(userId, itemId).orElse(null);
        if (target == null && !Boolean.TRUE.equals(item.getIsDefault())) {
            throw new RuntimeException("가지고 있지 않은 이펙트입니다.");
        }
        // 다른 이펙트는 모두 해제
        for (UserItem ui : userItemRepository.findAllByUserIdAndType(userId, Item.TYPE_EFFECT)) {
            if (ui != target && Boolean.TRUE.equals(ui.getEquipped())) ui.setEquipped(false);
        }
        if (target != null) {
            target.setEquipped(true);
        } else {
            // 기본 이펙트는 행이 없으므로 착용할 때 만듦
            userItemRepository.save(UserItem.builder()
                    .user(userRepository.getReferenceById(userId))
                    .item(item)
                    .equipped(true)
                    .source("DEFAULT")
                    .build());
        }
    }

    // 배틀에서 쓸 내 공격 이펙트 키 (없으면 null → 앱 기본값)
    @Transactional(readOnly = true)
    public String getEquippedEffectKey(Long userId) {
        List<Item> items = itemRepository.findAllByTypeAndIsActiveTrueOrderBySortOrderAscIdAsc(Item.TYPE_EFFECT);
        return findEquippedEffect(items, mineByItemId(userId, Item.TYPE_EFFECT))
                .map(Item::getItemKey).orElse(null);
    }

    private Map<Long, UserItem> mineByItemId(Long userId, String type) {
        return userItemRepository.findAllByUserIdAndType(userId, type).stream()
                .collect(Collectors.toMap(ui -> ui.getItem().getId(), Function.identity()));
    }

    // 착용한 행이 있는 이펙트 → 없으면 기본 이펙트 (판매 중지된 이펙트를 착용 중이어도 기본으로)
    private Optional<Item> findEquippedEffect(List<Item> items, Map<Long, UserItem> mine) {
        for (Item item : items) {
            UserItem ui = mine.get(item.getId());
            if (ui != null && Boolean.TRUE.equals(ui.getEquipped())) return Optional.of(item);
        }
        return items.stream().filter(i -> Boolean.TRUE.equals(i.getIsDefault())).findFirst();
    }
}
