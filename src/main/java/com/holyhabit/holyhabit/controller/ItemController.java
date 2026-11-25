package com.holyhabit.holyhabit.controller;

import com.holyhabit.holyhabit.controller.dto.ItemResponse;
import com.holyhabit.holyhabit.security.CustomUserDetails;
import com.holyhabit.holyhabit.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 가방 목록 — GET /items?type=EFFECT (전체 목록 + 보유·착용 여부)
    @GetMapping
    public ResponseEntity<List<ItemResponse>> myItems(
            @RequestParam(defaultValue = "EFFECT") String type,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(itemService.getMyItems(userDetails.getUserId(), type));
    }

    // 이펙트 착용 — POST /items/{itemId}/equip → 바뀐 이펙트 목록
    @PostMapping("/{itemId}/equip")
    public ResponseEntity<List<ItemResponse>> equip(
            @PathVariable Long itemId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        itemService.equipEffect(userDetails.getUserId(), itemId);
        return ResponseEntity.ok(itemService.getMyItems(userDetails.getUserId(), "EFFECT"));
    }
}
