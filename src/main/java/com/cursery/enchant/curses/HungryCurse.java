package com.cursery.enchant.curses;

import com.cursery.Cursery;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class HungryCurse extends Enchantment
{
    /**
     * Enchant id
     */
    public static final  String NAME_ID = "curse_hungry";
    private final static int    CHANCE  = 15;

    public HungryCurse(final Rarity rarity, final EquipmentSlot[] slotTypes)
    {
        super(rarity, EnchantmentCategory.ARMOR, slotTypes);
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchant)
    {
        return this != enchant;
    }

    @Override
    public void doPostHurt(LivingEntity user, Entity attacker, int enchantLevel)
    {
        if (Cursery.rand.nextInt(CHANCE) == 0)
        {
            user.addEffect(new MobEffectInstance(MobEffects.HUNGER, 6 * 20 * enchantLevel));
        }
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity attacker, int enchantLevel)
    {
        if (Cursery.rand.nextInt(CHANCE) == 0)
        {
            user.addEffect(new MobEffectInstance(MobEffects.HUNGER, 6 * 20 * enchantLevel));
        }
    }

    @Override
    public boolean canEnchant(ItemStack stack)
    {
        return stack.getItem() instanceof ArmorItem || stack.getItem() instanceof SwordItem;
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
    public boolean isTreasureOnly()
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
        return 3;
    }
}
