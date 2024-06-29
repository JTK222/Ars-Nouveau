package com.hollingsworth.arsnouveau.common.crafting.recipes;

import com.hollingsworth.arsnouveau.api.perk.IPerkHolder;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.items.data.ArmorPerkHolder;
import com.hollingsworth.arsnouveau.setup.registry.RecipeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class ArmorUpgradeRecipe extends EnchantingApparatusRecipe implements ITextOutput{

    public int tier; // 0 indexed

//    public ArmorUpgradeRecipe(List<Ingredient> pedestalItems, int cost, int tier) {
//        this(ArsNouveau.prefix( "upgrade_" + tier), pedestalItems, cost, tier);
//    }

    public ArmorUpgradeRecipe(List<Ingredient> pedestalItems, int cost, int tier) {
        super(Ingredient.EMPTY, ItemStack.EMPTY, pedestalItems, cost, false);
        this.tier = tier;
    }

    public int tier(){
        return tier;
    }

    @Override
    public boolean excludeJei() {
        return true;
    }

    @Override
    public boolean doesReagentMatch(ApparatusRecipeInput input, Level level, @org.jetbrains.annotations.Nullable Player player) {
        return true;
    }

    @Override
    public boolean matches(ApparatusRecipeInput input, Level level, @org.jetbrains.annotations.Nullable Player player) {
        IPerkHolder<ItemStack> perkHolder = PerkUtil.getPerkHolder(input.catalyst());
        if(!(perkHolder instanceof ArmorPerkHolder armorPerkHolder)){
            return false;
        }
        return armorPerkHolder.getTier() == (tier - 1) && super.matches(input, level, player);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack assemble(ApparatusRecipeInput input, HolderLookup.Provider p_346030_) {
        ItemStack reagent = input.catalyst();
        IPerkHolder<ItemStack> perkHolder = PerkUtil.getPerkHolder(reagent);
        if(!(perkHolder instanceof ArmorPerkHolder armorPerkHolder)){
            return reagent.copy();
        }
        armorPerkHolder.setTier(tier);
        return reagent.copy();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeRegistry.ARMOR_UPGRADE_TYPE.get();
    }

    @Override
    public Component getOutputComponent() {
        return Component.translatable("ars_nouveau.armor_upgrade.book_desc", tier);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.ARMOR_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<ArmorUpgradeRecipe> {
        public static MapCodec<ArmorUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("pedestalItems").forGetter(ArmorUpgradeRecipe::pedestalItems),
                Codec.INT.fieldOf("sourceCost").forGetter(ArmorUpgradeRecipe::sourceCost),
                Codec.INT.fieldOf("tier").forGetter(ArmorUpgradeRecipe::tier)
        ).apply(instance, ArmorUpgradeRecipe::new));

        public static StreamCodec<RegistryFriendlyByteBuf, ArmorUpgradeRecipe> STREAM_CODEC = CheatSerializer.create(CODEC);

        @Override
        public MapCodec<ArmorUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ArmorUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
