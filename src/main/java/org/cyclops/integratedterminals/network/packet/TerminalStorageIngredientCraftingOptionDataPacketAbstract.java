package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;

import javax.annotation.Nullable;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public abstract class TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M> extends PacketCodec {

    @CodecField
    private String ingredientComponent;
    @CodecField
    private BlockPos pos;
    @CodecField
    private Direction side;
    @CodecField
    private String tabName;
    @CodecField
    private int channel;
    @CodecField
    private CompoundNBT craftingOption;
    @CodecField
    private int amount;
    @CodecField
    private CompoundNBT craftingPlan;

    public TerminalStorageIngredientCraftingOptionDataPacketAbstract() {

    }

    public TerminalStorageIngredientCraftingOptionDataPacketAbstract(CraftingOptionGuiData<T, M> craftingOptionData) {
        this.ingredientComponent = craftingOptionData.getComponent().getName().toString();
        this.pos = craftingOptionData.getPos();
        this.side = craftingOptionData.getSide();
        this.tabName = craftingOptionData.getTabName();
        this.channel = craftingOptionData.getChannel();
        this.craftingOption = craftingOptionData.getCraftingOption() != null
                ? HandlerWrappedTerminalCraftingOption.serialize(craftingOptionData.getCraftingOption())
                : new CompoundNBT();
        this.amount = craftingOptionData.getAmount();
        this.craftingPlan = craftingOptionData.getCraftingPlan() != null
                ? HandlerWrappedTerminalCraftingPlan.serialize(craftingOptionData.getCraftingPlan())
                : new CompoundNBT();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(World world, PlayerEntity player) {

    }

    @Nullable
    protected HandlerWrappedTerminalCraftingOption<T> getCraftingOption(IngredientComponent<T, M> ingredientComponent) {
        try {
            return HandlerWrappedTerminalCraftingOption.deserialize(ingredientComponent, this.craftingOption);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    protected HandlerWrappedTerminalCraftingPlan getCraftingPlan() {
        try {
            return HandlerWrappedTerminalCraftingPlan.deserialize(this.craftingPlan);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public IngredientComponent<T, M> getIngredientComponent() {
        IngredientComponent<?, ?> component = IngredientComponent.REGISTRY.getValue(new ResourceLocation(ingredientComponent));
        if (component == null) {
            throw new IllegalArgumentException("Could not find the ingredient component type " + ingredientComponent);
        }
        return (IngredientComponent<T, M>) component;
    }

    public int getChannel() {
        return channel;
    }

    public String getTabName() {
        return tabName;
    }

    public int getAmount() {
        return amount;
    }

    public CraftingOptionGuiData<T, M> getCraftingOptionData() {
        IngredientComponent<T, M> ingredientComponent = getIngredientComponent();
        return new CraftingOptionGuiData<>(pos, side, ingredientComponent, tabName, channel,
                getCraftingOption(ingredientComponent), amount, getCraftingPlan());
    }
}