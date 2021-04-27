package com.cursery.enchant.curses;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

/**
 * Chance for bows to only fire short arrows
 */
public class StubbyCurse extends Enchantment
{
    /**
     * Enchant id
     */
    private final       String NAME_ID = "curse_stubby";
    public final static int    CHANCE  = 4;

    public StubbyCurse(final Rarity rarity, final EquipmentSlotType[] slotTypes)
    {
        super(rarity, EnchantmentType.WEAPON, slotTypes);
        setRegistryName(NAME_ID);
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchant)
    {
        return this != enchant;
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity attacker, int enchantLevel)
    {
    }

    @Override
    public boolean canEnchant(ItemStack stack)
    {
        return stack.getItem() instanceof BowItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean isCurse()
    {
        return true;
    }

    @Override
    public int getMinLevel()
    {
        return 1;
    }

    @Override
    public int getMaxLevel()
    {
        return 1;
    }
}
