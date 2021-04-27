package com.cursery.enchant;

import com.cursery.Cursery;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Helps visualizing curse application to the player
 */
public class PlayerVisualHelper
{
    public static void randomNotificationOnCurseApply(final ServerPlayerEntity playerEntity, final ItemStack cursed)
    {
        // Todo: add particle/sound alternatives

        playerEntity.sendMessage(new TranslationTextComponent(
          "enchant_curse_applied_" + (Cursery.rand.nextInt(4) + 1) + ".desc").setStyle(Style.EMPTY.withColor(TextFormatting.DARK_PURPLE)), playerEntity.getUUID());
        //playerEntity.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundCategory.MASTER,0.4f,1.0f);
        //playerEntity.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundCategory.MASTER,0.4f,1.0f);
        playerEntity.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundCategory.MASTER, 0.2f, 1.0f);
        playerEntity.level.addParticle(ParticleTypes.ASH, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), 1.0, 1.0, 1.0);
        playerEntity.level.addParticle(ParticleTypes.ASH, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), 0, 1.0, 1.0);
        playerEntity.level.addParticle(ParticleTypes.ASH, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), 1.0, 0, 0);
    }
}
