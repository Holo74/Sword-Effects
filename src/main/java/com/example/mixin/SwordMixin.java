package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.ExampleMod;
import com.example.SwordEffectsHelper;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

@Mixin(SwordItem.class)
public abstract class SwordMixin extends ToolItem {

    public SwordMixin(ToolMaterial material, Settings settings) {
        super(material, settings);
        // TODO Auto-generated constructor stub
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ExampleMod.LOGGER.info("Finished using the sword");

        ItemStack potionStackInHand = user.getStackInHand(Hand.MAIN_HAND);
        if (!SwordEffectsHelper.CanApplyItem(user)) {
            return stack;
        }

        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity) user : null;
        if (playerEntity instanceof ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity) playerEntity, potionStackInHand);
        }

        if (playerEntity != null) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(potionStackInHand.getItem()));
            potionStackInHand.decrementUnlessCreative(1, playerEntity);
        }

        if (playerEntity == null || !playerEntity.isInCreativeMode()) {
            if (potionStackInHand.isEmpty()) {
                playerEntity.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                nbtStuff -> nbtStuff.apply(current -> {
                    current.putInt("PotionCount", SwordEffectsHelper.GetSwordUses(potionStackInHand));
                }));
        stack.set(DataComponentTypes.POTION_CONTENTS, SwordEffectsHelper.GetEffects(potionStackInHand));

        ExampleMod.LOGGER.info("Applied the potion");

        return stack;

    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemHeld = user.getStackInHand(hand);
        ExampleMod.LOGGER.info(itemHeld.getName() + " Is being used");

        if (SwordEffectsHelper.CanApplyItem(user, hand)) {
            ExampleMod.LOGGER.info("Potion can be applied");
            user.setCurrentHand(hand);
            return ItemUsage.consumeHeldItem(world, user, hand);
        }
        return TypedActionResult.pass(itemHeld);
    }

    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Inject(at = @At("TAIL"), method = "postDamageEntity")
    private void ApplyPotion(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo info) {
        if (SwordEffectsHelper.HasPotionOnIt(stack)) {
            SwordEffectsHelper.ApplyEffects(stack, target, attacker);

            int potionCount = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getInt("PotionCount") - 1;

            stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbtStuff -> nbtStuff.apply(current -> {
                current.putInt("PotionCount", potionCount);
            }));

            if (potionCount <= 0) {
                stack.remove(DataComponentTypes.POTION_CONTENTS);

                NbtCompound compound = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                compound.remove("PotionCount");
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));
            }
        }
    }
}
