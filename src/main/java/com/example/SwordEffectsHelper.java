package com.example;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

public class SwordEffectsHelper {
    public static boolean AcceptableItems(Item item) {
        if (item instanceof PotionItem) {
            return true;
        }
        if (item instanceof SuspiciousStewItem) {
            return true;
        }
        return false;
    }

    public static boolean CanApplyItem(LivingEntity user) {
        if (!(user instanceof PlayerEntity)) {
            return false;
        }
        Hand hand = Hand.OFF_HAND;
        ItemStack itemStackInHand = user.getStackInHand(Hand.OFF_HAND);
        ItemStack stack = user.getStackInHand(Hand.MAIN_HAND);
        return itemStackInHand.getItem() instanceof SwordItem && user instanceof PlayerEntity
                && SwordEffectsHelper.AcceptableItems(stack.getItem()) && hand == Hand.OFF_HAND;
    }

    public static boolean CanApplyItem(PlayerEntity user, Hand hand) {
        ItemStack itemStackInHand = user.getStackInHand(Hand.OFF_HAND);
        ItemStack stack = user.getStackInHand(Hand.MAIN_HAND);
        return itemStackInHand.getItem() instanceof SwordItem && user instanceof PlayerEntity
                && SwordEffectsHelper.AcceptableItems(stack.getItem()) && hand == Hand.OFF_HAND;
    }

    public static boolean HasPotionOnIt(ItemStack stack) {
        return stack.get(DataComponentTypes.POTION_CONTENTS) != null
                && stack.get(DataComponentTypes.CUSTOM_DATA).contains("PotionCount");
    }

    public static void ApplyEffects(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        PotionContentsComponent potionContentsComponent = (PotionContentsComponent) stack
                .getOrDefault(DataComponentTypes.POTION_CONTENTS,
                        PotionContentsComponent.DEFAULT);
        potionContentsComponent.forEachEffect((effect) -> {
            if (((StatusEffect) effect.getEffectType().value()).isInstant()) {
                ((StatusEffect) effect.getEffectType().value()).applyInstantEffect(attacker,
                        attacker, target,
                        effect.getAmplifier(), 1.0);
            } else {
                target.addStatusEffect(effect);
            }
        });
    }

    public static PotionContentsComponent GetEffects(ItemStack consumedItem) {
        if (consumedItem.getItem() instanceof PotionItem) {
            return consumedItem.get(DataComponentTypes.POTION_CONTENTS);
        }
        return null;
    }

    public static int GetSwordUses(ItemStack potion) {

        return 15;
    }
}
